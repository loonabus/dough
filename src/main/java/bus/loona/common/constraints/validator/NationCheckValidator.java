package bus.loona.common.constraints.validator;

import bus.loona.common.constraints.NationCheck;
import com.google.common.collect.ImmutableSet;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see NationCheckValidator
 * started on 2017-05-?? ~
 */

public class NationCheckValidator implements ConstraintValidator<NationCheck, String> {

	private static final Set<String> ISO_COUNTRIES_CD;

	static {
		ISO_COUNTRIES_CD = Arrays.stream(Locale.getISOCountries()).map(e ->
				e.toLowerCase(Locale.getDefault())).collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
	}

	private boolean nullable = false;

	@Override
	public void initialize(final NationCheck annotation) {
		this.nullable = annotation.nullable();
	}

	@Override
	public boolean isValid(@Nullable final String value, final ConstraintValidatorContext context) {
		return (Objects.isNull(value)) ? nullable : checkISOCountriesCode(value.toLowerCase(Locale.getDefault()));
	}

	private boolean checkISOCountriesCode(final String source) {
		return CollectionUtils.isEmpty(ISO_COUNTRIES_CD) || ISO_COUNTRIES_CD.contains(source.toLowerCase(Locale.getDefault()));
	}
}
