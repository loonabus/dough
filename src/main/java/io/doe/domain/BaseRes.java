package io.doe.domain;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.beans.ConstructorProperties;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see BaseRes
 * started on 2017-05-?? ~
 */

@Getter @ToString
public final class BaseRes<T> {

	private final Integer status;
	private final String message;
	@Nullable private final T data;

	@ConstructorProperties({"status","message","data"})
	private BaseRes(final Integer status, final String message, @Nullable final T data) {
		this.status = status; this.message = message; this.data = data;
	}

	public static <T> BaseRes<T> from(final HttpStatus status, final String message) {
		return new BaseRes<>(status.value(), message, null);
	}

	public static <T> BaseRes<T> from(final HttpStatus status, final String message, final T data) {
		return new BaseRes<>(status.value(), message, data);
	}

	public static <T> BaseRes<T> success(@Nullable final T data) {
		return new BaseRes<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
	}
}
