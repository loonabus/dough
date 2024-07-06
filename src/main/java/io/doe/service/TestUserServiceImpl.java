package io.doe.service;

import io.doe.common.BaseException;
import io.doe.domain.TestReq;
import io.doe.domain.TestRes;
import io.doe.domain.TestUserMapping;
import io.doe.persistence.TestUserRepositories;
import io.doe.service.base.BaseService;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestUserServiceImpl
 * started on 2017-05-?? ~
 */

@Slf4j
@Service
public class TestUserServiceImpl implements TestUserService<TestRes.TestUser>, BaseService {

	private static final String META_CACHE_NAME = "meta";

	private final CacheManager manager;
	private final MessageSourceAccessor accessor;

	private final TestUserMapping mapping;
	private final TestUserRepositories<TestRes.TestUser, String> repositories;

	@Autowired
	public TestUserServiceImpl(final CacheManager manager, final MessageSourceAccessor accessor,
							   final TestUserMapping mapping, final TestUserRepositories<TestRes.TestUser, String> repositories) {
		this.manager = manager; this.accessor = accessor;
		this.mapping = mapping; this.repositories = repositories;
	}

	@Override
	@Cacheable(key="#root.methodName", value=META_CACHE_NAME, unless="#result == null")
	public List<TestRes.TestUser> retrieve() {
		return repositories.selectList();
	}

	@Nullable
	@Override
	@Transactional
	public TestRes.TestUser create(final TestReq.TestUserPrm prm) {

		try {
			Optional.ofNullable(repositories.create(mapping.from(prm)))
					.filter(v -> 1 == Integer.signum(v)).orElseThrow(() -> new BaseException(retrieveMessageFrom("failure.message")));
		} catch (final DuplicateKeyException e) {
				throw new BaseException(retrieveMessageFrom("duplicated.message", prm.testUserName()), e);
		}

		clearCache();

		return repositories.selectOne(prm.testUserName());
	}

	@Override
	public MessageSourceAccessor retrieveAccessor() {
		return accessor;
	}

	private void clearCache() {
		Optional.ofNullable(manager.getCache(META_CACHE_NAME)).ifPresent(Cache::invalidate);
	}
}
