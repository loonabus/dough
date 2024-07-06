package io.doe.config;

import jakarta.annotation.Nullable;
import jakarta.servlet.ServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.io.*;
import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(prefix="spring.aop", name="auto", havingValue="true", matchIfMissing=true)
public class LoggingConfig {

	protected static class Pointcuts {

		@Pointcut("within(io.doe..*)")
		void w() { /* Pointcut Designator Method */ }

		@Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
		void c() { /* Pointcut Designator Method */ }

		@Pointcut("execution(public * io.doe..*Controller.*(..))")
		void e() { /* Pointcut Designator Method */ }

		@Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || "
				+ "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
				+ "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
				+ "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
		void m() { /* Pointcut Designator Method */ }
	}

	@Slf4j
	protected static class ParameterExtractor {

		private static final String ERROR_FORMAT = "Param Extraction From Method Failure ({} Exception) -> {}";

		public Map<String, Object> extract(final JoinPoint point) {

			try {
				final String[] ns = ((MethodSignature)point.getSignature()).getParameterNames();
				final Object[] vs = Optional.ofNullable(point.getArgs()).orElseGet(() -> new Object[]{});

				return IntStream.range(0, vs.length).boxed().collect(Collectors.toMap(n -> ns[n], n -> convertIfMatched(vs[n])));
			} catch (final RuntimeException re) {
				log.warn(ERROR_FORMAT, "Runtime", point.getSignature().getName(), re);
			} catch (final Exception ee) {
				log.warn(ERROR_FORMAT, "Checked", point.getSignature().getName(), ee);
			}

			return Map.of();
		}

		private boolean skipIfMatched(final Object source) {

			return
					source instanceof Model || source instanceof ModelMap ||
					source instanceof File || source instanceof MultipartFile ||
					source instanceof Errors || source instanceof Principal ||
					source instanceof Locale || source instanceof HttpMethod ||
					source instanceof Reader || source instanceof Writer ||
					source instanceof InputStream || source instanceof OutputStream ||
					source instanceof SessionStatus || source instanceof WebRequest ||
					source instanceof ServletRequest || source instanceof MultipartRequest
					;
		}

		private Object convertIfMatched(final @Nullable Object source) {
			return Optional.ofNullable(source).filter(s -> !skipIfMatched(s)).orElseGet(Optional::empty);
		}
	}

	@Slf4j
	@Aspect
	@Order(101)
	public static class HandlerArgumentsLoggingAspect {

		private final ParameterExtractor extractor;

		protected HandlerArgumentsLoggingAspect(final ParameterExtractor extractor) {
			this.extractor = extractor;
		}

		@Before("io.doe.config.LoggingConfig.Pointcuts.w() && " +
				"io.doe.config.LoggingConfig.Pointcuts.c() && " +
				"io.doe.config.LoggingConfig.Pointcuts.e() && " +
				"io.doe.config.LoggingConfig.Pointcuts.m() && " + "args(..)")
		public void paramLoggingBefore(final JoinPoint point) {
			log.debug("{}.{} -> {}", point.getTarget().getClass().getSimpleName(), point.getSignature().getName(), extractor.extract(point));
		}
	}

	@Bean
	public HandlerArgumentsLoggingAspect paramLoggingAspect() {
		return new HandlerArgumentsLoggingAspect(new ParameterExtractor());
	}
}
