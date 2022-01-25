package bus.loona.common.constraints.validator;

import bus.loona.common.constraints.CidrIpCheck;
import bus.loona.domain.ServiceReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see CidrIpCheckValidator
 * started on 2017-05-?? ~
 */

public class CidrIpCheckValidator implements ConstraintValidator<CidrIpCheck, ServiceReq.IpAddress> {

	private static final String CIDR_08 = "/8";
	private static final String CIDR_16 = "/16";
	private static final String CIDR_24 = "/24";

	private final MessageSourceAccessor accessor;

	@Autowired
	public CidrIpCheckValidator(final MessageSourceAccessor accessor) {
		this.accessor = accessor;
	}

	@Override
	public boolean isValid(@Nullable final ServiceReq.IpAddress ipAddress, final ConstraintValidatorContext context) {

		if (ipAddress == null) { return true; }

		final String addr = ipAddress.getAddress();
		final String cidr = ipAddress.getCidr();

		if (!StringUtils.hasText(addr) || !StringUtils.hasText(cidr)) { return true; }

		final boolean valid = validate(addr, cidr);

		if (!valid) {
			final String messageTemplate = context.getDefaultConstraintMessageTemplate();
			final String code = messageTemplate.substring(1, messageTemplate.length()-1);
			final String message = accessor.getMessage(code, new String[]{addr, cidr}, LocaleContextHolder.getLocale());

			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
		}

		return valid;
	}

	private boolean validate(final String addr, final String cidr) {

		switch (cidr) {
			case CIDR_24: return addr.endsWith(".0");
			case CIDR_16: return addr.endsWith(".0.0");
			case CIDR_08: return addr.endsWith(".0.0.0");
			default: return true;
		}
	}
}
