package bus.loona.redis;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see SessionCallback
 * started on 2017-05-?? ~
 */

public interface BaseScCommand extends SessionCallback<Long> {

	default boolean runOnTestEnv() {
		return false;
	}

	default Long count(final List<Object> source) {

		final String FORMAT = "Commands Res -> {}";

		obtainLogger().info(FORMAT, source);

		final Predicate<Object> predicate = o -> {

			if (Objects.isNull(o)) { return false; }
			if (o instanceof String) { return Objects.equals("OK", o); }
			if (o instanceof Boolean) { return (Boolean)o; }
			if (o instanceof Number) { return ((Number)o).intValue() > 0; }

			return !(o instanceof Throwable);
		};

		return source.stream().filter(predicate).count();
	}

	default <V> void watch(final RedisOperations<String, V> operations, final String... k) {

		if (runOnTestEnv()) {
			obtainLogger().info("Redis (maybe mock server) runs on test mode -> skip watch command");
			return;
		}

		try {
			operations.watch(ImmutableList.copyOf(k));
		} catch (final RedisSystemException e) {
			obtainLogger().error("Unsupported Command -> {}", e.getMessage(), e);
		}
	}

	Logger obtainLogger();
}