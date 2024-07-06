package io.doe.config;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeFilter;
import jakarta.annotation.Nullable;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
public class FilterRegistrationConfig {

	@Bean
	public FilterRegistrationBean<XssFilter> xssProtectionFilterRegisterer() {

		final FilterRegistrationBean<XssFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(new XssFilter());
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

		return bean;
	}

	@Bean
	public FilterRegistrationBean<AbstractRequestLoggingFilter> requestLoggingFilterRegisterer() {

		final FilterRegistrationBean<AbstractRequestLoggingFilter> bean = new FilterRegistrationBean<>();

		final CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true); filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);  filter.setIncludeHeaders(true);

		bean.setFilter(filter);
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 8);

		return bean;
	}

	@Bean
	public FilterRegistrationBean<RemoteAddressFilter> remoteAddressFilterRegisterer() {

		final FilterRegistrationBean<RemoteAddressFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(new RemoteAddressFilter());
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 9);

		return bean;
	}

	public static class XssFilter implements Filter {

		private final XssEscapeFilter escape = XssEscapeFilter.getInstance();

		@Override
		public void init(final FilterConfig filterConfig) { /* No Operation Here */ }

		@Override
		public void doFilter(final ServletRequest request, final ServletResponse response,
							 final FilterChain chain) throws IOException, ServletException {
			chain.doFilter(new XssFilterWrapper(request, escape), response);
		}

		@Override public void destroy() { /* No Operation Here */ }
	}

	protected static class XssFilterWrapper extends HttpServletRequestWrapper {

		private final String path;
		private final XssEscapeFilter escape;

		protected XssFilterWrapper(final ServletRequest request, final XssEscapeFilter escape) {

			super((HttpServletRequest)request);

			this.escape = escape;
			this.path = ((HttpServletRequest)request).getRequestURI().substring(((HttpServletRequest)request).getContextPath().length());
		}

		@Override
		public String getParameter(final String name) {
			return escape.doFilter(path, name, super.getParameter(name));
		}

		@Nullable
		@Override
		public String[] getParameterValues(final String name) {

			final String[] values = super.getParameterValues(name);
			if (Objects.isNull(values)) {
				return null;
			}

			for (int i=0; i<values.length; i++) {
				values[i] = escape.doFilter(path, name, values[i]);
			}

			return values;
		}

		@Override
		public Map<String, String[]> getParameterMap() {

			final Map<String, String[]> n = new HashMap<>();
			final Map<String, String[]> o = super.getParameterMap();
			final Set<Map.Entry<String, String[]>> entries = o.entrySet();

			for (final Map.Entry<String, String[]> e : entries) {
				final String[] source = e.getValue();
				final String[] value = new String[source.length];

				for (int i=0; i<source.length; i++) {
					value[i] = escape.doFilter(path, e.getKey(), String.valueOf(source[i]));
				}

				n.put(e.getKey(), value);
			}

			return n;
		}
	}

	public static class RemoteAddressFilter implements Filter {

		@Override
		public void init(final FilterConfig filterConfig) { /* No Operation Here */ }

		@Override
		public void doFilter(final ServletRequest request, final ServletResponse response,
							 final FilterChain chain) throws IOException, ServletException {
			chain.doFilter((request instanceof HttpServletRequest r ? new RemoteAddressWrapper(r) : request), response);
		}

		@Override public void destroy() { /* No Operation Here */ }
	}

	protected static class RemoteAddressWrapper extends HttpServletRequestWrapper {

		private static final List<String> CANDIDATES;

		static {
			CANDIDATES = List.of("X-Forwarded-For","Proxy-Client-IP","WL-Proxy-Client-IP","HTTP_CLIENT_IP","HTTP_X_FORWARDED_FOR","X-Real-IP");
		}

		protected RemoteAddressWrapper(final HttpServletRequest request) {
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
			return refine(CANDIDATES.stream().map(super::getHeader)
					.filter(StringUtils::hasText).map(String::trim)
					.filter(s -> !Objects.equals("unknown", s)).findFirst().orElseGet(super::getRemoteAddr));
		}

		private String refine(final String source) {
			return !source.contains(",") ? source.trim() : source.split(",")[0].trim();
		}
	}
}
