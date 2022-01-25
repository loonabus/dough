package bus.loona;

import bus.loona.config.BaseProperties;
import bus.loona.redis.RedisCommandProvider;
import bus.loona.redis.RedisCommandProviderImpl;
import bus.loona.service.RedisService;
import bus.loona.service.RedisServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fppt.jedismock.RedisServer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisServiceTest
 * started on 2017-05-?? ~
 */

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

	private static final ObjectMapper MAPPER;

	private static final RedisSerializer<String> S_SERIALIZER;
	private static final RedisSerializer<Object> O_SERIALIZER;

	static {
		MAPPER = Jackson2ObjectMapperBuilder.json().build();

		S_SERIALIZER = new StringRedisSerializer();
		O_SERIALIZER = new GenericJackson2JsonRedisSerializer(MAPPER);
	}

	private RedisService service;

	private final RedisServer server;
	private final BaseProperties.Redis props;
	private final RedisCommandProvider provider;
	private final RedisTemplate<String, Object> redisTemplate;

	@Mock private MessageSourceAccessor accessor;

	RedisServiceTest() throws IOException {

		server = RedisServer.newRedisServer(32767);
		server.start();

		props = new BaseProperties.Redis("redis:base:", "redis:base:test:", "redis:base:operation", 30L, TimeUnit.SECONDS, true);

		provider = new RedisCommandProviderImpl(new Jackson2ObjectMapperBuilder().build(), props);

		final GenericObjectPoolConfig<?> pc = new GenericObjectPoolConfig<>();
		pc.setMaxTotal(5); pc.setMaxIdle(5); pc.setMinIdle(5); pc.setMaxWait(Duration.ofMillis(1000L));

		final LettuceClientConfiguration cc = LettucePoolingClientConfiguration.builder()
				.poolConfig(pc).commandTimeout(Duration.ofMillis(1500L))
				.shutdownTimeout(Duration.ofMillis(3000L))
				.clientOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.RESP2).build())
				.clientResources(DefaultClientResources.create()).build();

		RedisStandaloneConfiguration sc = new RedisStandaloneConfiguration();
		sc.setHostName("127.0.0.1"); sc.setPort(32767); sc.setDatabase(0); sc.setPassword(RedisPassword.of((String)null));

		LettuceConnectionFactory rc = new LettuceConnectionFactory(sc, cc);
		rc.setConvertPipelineAndTxResults(true);
		rc.afterPropertiesSet();

		redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(rc);
		redisTemplate.setKeySerializer(S_SERIALIZER); redisTemplate.setValueSerializer(O_SERIALIZER);
		redisTemplate.setHashKeySerializer(S_SERIALIZER); redisTemplate.setHashValueSerializer(O_SERIALIZER);
		redisTemplate.afterPropertiesSet();
	}

	@BeforeEach
	void createRedisService() {
		service = new RedisServiceImpl(props, provider, accessor, redisTemplate);
	}

	@AfterEach
	void closeRedisMockServer() { server.stop(); }

	@Test
	void createAndRetrieveTest() {

		final String k = props.makeTestKey("k");
		final String v = String.valueOf(System.currentTimeMillis());
		service.create(k, v);

		assertThat(service.<String>search(k)).isEqualTo(v);

		final String kk = props.makeTestKey("kk");
		final String hk = props.makeTestKey("hk");
		final String vv = String.valueOf(System.currentTimeMillis());
		service.create(kk, hk, vv);

		assertThat(service.<String>search(kk, hk)).isEqualTo(vv);
	}
}
