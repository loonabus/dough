package io.doe.contraint.validator;

import io.doe.config.BaseProperties;
import io.doe.contraint.FutureMoreThan;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see ZonedDateTime
 * started on 2017-05-?? ~
 */

@EnableConfigurationProperties(BaseProperties.Jackson.class)
public class FutureMoreThanValidator implements ConstraintValidator<FutureMoreThan, ZonedDateTime> {

	private Long minutes = 10L;
	private final BaseProperties.Jackson props;

	@Autowired
	public FutureMoreThanValidator(final BaseProperties.Jackson props) {
		this.props = props;
	}

	@Override
	public void initialize(final FutureMoreThan annotation) {
		this.minutes = annotation.minutes();
	}

	@Override
	public boolean isValid(@Nullable final ZonedDateTime value, final ConstraintValidatorContext context) {

		if (Objects.isNull(value)) { return true; }
		final ZonedDateTime comparison = Instant.now().atZone(props.getZoneId()).plusMinutes(minutes);

		return value.isEqual(comparison) || value.isAfter(comparison);
	}
}
