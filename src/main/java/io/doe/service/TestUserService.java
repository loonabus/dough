package io.doe.service;

import io.doe.domain.TestReq;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestUserService
 * started on 2017-05-?? ~
 */

public interface TestUserService<T> {

	List<T> retrieve();
	@Nullable T create(final TestReq.TestUserPrm prm);
}
