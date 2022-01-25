package bus.loona.common.constraints.validator;

import bus.loona.common.constraints.FutureMoreThanCheck;
import bus.loona.config.BaseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.Nullable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
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
public class FutureMoreThanCheckValidator implements ConstraintValidator<FutureMoreThanCheck, ZonedDateTime> {

	private Long minutes = 1L;
	private final BaseProperties.Jackson props;

	@Autowired
	public FutureMoreThanCheckValidator(final BaseProperties.Jackson props) {
		this.props = props;
	}

	@Override
	public void initialize(final FutureMoreThanCheck annotation) {
		this.minutes = annotation.minutes();
	}

	@Override
	public boolean isValid(@Nullable final ZonedDateTime value, final ConstraintValidatorContext context) {

		if (Objects.isNull(value)) { return true; }
		final ZonedDateTime comparison = Instant.now().atZone(props.getFixedZoneId()).plusMinutes(minutes);

		return value.isEqual(comparison) || value.isAfter(comparison);
	}
}
