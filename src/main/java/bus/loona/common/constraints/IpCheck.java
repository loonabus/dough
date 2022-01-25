package bus.loona.common.constraints;

import bus.loona.common.Constants;
import bus.loona.common.constraints.validator.IpCheckValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see IpCheck
 * started on 2017-05-?? ~
 */

@Documented
@Repeatable(IpCheck.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=IpCheckValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface IpCheck {

	String desc() default "";

	boolean nullable() default false;
	boolean receiveIpv6() default false;
	boolean receiveIpv4Reserved() default false;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	String message() default "{" + Constants.CONSTRAINTS_PREFIX + "IpCheck.message}";

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
	@interface List { IpCheck[] value(); }
}
