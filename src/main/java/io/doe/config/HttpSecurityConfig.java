package io.doe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
@EnableWebSecurity
public class HttpSecurityConfig {

	private static final String[] STATIC_RESOURCES = {"/favicon.ico"};
	private static final String[] PROTECTION_RESOURCES = {"/actuator/shutdown"};
	private static final String[] CSRF_PROTECTION_SKIP_RESOURCES = {"/actuator/**","/dough/**"};

	@Bean
	public SecurityFilterChain createFilterChain(final HttpSecurity http) throws Exception {

		http.sessionManagement(AbstractHttpConfigurer::disable);
		http.httpBasic(Customizer.withDefaults()).exceptionHandling(Customizer.withDefaults());
		http.csrf(c -> c.ignoringRequestMatchers(CSRF_PROTECTION_SKIP_RESOURCES));
		http.authorizeHttpRequests(r -> r.requestMatchers(PROTECTION_RESOURCES).fullyAuthenticated().anyRequest().permitAll());
		http.headers(h -> h.frameOptions(f -> f.sameOrigin().xssProtection(x -> x.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED))));

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(STATIC_RESOURCES);
	}
}
