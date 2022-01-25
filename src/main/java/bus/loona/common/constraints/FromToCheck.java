package bus.loona.common.constraints;

import bus.loona.common.Constants;
import bus.loona.common.constraints.validator.FromToCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see FromToCheckValidator
 * started on 2017-05-?? ~
 */

@Documented
@Target({TYPE, ANNOTATION_TYPE})
@Repeatable(FromToCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=FromToCheckValidator.class)
public @interface FromToCheck {

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "FromToCheck.message}";

	@Documented
	@Target({TYPE, ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@interface List { FromToCheck[] value(); }
}
