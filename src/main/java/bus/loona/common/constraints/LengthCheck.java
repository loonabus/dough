package bus.loona.common.constraints;

import bus.loona.common.Constants;
import org.hibernate.validator.constraints.Length;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Length
 * started on 2017-05-?? ~
 */

@Length
@Documented
@ReportAsSingleViolation
@Constraint(validatedBy={})
@Repeatable(LengthCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface LengthCheck {

	String name();

	@OverridesAttribute(constraint=Length.class, name="min") int min() default 1;
	@OverridesAttribute(constraint=Length.class, name="max") int max() default Integer.MAX_VALUE;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "LengthCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { LengthCheck[] value(); }
}
