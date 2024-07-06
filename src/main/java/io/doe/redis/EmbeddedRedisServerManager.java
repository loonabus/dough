package io.doe.redis;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisServer
 * started on 2017-05-?? ~
 */

@Slf4j
@Component
@ConditionalOnClass(RedisServer.class)
@EnableConfigurationProperties(RedisProperties.class)
public class EmbeddedRedisServerManager implements InitializingBean, DisposableBean {

	private static final String PROP_NAME = "spring.profiles.active";

	@Nullable private final RedisServer server;

	@Autowired
	public EmbeddedRedisServerManager(final RedisProperties props, final Environment env) throws IOException {
		this.server = !StringUtils.hasText(env.getProperty(PROP_NAME, "").replaceAll("logger|server|,", "")) ?
				RedisServer.newRedisServer().port(props.getPort()).build() : null;
	}

	@Override
	public void destroy() {

		if (Objects.nonNull(server)) {
			try {
				server.stop();
			} catch (final IOException e) {
				log.info("Failed to stop Embedded redis server !!", e);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws IOException {

		if (Objects.nonNull(server)) {
			server.start();
			log.info("Embedded redis server started !!"); return;
		}

		log.debug("Embedded redis server not started because profile is not embedded mode");
	}
}
