package bus.loona.config;

import bus.loona.common.BaseException;
import bus.loona.common.Constants;
import bus.loona.common.RandomGenerator;
import bus.loona.handler.RedisEventHandler;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.DefaultProductionExceptionHandler;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.processor.UsePartitionTimeOnInvalidTimestamp;
import org.apache.kafka.streams.state.Stores;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.env.Environment;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
public class AutoConfig {

	@Configuration
	@AutoConfigureAfter(name="org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration")
	public static class MessageAutoConfig {

		@Bean @Primary
		public MessageSourceAccessor messageSourceAccessor(final MessageSource messageSource) {
			return new BaseMessageAccessor(messageSource);
		}
	}

	static class BaseMessageAccessor extends MessageSourceAccessor {

		BaseMessageAccessor(final MessageSource messageSource) {
			super(messageSource);
		}

		@Override
		public String getMessage(final String code) {
			return super.getMessage(code, Constants.DEFAULT_ERROR_MESSAGE);
		}

		@Override
		public String getMessage(final String code, final Locale locale) {
			return super.getMessage(code, Constants.DEFAULT_ERROR_MESSAGE, locale);
		}

		@Override
		public String getMessage(final String code, @Nullable final Object[] args) {
			return super.getMessage(code, args, Constants.DEFAULT_ERROR_MESSAGE);
		}

		@Override
		public String getMessage(final String code, @Nullable final Object[] args, final Locale locale) {
			return super.getMessage(code, args, Constants.DEFAULT_ERROR_MESSAGE, locale);
		}
	}

	@Configuration
	@ConditionalOnClass(name="javax.validation.executable.ExecutableValidator")
	@ConditionalOnResource(resources="classpath:META-INF/services/javax.validation.spi.ValidationProvider")
	@AutoConfigureBefore(name="org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration")
	protected static class ValidationAutoConfig {

		@Bean
		@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
		@ConditionalOnMissingBean(type={"javax.validation.Validator", "org.springframework.validation.Validator"})
		public LocalValidatorFactoryBean defaultValidator(final MessageSource source, final Environment env) {

			final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

			validator.setMessageInterpolator(new ComplicatedMessageInterpolator(source, env));
			validator.afterPropertiesSet();

			return validator;
		}
	}

	static class ComplicatedMessageInterpolator extends ResourceBundleMessageInterpolator {

		private static final String ELLIPSIS = "...";
		private static final Integer MAX_LENGTH = 50;

		private static final String VALIDATED_VALUE_STR;
		private static final Pattern INTERPOLATION_EXPR;
		private static final Pattern SPECIAL_CHARACTERS_EXPR;
		private static final Map<String, String> SPECIAL_CHARACTERS_REPLACEMENTS;

		static {
			VALIDATED_VALUE_STR = "{validatedValue}";
			INTERPOLATION_EXPR = Pattern.compile("^\\{.+}");
			SPECIAL_CHARACTERS_EXPR = Pattern.compile("[{}/$\r\n]");
			SPECIAL_CHARACTERS_REPLACEMENTS = ImmutableMap.<String, String>builder().put("$","&#36;")
					.put("/","&#47;").put("{","&#123;").put("}","&#125;").put("\r","_CR_").put("\n","_LF_").build();
		}

		private final Environment env;
		private final MessageSource messageSource;

		ComplicatedMessageInterpolator(final MessageSource messageSource, final Environment env) {
			super(new MessageSourceResourceBundleLocator(messageSource));
			this.messageSource = messageSource; this.env = env;
		}

		@Override
		public String interpolate(final Context c, final Locale o, final String s) {
			return Optional.ofNullable(super.interpolate(c, o, s)).map(String::trim).map(v -> doExtraThings(c, o, v)).orElse("");
		}

		private String doExtraThings(final Context c, final Locale o, final String s) {

			if (!INTERPOLATION_EXPR.matcher(s).matches()) { return s; }

			return Objects.equals(VALIDATED_VALUE_STR, s) ? refine(c) : doMoreIfApplicable(o, s);
		}

		private String doMoreIfApplicable(final Locale o, final String s) {

			final String k = s.substring(1, s.length() - 1);
			final String v = env.getProperty(k);
			final String value = StringUtils.hasText(v) ? v : messageSource.getMessage(k, null, k, o);

			return StringUtils.hasText(value) ? value : "";
		}

		private String shorten(final String source) {
			return source.length() > MAX_LENGTH ? source.substring(0, MAX_LENGTH) + ELLIPSIS : source;
		}

		private String sanitize(final String source) {

			final StringBuffer values = new StringBuffer();
			final Matcher matcher = SPECIAL_CHARACTERS_EXPR.matcher(source);

			while (matcher.find()) {
				matcher.appendReplacement(values, SPECIAL_CHARACTERS_REPLACEMENTS.get(matcher.group()));
			}

			return matcher.appendTail(values).toString();
		}

		private String refine(final Context context) {
			return Optional.ofNullable(context.getValidatedValue()).map(String::valueOf).map(this::shorten).map(this::sanitize).orElse("");
		}
	}

	@Configuration
	@ConditionalOnClass(name="org.springframework.data.redis.core.RedisOperations")
	@AutoConfigureAfter(name="org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration")
	protected static class RedisTemplateAutoConfig {

		@Bean @Primary
		public RedisTemplate<String, Object> redisJsonTemplate(
				final Jackson2ObjectMapperBuilder builder, final RedisConnectionFactory factory) {

			final RedisSerializer<String> ss = new StringRedisSerializer();
			final RedisSerializer<Object> os = new GenericJackson2JsonRedisSerializer(builder.build());
			final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

			redisTemplate.setConnectionFactory(factory);
			redisTemplate.setKeySerializer(ss);
			redisTemplate.setValueSerializer(os);
			redisTemplate.setHashKeySerializer(ss);
			redisTemplate.setHashValueSerializer(os);

			changeConvertTxResults(factory);

			return redisTemplate;
		}

		private void changeConvertTxResults(final RedisConnectionFactory source) {

			final ClassLoader classLoader = RedisTemplate.class.getClassLoader();

			if (ClassUtils.isPresent("io.lettuce.core.RedisClient", classLoader)) {
				((LettuceConnectionFactory)source).setConvertPipelineAndTxResults(true);
				return;
			}
			if (ClassUtils.isPresent("redis.clients.jedis.Jedis", classLoader)) {
				((JedisConnectionFactory)  source).setConvertPipelineAndTxResults(true);
			}
		}
	}

	@Slf4j
	@Configuration
	@EnableConfigurationProperties(BaseProperties.RedisConfig.class)
	@ConditionalOnClass(name="org.springframework.data.redis.core.RedisOperations")
	@AutoConfigureAfter(name=Constants.BASE_PACKAGE + ".config.AutoConfig.RedisTemplateAutoConfig")
	protected static class RedisListenerAutoConfig {

		private final BaseProperties.RedisConfig props;

		@Autowired
		protected RedisListenerAutoConfig(final BaseProperties.RedisConfig props) {
			this.props = props;
		}

		@Bean
		@ConditionalOnProperty(prefix="base.redis-config", name="enabled", havingValue="true")
		public InitializingBean redisNotificationsInitializer(final RedisConnectionFactory factory) {
			return new RedisServerConfigurer(factory, props);
		}

		@Bean
		public RedisMessageListenerContainer redisMessageListenerContainer(
				final RedisConnectionFactory factory, final RedisEventHandler handler) {

			final RedisMessageListenerContainer container = new RedisMessageListenerContainer();

			container.setConnectionFactory(factory);
			container.setErrorHandler(e -> {
				if (e instanceof BaseException) {
					log.info("BaseException -> {}", e.getMessage());
					if (Objects.nonNull(e.getCause())) { log.warn("", e.getCause()); }
					return;
				}
				log.error("", e);
			});
			container.addMessageListener(new BaseMessageListener(handler, props), new PatternTopic(props.getBasePattern()));

			return container;
		}
	}

	@Slf4j
	static class RedisServerConfigurer implements InitializingBean {

		private static final String CONFIG_NAME = "notify-keyspace-events";

		private final RedisConnectionFactory factory;
		private final BaseProperties.RedisConfig props;

		RedisServerConfigurer(final RedisConnectionFactory factory, final BaseProperties.RedisConfig props) {
			this.factory = factory; this.props = props;
		}

		@Override
		public void afterPropertiesSet() {
			configure(factory.getConnection());
		}

		private void configure(final RedisConnection connection) {

			try {
				connection.setConfig(CONFIG_NAME, customize(props.getOptions(), connection));
			} catch (final InvalidDataAccessApiUsageException e) {
				log.error("Unable to get or set redis config " + CONFIG_NAME + " Option Value", e);
				throw e;
			} finally {
				RedisConnectionUtils.releaseConnection(connection, factory);
			}
		}

		private String customize(final String options, final RedisConnection connection) {

			final String current = Optional.ofNullable(connection.getConfig(CONFIG_NAME))
					.map(p -> p.getProperty(CONFIG_NAME)).filter(StringUtils::hasText).orElse(options);
			final String customized = String.join("", ImmutableSet.copyOf(Splitter.fixedLength(1).split(current + options)));

			return customized.contains("A") ? customized.replaceAll("[^EKA]", "") : customized;
		}
	}

	@Slf4j
	static class BaseMessageListener implements MessageListener {

		private final RedisEventHandler handler;
		private final BaseProperties.RedisConfig props;

		private final Pattern pattern;

		BaseMessageListener(final RedisEventHandler handler, final BaseProperties.RedisConfig props) {

			this.props = props;
			this.handler = handler;
			this.pattern = Pattern.compile(props.getBaseMessage(), Pattern.LITERAL);
		}

		@Override
		public void onMessage(final Message message, @Nullable final byte[] pattern) {

			final String converted = convert(message, pattern);

			if (converted.startsWith(props.getBaseMessage())) {
				Optional.of(this.pattern.matcher(converted).replaceFirst("")).filter(StringUtils::hasText).ifPresent(handler::handleEvent);
			}
		}

		private String convert(final Message source, @Nullable final byte[] pattern) {

			final String message = new String(source.getBody(), StandardCharsets.UTF_8);
			final String ps = Objects.nonNull(pattern) ? new String(pattern, StandardCharsets.UTF_8) : "";

			log.info("Redis Event Message From {} -> {}", ps, message);

			return StringUtils.hasText(message) ? message : Constants.NOT_AVAILABLE;
		}
	}

	@Configuration
	@Profile("kafka")
	@EnableConfigurationProperties(BaseProperties.Kafka.class)
	@AutoConfigureAfter(name="org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
	protected static class KafkaAutoConfig {

		private final BaseProperties.Kafka props;

		@Autowired
		protected KafkaAutoConfig(final BaseProperties.Kafka props) {
			this.props = props;
		}

		@Bean
		public KafkaRecordProducer kafkaRecordProducer(final KafkaTemplate<?, ?> kafkaTemplate) throws IOException {
			return new KafkaRecordProducerImpl(props, kafkaTemplate);
		}

		@Bean
		public KafkaRecordConsumer kafkaRecordConsumer(final KafkaTemplate<?, ?> kafkaTemplate) throws IOException {
			return new KafkaRecordConsumerImpl(kafkaRecordProducer(kafkaTemplate));
		}
	}

	interface KafkaRecordProducer {
		void produce();
	}

	interface KafkaRecordConsumer {
		@SuppressWarnings("unused") void consume(@Payload final String source);
	}

	@Slf4j
	static class KafkaRecordProducerImpl implements KafkaRecordProducer {

		private static final String PATH = "classpath:words.txt";
		private static final SecureRandom RANDOM = new SecureRandom();

		private final List<String> words;

		private final BaseProperties.Kafka props;
		private final KafkaTemplate<Object, Object> kt;

		@SuppressWarnings("unchecked")
		KafkaRecordProducerImpl(final BaseProperties.Kafka props, final KafkaTemplate<?, ?> kt) throws IOException {

			this.words = ImmutableList.copyOf(createWords());
			this.props = props; this.kt = (KafkaTemplate<Object, Object>)kt;
		}

		@Override
		public void produce() {

			final List<String> values = Lists.newArrayListWithExpectedSize(10);
			IntStream.range(0, 10).boxed().forEach(e -> values.add(words.get(RANDOM.nextInt(words.size()))));

			kt.send(props.getSendTopics(), retrieveCurrentMillis(), String.join(" ", values));
		}

		@Scheduled(fixedRate=5000L)
		private void produceForTest() {
			kt.send(props.getTopics(), retrieveCurrentMillis(), RandomGenerator.createRandomUUID());
		}

		private String retrieveCurrentMillis() {
			return String.valueOf(System.currentTimeMillis());
		}

		private List<String> createWords() throws IOException {

			final List<String> wordsList = Lists.newArrayList();

			Files.readAllLines(ResourceUtils.getFile(PATH).toPath(), StandardCharsets.UTF_8).forEach(e ->
					Splitter.on(',').omitEmptyStrings().trimResults().splitToList(e).forEach(ee ->
							IntStream.range(0, RANDOM.nextInt(10)).boxed().forEach(eee -> wordsList.add(ee))));
			IntStream.range(0, 10).boxed().forEach(e -> Collections.shuffle(wordsList));

			return wordsList;
		}
	}

	@Slf4j
	static class KafkaRecordConsumerImpl implements KafkaRecordConsumer {

		private final KafkaRecordProducer producer;

		KafkaRecordConsumerImpl(final KafkaRecordProducer producer) {
			this.producer = producer;
		}

		@Override
		@KafkaListener(id="${spring.kafka.consumer.group-id}", topics="${base.kafka.topics}")
		public void consume(@Payload final String source) {
			log.info("Payload From Kafka -> {}", source); producer.produce();
		}
	}

	@Configuration
	@Profile("kafka")
	@EnableConfigurationProperties(BaseProperties.KafkaStreams.class)
	@ConditionalOnClass(name="org.apache.kafka.streams.KafkaStreams")
	@AutoConfigureAfter(name=Constants.BASE_PACKAGE + ".config.AutoConfig.KafkaAutoConfig")
	protected static class KafkaStreamsAutoConfig {

		private final BaseProperties.KafkaStreams props;

		@Autowired
		protected KafkaStreamsAutoConfig(final BaseProperties.KafkaStreams props) {
			this.props = props;
		}

		@Bean
		public KafkaStreamsConfiguration defaultKafkaStreamsConfig() {

			final Map<String, Object> properties = ImmutableMap.<String, Object>builder()
					.put(StreamsConfig.APPLICATION_ID_CONFIG, props.getConfig().getAppId())
					.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, props.getConfig().getBootstrapServers())
					.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, props.getConfig().getReplicationFactor())
					.put(StreamsConfig.STATE_DIR_CONFIG, props.getConfig().getStateDir())
					.put(StreamsConfig.CLIENT_ID_CONFIG, props.getConfig().getClientId())
					.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName())
					.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName())
					.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, props.getConfig().getReplicasForTask())
					.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, props.getConfig().getConcurrentStreamThreads())
					.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "at_least_once")
					.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, UsePartitionTimeOnInvalidTimestamp.class.getName())
					.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class.getName())
					.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, DefaultProductionExceptionHandler.class.getName())
					.build();

			return new KafkaStreamsConfiguration(properties);
		}

		@Bean
		public StreamsBuilderFactoryBean defaultKafkaStreamsBuilder() {
			return new StreamsBuilderFactoryBean(defaultKafkaStreamsConfig());
		}

		@Bean
		public KStream<String, String> kStream(final StreamsBuilder builder) {

			final KStream<String, String> stream = builder.stream(props.getSourceTopics());

			stream.flatMapValues(v -> ImmutableList.copyOf(v.toLowerCase(Locale.getDefault()).split("\\W+")))
					.groupBy((k, v) -> v).windowedBy(TimeWindows.of(props.getWindowSize()))
					.count(Materialized.as(Stores.persistentWindowStore(props.getStateStoreName(), props.getWindowRetention(), props.getWindowSize(), false)))
					.toStream().filter((k, v) -> v > props.getBlockThresholds()).print(Printed.toSysOut());

			return stream;
		}
	}
}
