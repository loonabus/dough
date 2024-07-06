package io.doe.domain;

import io.doe.common.Constants;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestRes
 * started on 2017-05-?? ~
 */

public final class TestRes {

	private TestRes() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}

	public record TestUser(@Nullable Long testUserId, @Nullable String testUserName, @Nullable LocalDateTime createDt) { /* Java record */ }
}
