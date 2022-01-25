package bus.loona.redis;

import org.springframework.data.redis.core.RedisCallback;

import java.nio.charset.StandardCharsets;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisCallback
 * started on 2017-05-?? ~
 */

public interface BaseRcCommand<T> extends RedisCallback<T> {
	default byte[] stringToByte(final String source) {
		return source.getBytes(StandardCharsets.UTF_8);
	}
}