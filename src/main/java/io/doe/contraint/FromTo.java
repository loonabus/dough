package io.doe.contraint;

import io.doe.common.Constants;
import io.doe.contraint.validator.FromToValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see FromToValidator
 * started on 2017-05-?? ~
 */

@Documented
@Target({TYPE, ANNOTATION_TYPE})
@Repeatable(FromTo.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=FromToValidator.class)
public @interface FromTo {

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default Constants.BASE_PACKAGE + ".validation.constraints.FromTo.message";

	@Documented
	@Target({TYPE, ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@interface List { FromTo[] value(); }
}
