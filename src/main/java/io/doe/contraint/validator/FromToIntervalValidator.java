package io.doe.contraint.validator;

import io.doe.contraint.FromToInterval;
import io.doe.domain.BaseReq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see ZonedDateTime
 * started on 2017-05-?? ~
 */

public class FromToIntervalValidator implements ConstraintValidator<FromToInterval, BaseReq.FromTo> {

	private Long minutes = 10L;
	private final MessageSourceAccessor accessor;

	@Autowired
	public FromToIntervalValidator(final MessageSourceAccessor accessor) {
		this.accessor = accessor;
	}

	@Override
	public void initialize(final FromToInterval annotation) {
		this.minutes = annotation.minutes();
	}

	@Override
	public boolean isValid(final BaseReq.FromTo req, final ConstraintValidatorContext context) {

		final ZonedDateTime from = req.getFrom();
		final ZonedDateTime to = req.getTo();

		if (Objects.isNull(from)) { return true; }
		if (TimeUnit.MINUTES.toSeconds(minutes) <= Duration.between(from, to).getSeconds()) { return true; }

		final String message = accessor.getMessage(context.getDefaultConstraintMessageTemplate(), new Object[]{minutes, from, to}, LocaleContextHolder.getLocale());

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation();

		return false;
	}
}
