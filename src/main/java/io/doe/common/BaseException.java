package io.doe.common;

import java.io.Serial;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RuntimeException
 * started on 2017-05-?? ~
 */

public class BaseException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public BaseException(final String message) {
		super(message);
	}
	public BaseException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
