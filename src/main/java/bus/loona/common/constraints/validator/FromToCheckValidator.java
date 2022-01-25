package bus.loona.common.constraints.validator;

import bus.loona.common.constraints.FromToCheck;
import bus.loona.domain.base.BaseReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see ZonedDateTime
 * started on 2017-05-?? ~
 */

public class FromToCheckValidator implements ConstraintValidator<FromToCheck, BaseReq.FromTo> {

	private final MessageSourceAccessor accessor;

	@Autowired
	public FromToCheckValidator(final MessageSourceAccessor accessor) {
		this.accessor = accessor;
	}

	@Override
	public boolean isValid(final BaseReq.FromTo req, final ConstraintValidatorContext context) {

		final ZonedDateTime from = req.getFrom();
		final ZonedDateTime to = req.getTo();

		if (Objects.isNull(from) || to.isAfter(from)) { return true; }

		final String messageTemplate = context.getDefaultConstraintMessageTemplate();
		final String code = messageTemplate.substring(1, messageTemplate.length()-1);
		final String message = accessor.getMessage(code, new ZonedDateTime[]{from, to}, LocaleContextHolder.getLocale());

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

		return false;
	}
}
