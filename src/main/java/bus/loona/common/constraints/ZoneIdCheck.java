package bus.loona.common.constraints;

import bus.loona.common.Constants;
import bus.loona.common.constraints.validator.ZoneIdCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see java.time.ZoneId
 * started on 2017-05-?? ~
 */

@Documented
@Repeatable(ZoneIdCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= ZoneIdCheckValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface ZoneIdCheck {

	String name() default "ZoneId";

	boolean nullable() default false;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "ZoneIdCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { ZoneIdCheck[] value(); }
}
