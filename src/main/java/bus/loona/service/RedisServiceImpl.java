package bus.loona.service;

import bus.loona.common.BaseException;
import bus.loona.common.RandomGenerator;
import bus.loona.config.BaseProperties;
import bus.loona.domain.ServiceReq;
import bus.loona.domain.ServiceRes;
import bus.loona.redis.RedisCommandProvider;
import bus.loona.service.base.BaseService;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisServiceImpl
 * started on 2017-05-?? ~
 */

@Slf4j @Service
@EnableConfigurationProperties(BaseProperties.Redis.class)
public class RedisServiceImpl implements RedisService, BaseService {

	private static final String WATCH_COMMAND_MESSAGE = "Redis (maybe mock server) runs on test mode -> skip command";

	private final BaseProperties.Redis props;
	private final RedisCommandProvider provider;
	private final MessageSourceAccessor accessor;
	private final RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public RedisServiceImpl(final BaseProperties.Redis props,
			final RedisCommandProvider provider, final MessageSourceAccessor accessor, final RedisTemplate<String, Object> redisTemplate) {
		this.props = props; this.provider = provider; this.accessor = accessor; this.redisTemplate = redisTemplate;
	}

	@Nullable @Override
	public <T> T search(final String k) {
		return vGet(k);
	}

	@Nullable @Override
	public <T> T search(final String k, final String hk) {
		return redisTemplate.<String, T>opsForHash().get(k, hk);
	}

	@Override
	public <T> void create(final String k, final T v) {
		redisTemplate.opsForValue().set(k, v);
	}

	@Override
	public <T> void create(final String k, final String hk, final T v) {
		redisTemplate.<String, T>opsForHash().put(k, hk, v);
	}

	@Override
	public ServiceRes.Res create(final ServiceReq.RedisPrm prm) {

		final String value = createRedisLock();

		if (!StringUtils.hasText(value)) {
			throw new BaseException(retrieveMessage("RedisLockObtainException"));
		}

		try {
			final Long created = redisTemplate.execute(provider.createCommand(prm));
			if (!Long.valueOf(1L).equals(created)) {
				throw new BaseException(retrieveMessage("RedisOperationException"));
			}

			final Optional<List<ServiceReq.IpAddress>> res = Optional.ofNullable(redisTemplate.execute(provider.searchCommand(prm)));

			return ServiceRes.Res.builder().value(res.orElseGet(ImmutableList::of)).build();
		} finally {
			removeRedisLock(value);
		}
	}

	@Override
	public MessageSourceAccessor retrieveAccessor() {
		return accessor;
	}

	@Nullable @SuppressWarnings("unchecked")
	private <T> T vGet(final String k) {
		return (T)redisTemplate.opsForValue().get(k);
	}

	private boolean unwrap(@Nullable final Boolean b) { return Objects.nonNull(b) && b; }

	@Nullable
	private String createRedisLock() {

		final String value = RandomGenerator.createRandomUUID();
		final Boolean created = redisTemplate.opsForValue().setIfAbsent(props.getOperationKey(), value,
				props.obtainOperationExpiration().getExpirationTime(), props.obtainOperationExpiration().getTimeUnit());

		if (unwrap(Optional.ofNullable(created).orElse(Boolean.FALSE))) { return value; }

		log.info(retrieveMessage("RedisLockObtainException"));

		return null;
	}

	private void removeRedisLock(final String v) {

		try {
			if (props.isRedisTestMode()) {
				log.info(WATCH_COMMAND_MESSAGE); return;
			}
			redisTemplate.unwatch();
		} catch (final RedisSystemException e) {
			log.error("Unsupported Command -> {}", e.getMessage(), e);
		}

		final String value = vGet(props.getOperationKey());

		if (Objects.nonNull(value) && Objects.equals(v, value)) {
			log.info("Lock key erased -> {}", redisTemplate.delete(props.getOperationKey())); return;
		}

		log.warn("No Operation Lock (Expired or Unmatched Value");
	}
}
