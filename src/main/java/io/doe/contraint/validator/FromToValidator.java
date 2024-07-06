package io.doe.contraint.validator;

import io.doe.contraint.FromTo;
import io.doe.domain.BaseReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see ZonedDateTime
 * started on 2017-05-?? ~
 */

public class FromToValidator implements ConstraintValidator<FromTo, BaseReq.FromTo> {

	private final MessageSourceAccessor accessor;

	@Autowired
	public FromToValidator(final MessageSourceAccessor accessor) {
		this.accessor = accessor;
	}

	@Override
	public boolean isValid(final BaseReq.FromTo req, final ConstraintValidatorContext context) {

		final ZonedDateTime from = req.getFrom();
		final ZonedDateTime to = req.getTo();

		if (Objects.isNull(from) || to.isAfter(from)) { return true; }

		final String message = accessor.getMessage(context.getDefaultConstraintMessageTemplate(), new ZonedDateTime[]{from, to}, LocaleContextHolder.getLocale());

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

		return false;
	}
}
