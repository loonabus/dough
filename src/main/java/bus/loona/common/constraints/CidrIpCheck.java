package bus.loona.common.constraints;

import bus.loona.common.Constants;
import bus.loona.common.constraints.validator.CidrIpCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see CidrIpCheck
 * started on 2017-05-?? ~
 */

@Documented
@Target({TYPE, ANNOTATION_TYPE})
@Repeatable(CidrIpCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= CidrIpCheckValidator.class)
public @interface CidrIpCheck {

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "CidrIpCheck.message}";

	@Documented
	@Target({TYPE, ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@interface List { CidrIpCheck[] value(); }
}
