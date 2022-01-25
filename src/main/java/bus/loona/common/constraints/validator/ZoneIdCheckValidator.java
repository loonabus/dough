package bus.loona.common.constraints.validator;

import bus.loona.common.constraints.ZoneIdCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see java.time.ZoneId
 * started on 2017-05-?? ~
 */

@Slf4j
public class ZoneIdCheckValidator implements ConstraintValidator<ZoneIdCheck, String> {

	private boolean nullable = false;

	@Override
	public void initialize(final ZoneIdCheck annotation) {
		this.nullable = annotation.nullable();
	}

	@Override
	public boolean isValid(@Nullable final String value, final ConstraintValidatorContext context) {
		return Objects.isNull(value) ? nullable : validate(value);
	}

	private boolean validate(final String zone) {

		try {
			return !ZoneId.of(zone, ZoneId.SHORT_IDS).getId().isEmpty();
		} catch (final DateTimeException e) {
			log.debug("ZoneId Validation Failed With -> {}", e.getMessage(), e);
		} catch (final Exception e) {
			log.debug("ZoneId Validation Failed With Non-DateTimeException -> {}", e.getMessage(), e);
		}

		return false;
	}
}
