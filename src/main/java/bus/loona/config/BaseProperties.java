package bus.loona.config;

import bus.loona.common.BaseException;
import bus.loona.common.Constants;
import com.amazonaws.regions.Regions;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

public final class BaseProperties {

	private static final String ACCESS_KEY_EXPR = "[A-Z0-9]{20}";
	private static final String SECRET_KEY_EXPR = "[A-Za-z0-9]{40}";

	private static final String ABORT = "ABORT";
	private static final String CALLER_RUNS = "CALLER_RUNS";
	private static final String DISCARD = "DISCARD";
	private static final String DISCARD_OLDEST = "DISCARD_OLDEST";
	private static final String REJECT_RULES_EXPR = ABORT + "|" + CALLER_RUNS + "|" + DISCARD + "|" + DISCARD_OLDEST;

	private static RejectedExecutionHandler chooseRejectedExecutionHandler(final String rule) {

		switch (rule) {
			case ABORT: return new ThreadPoolExecutor.AbortPolicy();
			case DISCARD: return new ThreadPoolExecutor.DiscardPolicy();
			case DISCARD_OLDEST: return new ThreadPoolExecutor.DiscardOldestPolicy();
			case CALLER_RUNS:
			default: return new ThreadPoolExecutor.CallerRunsPolicy();
		}
	}

	private static void checkValuesIfModeMatched(final String mode, @Nullable final String access,
												 @Nullable final String secret, final String message) {

		final String STATIC_CREDENTIALS_MODE = "S";
		final String EX_MESSAGE_FORMAT = "%s Is Essential For Static Credential Provider";

		if (Objects.equals(STATIC_CREDENTIALS_MODE, mode)) {
			if (!StringUtils.hasText(access) || !access.matches(ACCESS_KEY_EXPR)) {
				throw new BaseException(String.format(EX_MESSAGE_FORMAT, message + "-access"));
			}
			if (!StringUtils.hasText(secret) || !secret.matches(SECRET_KEY_EXPR)) {
				throw new BaseException(String.format(EX_MESSAGE_FORMAT, message + "-secret"));
			}
		}
	}

	private BaseProperties() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}

	interface AwsAccess {

		String getMode();
		@Nullable String getAccess();
		@Nullable String getSecret();
		Regions getRegion();
	}

	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.aws-ses")
	public static class AwsSes implements AwsAccess {

		@NotNull @Pattern(regexp="[IS]") private final String mode;
		@Nullable private final String access;
		@Nullable private final String secret;
		@NotNull private final Regions region;
		@NotNull @Email private final String sender;

		public AwsSes(final String mode, @Nullable final String access,
					  @Nullable final String secret, final Regions region, final String sender) {

			this.mode = mode;
			this.access = access;
			this.secret = secret;
			this.region = region;
			this.sender = sender;

			checkValuesIfModeMatched(mode, access, secret, "ses");
		}
	}

	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.aws-sts")
	public static class AwsSts implements AwsAccess {

		@NotNull @Pattern(regexp="[IS]") private final String mode;
		@Nullable private final String access;
		@Nullable private final String secret;
		@NotNull private final Regions region;

		public AwsSts(final String mode, @Nullable final String access,
					  @Nullable final String secret, final Regions region) {

			this.mode = mode;
			this.access = access;
			this.secret = secret;
			this.region = region;

			checkValuesIfModeMatched(mode, access, secret, "sts");
		}
	}

	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.async")
	public static class Async {

		private static final String ERROR_MESSAGE = "pool-max-size Must Be Equal Or Greater Than pool-min-size";

		@NotBlank private final String namePrefix;
		@NotNull @Range(min=1, max=10) private final Integer poolMinSize;
		@NotNull @Range(min=1, max=10) private final Integer poolMaxSize;
		@NotNull @Range(min=100, max=10000) private final Integer poolQueueSize;
		@NotNull @Pattern(regexp=REJECT_RULES_EXPR) private final String rejectRule;
		@NotNull private final Duration keepAlive;

		private final RejectedExecutionHandler rejectedExecutionHandler;

		public Async(final String namePrefix, final Integer poolMinSize, final Integer poolMaxSize,
					 final Integer poolQueueSize, final String rejectRule, final Duration keepAlive) {

			if (poolMinSize > poolMaxSize) { throw new BaseException(ERROR_MESSAGE); }

			this.namePrefix = namePrefix;
			this.poolMinSize = poolMinSize;
			this.poolMaxSize = poolMaxSize;
			this.poolQueueSize = poolQueueSize;
			this.rejectRule = rejectRule;
			this.keepAlive = keepAlive;

			this.rejectedExecutionHandler = chooseRejectedExecutionHandler(rejectRule);
		}
	}

	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.jackson")
	public static class Jackson {

		@NotBlank private final String dateFormat;
		@NotNull private final ZoneId fixedZoneId;

		public Jackson(final String dateFormat, final ZoneId fixedZoneId) {
			this.dateFormat = dateFormat;
			this.fixedZoneId = fixedZoneId;
		}
	}

	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.redis")
	public static class Redis {

		private static final String COLON = ":";
		public static final String ASTERISK = "*";
		private static final String EXPR = "[A-Za-z0-9_:-]+";
		private static final String EXPR_WORD = "^" + EXPR + "$";
		private static final String EXPR_PREFIX = "^" + EXPR + ":$";

		@NotNull @Pattern(regexp=EXPR_PREFIX) private final String basePrefix;
		@NotNull @Pattern(regexp=EXPR_PREFIX) private final String testPrefix;
		@NotNull @Pattern(regexp=EXPR_WORD) private final String operationKey;
		@NotNull @Range(min=1, max=600) private final Long operationExpiredAfter;
		@NotNull private final TimeUnit operationTimeUnit;

		private final boolean redisTestMode;

		public Redis(final String basePrefix, final String testPrefix, final String operationKey,
					 final Long operationExpiredAfter, final TimeUnit operationTimeUnit, final boolean redisTestMode) {

			this.basePrefix = basePrefix;
			this.testPrefix = testPrefix;
			this.operationKey = operationKey;
			this.operationExpiredAfter = operationExpiredAfter;
			this.operationTimeUnit = operationTimeUnit;
			this.redisTestMode = redisTestMode;
		}

		public String makeTestKey(final String source) { return testPrefix + source; }

		public Expiration obtainOperationExpiration() {
			return Expiration.from(operationExpiredAfter, operationTimeUnit);
		}
	}

	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.redis-config")
	public static class RedisConfig {

		@NotNull private final Boolean enabled;
		@NotNull @Pattern(regexp="[EKAeglshxz$]+") private final String options;
		@NotBlank private final String baseMessage;
		@NotNull @Pattern(regexp="__key(event|space)@[0-9]{1,2}__:.+") private final String basePattern;

		public RedisConfig(final Boolean enabled,
						   final String options, final String baseMessage, final String basePattern) {

			this.enabled = enabled;
			this.options = options;
			this.baseMessage = baseMessage;
			this.basePattern = basePattern;
		}
	}

	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.scheduled")
	public static class Scheduler {

		@NotBlank private final String namePrefix;
		@NotNull @Range(min=1, max=10) private final Integer poolMinSize;
		@NotNull @Pattern(regexp=REJECT_RULES_EXPR) private final String rejectRule;

		private final RejectedExecutionHandler rejectedExecutionHandler;

		public Scheduler(final String namePrefix, final Integer poolMinSize, final String rejectRule) {
			this.namePrefix = namePrefix;
			this.poolMinSize = poolMinSize;
			this.rejectRule = rejectRule;
			this.rejectedExecutionHandler = chooseRejectedExecutionHandler(rejectRule);
		}
	}

	@Profile("kafka")
	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.kafka")
	public static class Kafka {

		@NotBlank private final String bootstrapServers;
		@NotBlank private final String namePrefix;
		@NotBlank private final String topics;
		@NotBlank private final String sendTopics;

		public Kafka(final String bootstrapServers,
					 final String namePrefix, final String topics, final String sendTopics) {

			this.bootstrapServers = bootstrapServers;
			this.namePrefix = namePrefix;
			this.topics = topics;
			this.sendTopics = sendTopics;
		}
	}

	@Profile("kafka")
	@Getter @Validated @ConstructorBinding
	@ConfigurationProperties(prefix="base.kafka-streams")
	public static class KafkaStreams {

		@NotBlank private final String sourceTopics;
		@NotNull @Range(min=1, max=1000000) private final Integer blockThresholds;
		@NotNull private final Duration windowSize;
		@NotNull private final Duration windowRetention;
		@NotBlank private final String stateStoreName;

		@NotNull private final Config config;

		public KafkaStreams(final String sourceTopics, final Integer blockThresholds, final Duration windowSize,
							final Duration windowRetention, final String stateStoreName, final Config config) {

			this.sourceTopics = sourceTopics;
			this.blockThresholds = blockThresholds;
			this.windowSize = windowSize;
			this.windowRetention = windowRetention;
			this.stateStoreName = stateStoreName;
			this.config = config;
		}

		@Getter @Validated
		public static class Config {

			@NotBlank private final String appId;
			@NotBlank private final String bootstrapServers;
			@NotNull @Range(min=1, max=3) private final Integer replicationFactor;
			@NotBlank private final String stateDir;
			@NotBlank private final String clientId;
			@NotNull @Range(min=0, max=5) private final Integer replicasForTask;
			@NotNull @Range(min=1, max=50) private final Integer concurrentStreamThreads;

			public Config(final String appId, final String bootstrapServers,
						  final Integer replicationFactor, final String stateDir, final String clientId,
						  final Integer replicasForTask, final Integer concurrentStreamThreads) {

				this.appId = appId;
				this.bootstrapServers = bootstrapServers;
				this.replicationFactor = replicationFactor;
				this.stateDir = stateDir;
				this.clientId = clientId;
				this.replicasForTask = replicasForTask;
				this.concurrentStreamThreads = concurrentStreamThreads;
			}
		}
	}
}
