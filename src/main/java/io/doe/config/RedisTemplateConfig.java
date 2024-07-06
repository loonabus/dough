package io.doe.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ClassUtils;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
@ConditionalOnClass(RedisOperations.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisTemplateConfig {

	@Bean
	@Primary
	public RedisTemplate<String, Object> redisJsonTemplate(
			final Jackson2ObjectMapperBuilder builder, final RedisConnectionFactory f) {

		final RedisSerializer<String> ss = new StringRedisSerializer();
		final RedisSerializer<Object> os = new GenericJackson2JsonRedisSerializer(builder.build());
		final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

		redisTemplate.setConnectionFactory(f);
		redisTemplate.setKeySerializer(ss);
		redisTemplate.setValueSerializer(os);
		redisTemplate.setHashKeySerializer(ss);
		redisTemplate.setHashValueSerializer(os);

		changeConvertTxResults(f);

		return redisTemplate;
	}

	private void changeConvertTxResults(final RedisConnectionFactory f) {

		final ClassLoader classLoader = RedisTemplate.class.getClassLoader();

		if (ClassUtils.isPresent("io.lettuce.core.RedisClient", classLoader)) {
			((LettuceConnectionFactory)f).setConvertPipelineAndTxResults(true);
			return;
		}
		if (ClassUtils.isPresent("redis.clients.jedis.Jedis", classLoader)) {
			((JedisConnectionFactory)f).setConvertPipelineAndTxResults(true);
		}
	}
}
