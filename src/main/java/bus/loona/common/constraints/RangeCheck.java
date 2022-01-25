package bus.loona.common.constraints;

import bus.loona.common.Constants;
import org.hibernate.validator.constraints.Range;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Range
 * started on 2017-05-?? ~
 */

@Range
@Documented
@ReportAsSingleViolation
@Constraint(validatedBy={})
@Repeatable(RangeCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface RangeCheck {

	String name();

	@OverridesAttribute(constraint=Range.class, name="min") long min() default 1;
	@OverridesAttribute(constraint=Range.class, name="max") long max() default Long.MAX_VALUE;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "RangeCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { RangeCheck[] value(); }
}
