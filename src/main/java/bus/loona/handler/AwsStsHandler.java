package bus.loona.handler;

import org.springframework.lang.Nullable;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see com.amazonaws.services.securitytoken.AWSSecurityTokenService
 * started on 2017-05-?? ~
 */

public interface AwsStsHandler {
	@Nullable String decodeFailureMessage(@Nullable final String message);
}
