package io.doe.contraint;

import io.doe.common.Constants;
import io.doe.contraint.validator.FromToIntervalValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see FromToIntervalValidator
 * started on 2017-05-?? ~
 */

@Documented
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FromToInterval.List.class)
@Constraint(validatedBy=FromToIntervalValidator.class)
public @interface FromToInterval {

	long minutes() default 10L;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default Constants.BASE_PACKAGE + ".validation.constraints.FromToInterval.message";

	@Documented
	@Target({TYPE, ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@interface List { FromToInterval[] value(); }
}
