package io.doe.config;

import io.doe.common.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure
 * started on 2017-05-?? ~
 */

public final class BaseProperties {

	private BaseProperties() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}

	@Getter @Validated
	@ConfigurationProperties(prefix="base.jackson")
	public static class Jackson {

		@NotBlank private final String dateFormat;
		@NotNull private final ZoneId zoneId;

		public Jackson(final String dateFormat, final ZoneId zoneId) {
			this.dateFormat = dateFormat; this.zoneId = zoneId;
		}
	}
}
