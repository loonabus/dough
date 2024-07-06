package io.doe.config;

import feign.Logger;
import io.doe.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Locale;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
@EnableFeignClients(basePackages=Constants.BASE_PACKAGE)
public class FeignConfig {

	private static final String PROP_NAME = "base.logging.feign-client-level";

	private final Logger.Level feignLoggerLevel;

	@Autowired
	protected FeignConfig(final Environment env) {
		this.feignLoggerLevel = Enum.valueOf(Logger.Level.class, env.getProperty(PROP_NAME, Logger.Level.FULL.name()).toUpperCase(Locale.getDefault()));
	}

	@Bean
	public Logger.Level feignClientLoggerLevel() {
		return feignLoggerLevel;
	}

	@Configuration
	@EnableCaching(proxyTargetClass=true)
	static class CacheConfig { /* Spring Configuration Class */ }
}
