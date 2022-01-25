package bus.loona.common.constraints;
import bus.loona.common.Constants;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Email
 * started on 2017-05-?? ~
 */

@Email
@Documented
@ReportAsSingleViolation
@Constraint(validatedBy={})
@Repeatable(EmailCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface EmailCheck {

	String desc() default "";

	@OverridesAttribute(constraint=Email.class, name="regexp") String regexp() default ".*";
	@OverridesAttribute(constraint=Email.class, name="flags") Pattern.Flag[] flags() default {};

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "EmailCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { EmailCheck[] value(); }
}

