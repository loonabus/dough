package bus.loona.service;

import bus.loona.domain.ServiceReq;
import bus.loona.domain.ServiceRes;
import org.springframework.lang.Nullable;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisService
 * started on 2017-05-?? ~
 */

public interface RedisService {

	@Nullable <T> T search(final String k);
	@Nullable <T> T search(final String k, final String hk);

	<T> void create(final String k, final T v);
	<T> void create(final String k, final String hk, final T v);

	ServiceRes.Res create(final ServiceReq.RedisPrm prm);
}
