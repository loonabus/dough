package bus.loona.common;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Constants
 * started on 2017-05-?? ~
 */

public final class Constants {

	public static final String BASE_PACKAGE = "bus.loona";

	public static final String DOMAIN_PREFIX = BASE_PACKAGE + ".base.";
	public static final String VALIDATIONS_PREFIX = BASE_PACKAGE + ".valid.validations.";
	public static final String CONSTRAINTS_PREFIX = BASE_PACKAGE + ".valid.constraints.";

	public static final String DEFAULT_ERROR_MESSAGE = "Exception Occurred";
	public static final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "Utility class";

	public static final String NOT_AVAILABLE = "N/A";

	private Constants() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}
}
