package io.doe.config;

import jakarta.annotation.Nullable;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.Locale;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
@AutoConfigureAfter(MessageSourceAutoConfiguration.class)
public class MessageAccessorConfig {

	@Bean
	@Primary
	public MessageSourceAccessor messageSourceAccessor(final MessageSource source) {
		return new BaseMessageAccessor(source);
	}

	protected static class BaseMessageAccessor extends MessageSourceAccessor {

		private static final String DEFAULT_ERROR_MESSAGE = "Exception Occurred";

		BaseMessageAccessor(final MessageSource source) {
			super(source);
		}

		@Override
		public String getMessage(final String code) {
			return super.getMessage(code, DEFAULT_ERROR_MESSAGE);
		}

		@Override
		public String getMessage(final String code, final Locale locale) {
			return super.getMessage(code, DEFAULT_ERROR_MESSAGE, locale);
		}

		@Override
		public String getMessage(final String code, @Nullable final Object[] args) {
			return super.getMessage(code, args, DEFAULT_ERROR_MESSAGE);
		}

		@Override
		public String getMessage(final String code, @Nullable final Object[] args, final Locale locale) {
			return super.getMessage(code, args, DEFAULT_ERROR_MESSAGE, locale);
		}
	}
}
