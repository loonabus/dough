package io.doe.config;

import io.doe.common.Constants;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
public class WebMvcConfig {

	@Bean(name="error")
	@ConditionalOnMissingBean(name="error")
	public View defaultErrorView() {
		return new GlobalErrorHtmlView();
	}

	@Slf4j
	public static class GlobalErrorHtmlView implements View {

		private static final String ERROR_HTML = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta charset="UTF-8" />
				<title>Error!!</title>
			</head>
			<body>
				<header>Unexpected Error Occurred</header>
				<script type="text/javascript">
					window.onload = function() { 
						console.log('status : #status# (#error#)');
						console.log('message : #message# (#trace#)');
					};
				</script>
			</body>
		</html>""";

		private static final Pattern ERASE_PATTERN;
		private static final Pattern INTERPOLATION_FIELD_PATTERN;

		static {
			ERASE_PATTERN = Pattern.compile("#", Pattern.LITERAL);
			INTERPOLATION_FIELD_PATTERN = Pattern.compile("(#status#|#error#|#message#|#trace#)");
		}

		@Override
		public String getContentType() {
			return MediaType.TEXT_HTML_VALUE;
		}

		@Override
		public void render(@Nullable final Map<String, ?> model,
						   final HttpServletRequest request, final HttpServletResponse response) throws Exception {

			if (response.isCommitted()) {
				log.error("Response committed so you may ignore this information -> {}", model); return;
			}

			response.setContentType(getContentType());

			FileCopyUtils.copy(createContents(model), response.getOutputStream());
		}

		private InputStream createContents(@Nullable final Map<String, ?> model) {

			final StringBuilder values = new StringBuilder();
			final Matcher matcher = INTERPOLATION_FIELD_PATTERN.matcher(ERROR_HTML);
			final Map<String, ?> contents = Optional.ofNullable(model).orElseGet(Map::of);

			while (matcher.find()) {
				matcher.appendReplacement(values, String.valueOf(contents.get(ERASE_PATTERN.matcher(matcher.group()).replaceAll(""))));
			}

			return new ByteArrayInputStream(matcher.appendTail(values).toString().getBytes(StandardCharsets.UTF_8));
		}
	}

	@Bean
	public WebMvcConfigurer converterConfigurer() {

		return new WebMvcConfigurer() {
			@Override
			public void addFormatters(final FormatterRegistry registerer) {
				registerer.addConverter(String.class, String.class, String::trim);
			}
		};
	}

	@Bean
	@ConditionalOnProperty(prefix="base", name="springdoc-swagger-enabled", havingValue="true")
	public GroupedOpenApi createGroupedOpenApi() {
		return GroupedOpenApi.builder().group("public").pathsToMatch("/**").packagesToScan(Constants.BASE_PACKAGE + ".web").build();
	}
}
