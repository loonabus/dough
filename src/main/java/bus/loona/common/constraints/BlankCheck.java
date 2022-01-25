package bus.loona.common.constraints;

import bus.loona.common.Constants;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotBlank;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see NotBlank
 * started on 2017-05-?? ~
 */

@NotBlank
@Documented
@ReportAsSingleViolation
@Constraint(validatedBy={})
@Repeatable(BlankCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface BlankCheck {

	String name();

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "BlankCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { BlankCheck[] value(); }
}
