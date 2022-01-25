package bus.loona.common.constraints;

import bus.loona.common.Constants;
import bus.loona.common.constraints.validator.FutureMoreThanCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see FutureMoreThanCheckValidator
 * started on 2017-05-?? ~
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FutureMoreThanCheck.List.class)
@Constraint(validatedBy=FutureMoreThanCheckValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface FutureMoreThanCheck {

	String name();
	long minutes() default 10L;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "FutureMoreThanCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { FutureMoreThanCheck[] value(); }
}
