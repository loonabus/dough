package bus.loona.config;

import bus.loona.common.Constants;
import bus.loona.view.GlobalErrorHtmlView;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import com.nhncorp.lucy.security.xss.XssPreventer;
import feign.Logger;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.InitBinderDataBinderFactory;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
public class BaseConfig {

	@Configuration
	@EnableCaching(proxyTargetClass=true)
	protected static class CacheConfig { /* Spring Configuration Class */ }

	@Configuration
	@MapperScan(basePackages=Constants.BASE_PACKAGE, annotationClass=Mapper.class)
	protected static class MapperScannerConfig { /* Spring Configuration Class */ }

	@Slf4j
	@Configuration
	@EnableAsync(annotation=Async.class)
	@EnableConfigurationProperties(BaseProperties.Async.class)
	protected static class AsyncExecutorConfig implements AsyncConfigurer {

		private final BaseProperties.Async props;

		@Autowired
		protected AsyncExecutorConfig(final BaseProperties.Async props) {
			this.props = props;
		}

		@Bean(destroyMethod="destroy")
		public ThreadPoolTaskExecutor executor() {

			final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

			executor.setThreadNamePrefix(props.getNamePrefix());
			executor.setCorePoolSize(props.getPoolMinSize());
			executor.setMaxPoolSize(props.getPoolMaxSize());
			executor.setQueueCapacity(props.getPoolQueueSize());
			executor.setRejectedExecutionHandler(props.getRejectedExecutionHandler());
			executor.setAllowCoreThreadTimeOut(false);
			executor.setKeepAliveSeconds((int)props.getKeepAlive().getSeconds());
			executor.setAwaitTerminationMillis(60000L);
			executor.setWaitForTasksToCompleteOnShutdown(true);
			executor.initialize();

			return executor;
		}

		@Override
		public Executor getAsyncExecutor() {
			return executor();
		}

		@Override
		public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
			final String message = "Exception From @Async '{}' -> {}";
			return (e, m, p) -> log.warn(message, m.getName(), Arrays.stream(p).map(Object::toString).collect(Collectors.joining(",")));
		}
	}

	@Slf4j
	@Configuration
	@EnableScheduling
	@EnableConfigurationProperties(BaseProperties.Scheduler.class)
	protected static class TaskSchedulerConfig implements SchedulingConfigurer {

		private final BaseProperties.Scheduler props;

		@Autowired
		protected TaskSchedulerConfig(final BaseProperties.Scheduler props) {
			this.props = props;
		}

		@Bean(destroyMethod="destroy")
		public ThreadPoolTaskScheduler scheduler() {

			final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

			scheduler.setThreadNamePrefix(props.getNamePrefix());
			scheduler.setPoolSize(props.getPoolMinSize());
			scheduler.setErrorHandler(e -> log.warn("Exception From @Scheduler -> {}", e.getMessage(), e.getCause()));
			scheduler.setRejectedExecutionHandler(props.getRejectedExecutionHandler());
			scheduler.setAwaitTerminationMillis(60000L);
			scheduler.setWaitForTasksToCompleteOnShutdown(true);

			return scheduler;
		}

		@Override
		public void configureTasks(final ScheduledTaskRegistrar registrar) {
			registrar.setScheduler(scheduler());
		}
	}

	static class CaseConverter {

		String convert(final String source) {
			return !source.contains("_") ? source : CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, source);
		}
	}

	@Configuration
	protected static class FilterRegistererConfig {

		@Bean
		public FilterRegistrationBean<RemoteAddressFilter> remoteAddressFilterRegisterer() {

			final FilterRegistrationBean<RemoteAddressFilter> bean  =  new FilterRegistrationBean<>();
			bean.setFilter(new RemoteAddressFilter()); bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);

			return bean;
		}

		@Bean
		@ConditionalOnProperty(prefix="base", name="param-filter-enabled", havingValue="true")
		public FilterRegistrationBean<ParameterNameFilter> parameterNameFilterRegisterer() {

			final FilterRegistrationBean<ParameterNameFilter> bean  =  new FilterRegistrationBean<>();
			bean.setFilter(new ParameterNameFilter()); bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 11);

			return bean;
		}

		@Bean
		public FilterRegistrationBean<XssEscapeServletFilter> xssProtectionFilterRegisterer() {

			final FilterRegistrationBean<XssEscapeServletFilter> bean  =  new FilterRegistrationBean<>();
			bean.setFilter(new XssEscapeServletFilter()); bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 12);

			return bean;
		}
	}

	static class RemoteAddressFilter implements Filter {

		@Override
		public void init(final FilterConfig filterConfig) { /* No Operation Here */ }

		@Override
		public void doFilter(final ServletRequest request, final ServletResponse response,
							 final FilterChain chain) throws IOException, ServletException {

			chain.doFilter((request instanceof HttpServletRequest ?
					new RemoteAddressWrapper((HttpServletRequest)request) : request), response);
		}

		@Override public void destroy() { /* No Operation Here */ }
	}

	static class RemoteAddressWrapper extends HttpServletRequestWrapper {

		private static final List<String> CANDIDATES;

		static {
			CANDIDATES = ImmutableList.<String>builder().add("X-Forwarded-For","Proxy-Client-IP")
					.add("WL-Proxy-Client-IP","HTTP_CLIENT_IP","HTTP_X_FORWARDED_FOR","X-Real-IP").build();
		}

		RemoteAddressWrapper(final HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getRemoteHost() {

			try {
				return InetAddress.getByName(getRemoteAddr()).getHostName();
			} catch (final UnknownHostException e) {
				return getRemoteAddr();
			}
		}

		@Override
		public String getRemoteAddr() {
			return refine(CANDIDATES.stream().sequential().map(super::getHeader)
					.filter(StringUtils::hasText).map(String::trim)
					.filter(s -> !Objects.equals("unknown", s)).findFirst().orElseGet(super::getRemoteAddr));
		}

		private String refine(final String source) {
			return !source.contains(",") ? source.trim() : source.split(",")[0].trim();
		}
	}

	static class ParameterNameFilter implements Filter {

		private static final CaseConverter CONVERTER = new CaseConverter();

		@Override
		public void init(final FilterConfig filterConfig) { /* No Operation Here */ }

		@Override
		public void doFilter(final ServletRequest request, final ServletResponse response,
							 final FilterChain chain) throws IOException, ServletException {
			chain.doFilter(request instanceof HttpServletRequest ? new ParameterNameWrapper((HttpServletRequest)request, CONVERTER) : request, response);
		}

		@Override public void destroy() { /* No Operation Here */ }
	}

	static class ParameterNameWrapper extends HttpServletRequestWrapper {

		private final Map<String, String[]> converted;

		ParameterNameWrapper(final HttpServletRequest request, final CaseConverter converter) {
			super(request); converted = convertParameters(request.getParameterMap(), converter);
		}

		@Nullable @Override
		public String getParameter(final String name) {
			return Optional.ofNullable(converted.get(name)).map(v -> v[0]).orElse(null);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return converted;
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return Collections.enumeration(converted.keySet());
		}

		@Nullable @Override
		public String[] getParameterValues(final String name) {
			return converted.get(name);
		}

		private Map<String, String[]> convertParameters(final Map<String, String[]> source, final CaseConverter converter) {
			return source.entrySet().stream().collect(Collectors.toMap(e -> converter.convert(e.getKey()), Map.Entry::getValue));
		}
	}

	@Configuration
	@ConditionalOnProperty(prefix="base", name="swagger-enabled", havingValue="true")
	protected static class SwaggerConfig {

		@Bean
		public Docket docket() {
			return new Docket(DocumentationType.SWAGGER_2)
					.produces(ImmutableSet.of(MediaType.APPLICATION_JSON_VALUE))
					.select().paths(PathSelectors.any())
					.apis(RequestHandlerSelectors.basePackage(Constants.BASE_PACKAGE + ".web")).build();
		}
	}

	@Configuration
	protected static class WebMvcConfigurerProvider {

		@Bean
		public WebMvcConfigurer converterConfigurer() {

			return new WebMvcConfigurer() {
				@Override
				public void addFormatters(final FormatterRegistry registerer) {
					registerer.addConverter(String.class, String.class, String::trim);
				}
			};
		}
	}

	@Configuration
	protected static class WebMvcRegistrationsProvider {

		@Bean
		@ConditionalOnProperty(prefix="base", name="param-filter-enabled", havingValue="true")
		public WebMvcRegistrations requestMappingHandlerAdapterProvider() {

			return new WebMvcRegistrations() {
				@Override
				public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
					return new BaseRequestMappingHandlerAdapter();
				}
			};
		}
	}

	@SuppressWarnings("squid:MaximumInheritanceDepth")
	static class BaseRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

		@Override
		protected InitBinderDataBinderFactory createDataBinderFactory(final List<InvocableHandlerMethod> bm) {

			return new ServletRequestDataBinderFactory(bm, super.getWebBindingInitializer()) {
				@Override
				protected ServletRequestDataBinder createBinderInstance(
						@Nullable final Object o, final String n, final NativeWebRequest wr) {
					return new CamelCaseRequestDataBinder(o, n);
				}
			};
		}
	}

	@Slf4j
	static class CamelCaseRequestDataBinder extends ExtendedServletRequestDataBinder {

		private static final CaseConverter CONVERTER = new CaseConverter();

		CamelCaseRequestDataBinder(@Nullable final Object target, final String objectName) {
			super(target, objectName);
		}

		@Override
		protected void bindMultipart(final Map<String, List<MultipartFile>> mp, final MutablePropertyValues mpvs) {

			mp.forEach((k, v) -> {
				final String converted = CONVERTER.convert(k);

				if (v.size() == 1) {
					final MultipartFile value = v.get(0);
					if (isBindEmptyMultipartFiles() || !value.isEmpty()) {
						mpvs.add(converted, value);
					}
				} else {
					mpvs.add(converted, v);
				}
			});
		}

		@Override
		protected void addBindValues(final MutablePropertyValues mpvs, final ServletRequest request) {

			@SuppressWarnings("unchecked")
			final Map<String, String> pathVariables =
					(Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

			if (Objects.nonNull(pathVariables)) {
				pathVariables.forEach((k, v) -> {
					if (mpvs.contains(k)) {
						log.warn("PathVariable '{}' Found In RequestParameters", k);
						log.warn("Spring's Default Is 'Skip Overwriting' But We Overwrite !!");
					}
					mpvs.addPropertyValue(k, v);
				});
			}
		}
	}

	@Configuration
	@ConditionalOnClass(name="org.springframework.web.servlet.View")
	protected static class GlobalErrorHtmlViewConfig {

		@Bean(name="error")
		@ConditionalOnMissingBean(name="error")
		public View defaultErrorView() {
			return new GlobalErrorHtmlView();
		}
	}

	@Configuration
	@ConditionalOnClass(name="com.fasterxml.jackson.databind.ObjectMapper")
	protected static class JacksonCustomizerConfig {

		@Bean
		public Jackson2ObjectMapperBuilderCustomizer objectMapperBuilderCustomizer() {
			return customizer -> customizer.findModulesViaServiceLoader(true);
		}
	}

	@Configuration
	@ConditionalOnClass(name={
			"org.springframework.http.converter.json.Jackson2ObjectMapperBuilder",
			"org.springframework.web.servlet.view.json.MappingJackson2JsonView"} )
	public static class JsonViewConfig {

		@Bean
		@ConditionalOnMissingBean
		public MappingJackson2JsonView mappingJacksonView(final Jackson2ObjectMapperBuilder builder) {

			final MappingJackson2JsonView mappingJacksonView = new MappingJackson2JsonView(builder.build());
			mappingJacksonView.setExtractValueFromSingleKeyModel(true);

			return mappingJacksonView;
		}
	}

	@Configuration
	@ConditionalOnClass(name={
			"org.springframework.web.accept.ContentNegotiationManager",
			"org.springframework.web.servlet.view.json.MappingJackson2JsonView",
			"org.springframework.web.servlet.view.ContentNegotiatingViewResolver"})
	protected static class ContentNegotiatingViewResolverConfig {

		@Bean
		public ContentNegotiatingViewResolver viewResolver(
				final ContentNegotiationManager manager, final MappingJackson2JsonView mappingJacksonView) {

			final ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();

			resolver.setDefaultViews(ImmutableList.of(mappingJacksonView));
			resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
			resolver.setContentNegotiationManager(manager);

			return resolver;
		}
	}

	@Configuration
	@EnableConfigurationProperties(BaseProperties.Jackson.class)
	@ConditionalOnClass(name={"java.time.LocalDateTime",
			"java.time.ZonedDateTime", "com.fasterxml.jackson.databind.module.SimpleModule"})
	public static class JacksonModuleProvider {

		private final String format;
		private final ZoneId zoneId;
		private final DateTimeFormatter formatter;

		@Autowired
		public JacksonModuleProvider(final BaseProperties.Jackson props) {

			this.format = props.getDateFormat();
			this.zoneId = props.getFixedZoneId();
			this.formatter = DateTimeFormatter.ofPattern(format);
		}

		@Bean
		public SimpleModule customJsr310DateTimeModule() {

			return new SimpleModule("CustomJsr310DateTimeModule", Version.unknownVersion(), ImmutableMap.of(
					LocalDateTime.class, new LocalDateTimeJsonDeserializer(format, formatter),
					ZonedDateTime.class, new ZonedDateTimeJsonDeserializer(format, formatter.withZone(zoneId))),
					ImmutableList.of(new LocalDateTimeJsonSerializer(formatter), new ZonedDateTimeJsonSerializer(formatter)));
		}

		@Bean
		public SimpleModule xssProtectionModule() {
			return new SimpleModule("XssProtectionModule",
					Version.unknownVersion(), ImmutableMap.of(String.class, new XssProtectionJsonDeserializer()));
		}
	}

	static class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {

		private final DateTimeFormatter formatter;

		LocalDateTimeJsonSerializer(final DateTimeFormatter formatter) {
			super(); this.formatter = formatter;
		}

		@Override
		public Class<LocalDateTime> handledType() {
			return LocalDateTime.class;
		}

		@Override
		public void serialize(final LocalDateTime s, final JsonGenerator n, final SerializerProvider p) throws IOException {
			n.writeString(s.format(formatter));
		}
	}

	@Slf4j
	static class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {

		private final String format;
		private final DateTimeFormatter formatter;

		LocalDateTimeJsonDeserializer(final String format, final DateTimeFormatter formatter) {
			super(); this.format = format; this.formatter = formatter;
		}

		@Override
		public Class<LocalDateTime> handledType() {
			return LocalDateTime.class;
		}

		@Nullable @Override
		public LocalDateTime deserialize(final JsonParser p, final DeserializationContext c) throws IOException {

			final String source = p.getText();

			try {
				return LocalDateTime.parse(source, formatter);
			} catch (final NullPointerException e) {
				log.debug(e.getMessage());
			} catch (final DateTimeParseException e) {
				log.info("Invalid DateFormat '{}' (Expected '{}')", source, format);
			}

			return null;
		}
	}

	static class ZonedDateTimeJsonSerializer extends JsonSerializer<ZonedDateTime> {

		private final DateTimeFormatter formatter;

		ZonedDateTimeJsonSerializer(final DateTimeFormatter formatter) {
			this.formatter = formatter;
		}

		@Override
		public Class<ZonedDateTime> handledType() {
			return ZonedDateTime.class;
		}

		@Override
		public void serialize(final ZonedDateTime s, final JsonGenerator n, final SerializerProvider p) throws IOException {
			n.writeString(s.format(formatter));
		}
	}

	@Slf4j
	static class ZonedDateTimeJsonDeserializer extends JsonDeserializer<ZonedDateTime> {

		private final String format;
		private final DateTimeFormatter formatter;

		ZonedDateTimeJsonDeserializer(final String format, final DateTimeFormatter formatter) {
			this.format = format; this.formatter = formatter;
		}

		@Override
		public Class<ZonedDateTime> handledType() {
			return ZonedDateTime.class;
		}

		@Nullable @Override
		public ZonedDateTime deserialize(final JsonParser p, final DeserializationContext c) throws IOException {

			final String source = p.getText();

			try {
				return ZonedDateTime.parse(source, formatter);
			} catch (final NullPointerException e) {
				log.debug(e.getMessage());
			} catch (final DateTimeParseException e) {
				log.info("Invalid DateFormat '{}' (Expected '{}')", source, format);
			}

			return null;
		}
	}

	static class XssProtectionJsonDeserializer extends StringDeserializer implements ContextualDeserializer {

		private static final long serialVersionUID = 1L;

		@Override
		public JsonDeserializer<String> createContextual(final DeserializationContext c, final BeanProperty bp) {
			return this;
		}

		@Nullable @Override
		public String deserialize(final JsonParser p, final DeserializationContext c) throws IOException {
			return XssPreventer.escape(super.deserialize(p, c));
		}
	}

	@Configuration
	@ConditionalOnClass(name="org.aspectj.lang.annotation.Aspect")
	@ConditionalOnProperty(prefix="spring.aop", name="auto", havingValue="true", matchIfMissing=true)
	protected static class LoggingAspectConfig {

		@Bean
		@ConditionalOnProperty(prefix="base.logging", name="param-logging-enabled", havingValue="true")
		public ParamLoggingAspect paramLoggingAspect() {
			return new ParamLoggingAspect(new ParamHandlerImpl());
		}
	}

	static class Pointcuts {

		@Pointcut("within(bus.loona..*)")
		void w() { /* Pointcut Designator Method */ }

		@Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
		void c() { /* Pointcut Designator Method */ }

		@Pointcut("execution(public * bus.loona..*Controller.*(..))")
		void e() { /* Pointcut Designator Method */ }

		@Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || "
				+ "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
				+ "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
				+ "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
		void m() { /* Pointcut Designator Method */ }
	}

	@Slf4j
	@Aspect
	@Order(101)
	static class ParamLoggingAspect {

		private final ParamHandler handler;

		ParamLoggingAspect(final ParamHandler handler) {
			this.handler = handler;
		}

		@Before("bus.loona.config.BaseConfig.Pointcuts.w() && " +
				"bus.loona.config.BaseConfig.Pointcuts.c() && " +
				"bus.loona.config.BaseConfig.Pointcuts.e() && " +
				"bus.loona.config.BaseConfig.Pointcuts.m() && " + "args(..)")
		public void paramLoggingBefore(final JoinPoint point) {
			log.debug("{}.{} -> {}", point.getTarget().getClass().getSimpleName(), point.getSignature().getName(), handler.extract(point));
		}
	}

	interface ParamHandler {
		Map<String, Object> extract(final JoinPoint point);
	}

	@Slf4j
	static class ParamHandlerImpl implements ParamHandler {

		private static final String ERROR_FORMAT = "Param Extraction From Method Failure ({} Exception) -> {}";

		@Override
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

			return ImmutableMap.of();
		}

		private boolean useless(final Object source) {

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
			return Optional.ofNullable(source).filter(s -> !useless(s)).orElseGet(Optional::empty);
		}
	}

	static class AwsCredentialsUtil {

		static AWSCredentialsProvider from(final BaseProperties.AwsAccess aws) {

			switch (aws.getMode()) {
				case "S": return new AWSStaticCredentialsProvider(
						new BasicAWSCredentials(Objects.requireNonNull(aws.getAccess()), Objects.requireNonNull(aws.getSecret())));
				case "I":
				default: return new InstanceProfileCredentialsProvider(true);
			}
		}

		private AwsCredentialsUtil() {
			throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
		}
	}

	@Configuration
	@EnableConfigurationProperties(BaseProperties.AwsSes.class)
	@ConditionalOnClass(name="com.amazonaws.services.simpleemail.AmazonSimpleEmailService")
	protected static class AwsSesConfig {

		private final BaseProperties.AwsSes props;

		@Autowired
		protected AwsSesConfig(final BaseProperties.AwsSes props) {
			this.props = props;
		}

		@Bean(destroyMethod="shutdown")
		public AmazonSimpleEmailService simpleMailServiceClient() {
			return AmazonSimpleEmailServiceClientBuilder.standard()
					.withCredentials(AwsCredentialsUtil.from(props)).withRegion(props.getRegion()).build();
		}
	}

	@Configuration
	@EnableConfigurationProperties(BaseProperties.AwsSts.class)
	@ConditionalOnClass(name="com.amazonaws.services.securitytoken.AWSSecurityTokenService")
	protected static class AwsStsConfig {

		private final BaseProperties.AwsSts props;

		@Autowired
		protected AwsStsConfig(final BaseProperties.AwsSts props) {
			this.props = props;
		}

		@Bean(destroyMethod="shutdown")
		public AWSSecurityTokenService amazonStsClient() {
			return AWSSecurityTokenServiceClientBuilder.standard()
					.withCredentials(AwsCredentialsUtil.from(props)).withRegion(props.getRegion()).build();
		}
	}

	@Configuration
	@EnableWebSecurity
	protected static class HttpWebSecuritiesConfig extends WebSecurityConfigurerAdapter {

		private static final String[] STATIC_RESOURCE_PATTERNS = {"/favicon.ico"};
		private static final String[] PROTECTION_PATTERNS = {"/actuator/shutdown", "/user/**"};

		@Override
		protected void configure(final HttpSecurity http) throws Exception {

			http.sessionManagement().disable();
			http.httpBasic().and().exceptionHandling();
			http.headers().frameOptions().sameOrigin().xssProtection();
			http.csrf().ignoringAntMatchers("/actuator/**", "/test/**", "/user/**");
			http.authorizeRequests().antMatchers(PROTECTION_PATTERNS).fullyAuthenticated().anyRequest().permitAll();
		}

		@Override
		public void configure(final WebSecurity web) {
			web.ignoring().antMatchers(STATIC_RESOURCE_PATTERNS);
		}
	}

	@Configuration
	@EnableFeignClients(basePackages=Constants.BASE_PACKAGE)
	protected static class FeignClientConfig {

		private static final String PROP_NAME = "base.logging.feign-level";

		private final Logger.Level feignLoggerLevel;

		@Autowired
		protected FeignClientConfig(final Environment env) {
			this.feignLoggerLevel = Enum.valueOf(Logger.Level.class,
					env.getProperty(PROP_NAME, Logger.Level.FULL.name()).toUpperCase(Locale.getDefault()));
		}

		@Bean
		public Logger.Level feignClientLoggerLevel() {
			return feignLoggerLevel;
		}
	}

	@Configuration
	@ConditionalOnClass(name="io.micrometer.core.instrument.MeterRegistry")
	protected static class CustomMetricsConfig {

		private static final String JVM_METRIC_NAME = "jvm.threads.deadlocked";
		private static final String JVM_METRIC_DESC = "In deadlock waiting to acquire object monitors or ownable synchronizers";

		private static final String CPU_METRIC_NAME = "process.cpu.seconds.total";
		private static final String CPU_METRIC_DESC = "Total user and system CPU time spent in seconds";

		private static final double NANOSECONDS_PER_SECOND = 1E9;
		private static final String CPU_METHOD_NAME = "getProcessCpuTime";
		private static final List<String> CPU_CLASS_NAMES = ImmutableList.of("com.ibm.lang.management.OperatingSystemMXBean", "com.sun.management.OperatingSystemMXBean");

		private final ThreadMXBean mxBean;
		private final OperatingSystemMXBean osBean;

		@Nullable private final Method osMethod;
		@Nullable private final Class<?> osClazz;

		protected CustomMetricsConfig() {

			this.mxBean = ManagementFactory.getThreadMXBean();
			this.osBean = ManagementFactory.getOperatingSystemMXBean();
			this.osClazz = findMXBeanClass();
			this.osMethod = findMXBeanMethod();
		}

		@Bean
		@ConditionalOnMissingBean
		public JvmInfoMetrics jvmInfoMetrics() {
			return new JvmInfoMetrics();
		}

		@Bean
		public MeterBinder jvmDeadlockThreadsMetrics() {
			return mr -> Gauge.builder(JVM_METRIC_NAME, mxBean, v -> calculate(v.findDeadlockedThreads())).description(JVM_METRIC_DESC).tags(ImmutableList.of()).register(mr);
		}

		@Bean
		public MeterBinder processCpuSecondTotalMetrics() {
			return mr -> FunctionCounter.builder(CPU_METRIC_NAME, osBean, m -> executeMethod()).tags(ImmutableList.of()).description(CPU_METRIC_DESC).register(mr);
		}

		private Double calculate(@Nullable final long[] source) {
			return Double.valueOf(Optional.ofNullable(source).map(c -> c.length).orElse(0));
		}

		@Nullable
		private Class<?> findMXBeanClass() {

			for (final String name : CPU_CLASS_NAMES) {
				try {
					return Class.forName(name);
				} catch (final ClassNotFoundException e) {
					// No Operation Here
				}
			}

			return null;
		}

		@Nullable
		private Method findMXBeanMethod() {

			if (Objects.isNull(osClazz)) { return null; }

			try {
				osClazz.cast(osBean);
				return osClazz.getDeclaredMethod(CPU_METHOD_NAME);
			} catch (final ClassCastException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}

		private double executeMethod() {

			if (Objects.isNull(osMethod)) { return Double.NaN; }

			try {
				return Optional.ofNullable(osMethod.invoke(osBean)).map(v -> ((Long)v) / NANOSECONDS_PER_SECOND).orElse(Double.NaN);
			} catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				return Double.NaN;
			}
		}
	}
}
