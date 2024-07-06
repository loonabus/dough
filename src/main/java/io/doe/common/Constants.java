package io.doe.common;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Constants
 * started on 2017-05-?? ~
 */

public final class Constants {

	public static final String BASE_PACKAGE = "io.doe";

	public static final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "cannot create instance of util class";

	private Constants() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}
}
