package bus.loona.common.constraints;

import bus.loona.common.Constants;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Pattern
 * started on 2017-05-?? ~
 */

@Documented
@Pattern(regexp="")
@ReportAsSingleViolation
@Constraint(validatedBy={})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PatternCheck.List.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface PatternCheck {

	String name();
	String desc() default "";

	@OverridesAttribute(constraint=Pattern.class, name="regexp") String regexp();

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "PatternCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { PatternCheck[] value(); }
}
