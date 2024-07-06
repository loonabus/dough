package io.doe.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.io.Serial;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

@Configuration
@EnableConfigurationProperties(BaseProperties.Jackson.class)
@AutoConfiguration(after=JacksonAutoConfiguration.class, before=HttpMessageConvertersAutoConfiguration.class)
public class JacksonConfig {

	private final ZoneId zoneId;
	private final String format;
	private final DateTimeFormatter formatter;

	@Autowired
	public JacksonConfig(final BaseProperties.Jackson props) {

		this.zoneId = props.getZoneId();
		this.format = props.getDateFormat();
		this.formatter = DateTimeFormatter.ofPattern(format);
	}

	@Bean
	public SimpleModule customJsr310DateTimeModule() {

		return new SimpleModule("Jsr310DateTimeModule", Version.unknownVersion(), Map.of(
				LocalDateTime.class, new LocalDateTimeDeserializer(formatter),
				ZonedDateTime.class, new ZonedDateTimeJsonDeserializer(format, formatter.withZone(zoneId))),
				List.of(new LocalDateTimeSerializer(formatter), new ZonedDateTimeSerializer(formatter.withZone(zoneId))));
	}

	@Bean
	public SimpleModule xssProtectionModule() {
		return new SimpleModule("XssProtectionModule", Version.unknownVersion(), Map.of(String.class, new XssProtectionDeserializer()));
	}

	@Slf4j
	protected static class ZonedDateTimeJsonDeserializer extends JsonDeserializer<ZonedDateTime> {

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

	@Documented
	@Target({FIELD, PARAMETER})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface XssIgnore { /* Marker Annotation */ }

	static class XssProtectionDeserializer extends StringDeserializer implements ContextualDeserializer {

		@Serial
		private static final long serialVersionUID = 1L;
		private static final StringDeserializer DESERIALIZER = StringDeserializer.instance;

		@Override
		public JsonDeserializer<String> createContextual(final DeserializationContext c, final BeanProperty bp) {
			return Objects.isNull(bp.getAnnotation(XssIgnore.class)) ? this : DESERIALIZER;
		}

		@Nullable
		@Override
		public String deserialize(final JsonParser p, final DeserializationContext c) throws IOException {
			return StringEscapeUtils.escapeHtml4(super.deserialize(p, c));
		}
	}

	@Bean
	@ConditionalOnBean(Jackson2ObjectMapperBuilder.class)
	public Jackson2ObjectMapperBuilderCustomizer objectMapperBuilderCustomizer() {
		return c -> c.findModulesViaServiceLoader(true);
	}


	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2Converter(final Jackson2ObjectMapperBuilder builder) {

		final ObjectMapper mapper = builder.build();
		mapper.getFactory().setCharacterEscapes(new XssCharacterEscapes());

		return new XssMappingJackson2HttpMessageConverter(mapper);
	}

	public static class XssMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter implements Ordered {

		public XssMappingJackson2HttpMessageConverter(final ObjectMapper mapper) {
			super(mapper);
		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE;
		}
	}

	@SuppressWarnings("unused")
	protected static class XssCharacterEscapes extends CharacterEscapes {

		@Serial
		private static final long serialVersionUID = 1L;

		private static final int[] ESCAPES;

		static {
			final int[] source = CharacterEscapes.standardAsciiEscapesForJSON();
			source['<'] = CharacterEscapes.ESCAPE_CUSTOM;
			source['>'] = CharacterEscapes.ESCAPE_CUSTOM;
			source['\"'] = CharacterEscapes.ESCAPE_CUSTOM;
			source['('] = CharacterEscapes.ESCAPE_CUSTOM;
			source[')'] = CharacterEscapes.ESCAPE_CUSTOM;
			source['#'] = CharacterEscapes.ESCAPE_CUSTOM;
			source['\''] = CharacterEscapes.ESCAPE_CUSTOM;

			ESCAPES = Arrays.copyOf(source, source.length);
		}

		@Override
		public int[] getEscapeCodesForAscii() {
			return ESCAPES;
		}

		@Override
		public SerializableString getEscapeSequence(final int c) {
			return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString((char)c)));
		}
	}
}
