package io.doe.domain;

import jakarta.annotation.Nullable;

import java.time.ZonedDateTime;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see BaseReq
 * started on 2017-05-?? ~
 */

public interface BaseReq {

	interface FromTo {
		@Nullable ZonedDateTime getFrom();
		ZonedDateTime getTo();
	}
}
