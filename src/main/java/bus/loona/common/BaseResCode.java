package bus.loona.common;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see HttpStatus
 * started on 2017-05-?? ~
 */

@Getter
public enum BaseResCode {

	OK(0, "Success")
	, BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase())
	, NOT_FOUND(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase())
	, METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED.value(), HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
	, NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE.value(), "'Accept' In Header " + HttpStatus.NOT_ACCEPTABLE.getReasonPhrase())
	, UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "'Content-Type' In Header " + HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
	, INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server Exception Occurred")
	, SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
	, DOMAIN_EXCEPTION(900, "Domain Exception Occurred")
	, PERSISTENCE_EXCEPTION(901, "Exception While Handling Data")
	, GENERAL_OTHER_EXCEPTION(902, "Unexpected Exception Occurred")
	, AWS_EXCEPTION(903, "Infrastructure related Exception Occurred")
	;

	private static final Set<BaseResCode> SERVER_SIDE_ERRORS;

	static {
		SERVER_SIDE_ERRORS = ImmutableSet.of(BaseResCode.INTERNAL_SERVER_ERROR,
				BaseResCode.DOMAIN_EXCEPTION, BaseResCode.PERSISTENCE_EXCEPTION, BaseResCode.GENERAL_OTHER_EXCEPTION);
	}

	BaseResCode(final Integer value, final String reason) {
		this.value = value; this.reason = reason;
	}

	private final Integer value;
	private final String reason;

	public boolean hasReasonPhrase() {
		return StringUtils.hasText(reason);
	}

	public static boolean oneOfServerSideError(final BaseResCode code) {
		return SERVER_SIDE_ERRORS.contains(code);
	}
}
