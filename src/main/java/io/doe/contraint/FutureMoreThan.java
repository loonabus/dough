package io.doe.contraint;

import io.doe.common.Constants;
import io.doe.contraint.validator.FutureMoreThanValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see FutureMoreThanValidator
 * started on 2017-05-?? ~
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FutureMoreThan.List.class)
@Constraint(validatedBy=FutureMoreThanValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface FutureMoreThan {

	String name();
	long minutes() default 10L;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default Constants.BASE_PACKAGE + ".validation.constraints.FutureMoreThan.message";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { FutureMoreThan[] value(); }
}
