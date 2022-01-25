package bus.loona.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisServer
 * started on 2017-05-?? ~
 */

@Slf4j
@Component
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnClass(name={"redis.embedded.RedisServer", "redis.embedded.RedisServerBuilder"})
public class EmbeddedRedisServerManager implements InitializingBean, DisposableBean {

	private static final String PROP_NAME = "spring.profiles.active";

	@Nullable private final RedisServer server;

	@Autowired
	public EmbeddedRedisServerManager(final RedisProperties props, final Environment env) {

		final boolean embedded = "".equals(env.getProperty(PROP_NAME, "").replaceAll("kafka|logger|server|,", "").trim());
		this.server = embedded ? RedisServer.builder().port(props.getPort()).build() : null ;
	}

	@Override
	public void destroy() {

		if (Objects.nonNull(server)) {
			server.stop();
		}
	}

	@Override
	public void afterPropertiesSet() {

		if (Objects.nonNull(server)) {
			server.start();
			log.info("Embedded redis server started !!");
		}
	}
}
