package bus.loona.common.constraints.validator;

import bus.loona.common.constraints.IpCheck;
import org.springframework.lang.Nullable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see IpCheckValidator
 * started on 2017-05-?? ~
 */

public class IpCheckValidator implements ConstraintValidator<IpCheck, String> {

	private boolean nullable = false;
	private boolean receiveIpv6 = false;
	private boolean receiveIpv4Reserved = false;

	@Override
	public void initialize(final IpCheck annotation) {

		this.nullable = annotation.nullable();
		this.receiveIpv6 = annotation.receiveIpv6();
		this.receiveIpv4Reserved = annotation.receiveIpv4Reserved();
	}

	@Override
	public boolean isValid(@Nullable final String value, final ConstraintValidatorContext context) {
		return Objects.isNull(value) ? nullable : checkIpv6(value, checkIpv4Reserved(value, checkIpv4(value)));
	}

	private boolean checkIpv4(final String source) {
		return IpCheckPattern.EXPR_IPV4.matcher(source).matches();
	}

	private boolean checkIpv4Reserved(final String source, final boolean checked) {
		return receiveIpv4Reserved ? checked : checked && IpCheckPattern.EXPR_IPV4_RESERVED.stream().noneMatch(p -> p.matcher(source).matches());
	}

	private boolean checkIpv6(final String source, final boolean checked) {
		return receiveIpv6 ? checked || IpCheckPattern.EXPR_IPV6.matcher(source).matches() : checked;
	}
}