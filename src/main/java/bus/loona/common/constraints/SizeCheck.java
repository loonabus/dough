package bus.loona.common.constraints;

import bus.loona.common.Constants;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Size;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Size
 * started on 2017-05-?? ~
 */

@Size
@Documented
@ReportAsSingleViolation
@Constraint(validatedBy={})
@Repeatable(SizeCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface SizeCheck {

	String name();

	@OverridesAttribute(constraint=Size.class, name="min") int min() default 1;
	@OverridesAttribute(constraint=Size.class, name="max") int max() default Integer.MAX_VALUE;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "SizeCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { SizeCheck[] value(); }
}
