package io.doe.persistence;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestUserRepositories
 * started on 2017-05-?? ~
 */

public interface TestUserRepositories<T, U> {

	List<T> selectList();
	@Nullable T selectOne(final U uk);
	@Nullable Integer create(final T v);
}
