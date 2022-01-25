package bus.loona.common.constraints;

import bus.loona.common.Constants;
import bus.loona.common.constraints.validator.FromToIntervalCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see FromToIntervalCheckValidator
 * started on 2017-05-?? ~
 */

@Documented
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FromToIntervalCheck.List.class)
@Constraint(validatedBy=FromToIntervalCheckValidator.class)
public @interface FromToIntervalCheck {

	long minutes() default 10L;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "FromToIntervalCheck.message}";

	@Documented
	@Target({TYPE, ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@interface List { FromToIntervalCheck[] value(); }
}
