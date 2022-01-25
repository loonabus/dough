package bus.loona.redis;

import bus.loona.config.BaseProperties;
import bus.loona.domain.ServiceReq;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisCommandProvider
 * started on 2017-05-?? ~
 */

@Slf4j @Component
@EnableConfigurationProperties(BaseProperties.Redis.class)
public class RedisCommandProviderImpl implements RedisCommandProvider {

	private static final Duration DURATION = Duration.ofSeconds(60L);

	private final ObjectMapper mapper;
	private final BaseProperties.Redis props;

	@Autowired
	public RedisCommandProviderImpl(final ObjectMapper mapper, final BaseProperties.Redis props) {
		this.mapper = mapper; this.props = props;
	}

	@Override
	public BaseScCommand createCommand(final ServiceReq.RedisPrm prm) {

		return new BaseScCommand() {

			@Override @SuppressWarnings("unchecked")
			public Long execute(final RedisOperations operations) {

				watch(operations, props.getOperationKey());
				operations.multi();
				operations.opsForValue().setIfAbsent(props.makeTestKey(prm.getCode()), prm.getIpAddresses(), DURATION);

				return count(operations.exec());
			}

			@Override
			public Logger obtainLogger() { return log; }
		};
	}

	@Override
	public BaseRcCommand<List<ServiceReq.IpAddress>> searchCommand(final ServiceReq.RedisPrm prm) {

		return new BaseRcCommand<List<ServiceReq.IpAddress>>() {

			final JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ServiceReq.IpAddress.class);

			@Override
			public List<ServiceReq.IpAddress> doInRedis(final RedisConnection rc) {

				final byte[] res = rc.get(stringToByte(props.makeTestKey(prm.getCode())));

				return Objects.nonNull(res) ? parseResult(res) : ImmutableList.of();
			}

			private List<ServiceReq.IpAddress> parseResult(@Nullable final byte[] source) {

				if (Objects.isNull(source)) {
					return ImmutableList.of();
				}

				try {
					return mapper.readValue(source, type);
				} catch (final IOException e) {
					log.warn("ObjectMapper readValue() from byte failed -> {}", source, e);
					return ImmutableList.of();
				}
			}
		};
	}
}
