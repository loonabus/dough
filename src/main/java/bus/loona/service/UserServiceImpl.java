package bus.loona.service;

import bus.loona.common.BaseException;
import bus.loona.domain.ServiceReq;
import bus.loona.domain.vo.User;
import bus.loona.persistence.UserMapper;
import bus.loona.service.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see UserServiceImpl
 * started on 2017-05-?? ~
 */

@Slf4j @Service
public class UserServiceImpl implements UserService<User>, BaseService {

	private static final String META_CACHE_NAME = "meta";

	private final UserMapper mapper;
	private final CacheManager manager;
	private final MessageSourceAccessor accessor;

	@Autowired
	public UserServiceImpl(final UserMapper mapper, final CacheManager manager, final MessageSourceAccessor accessor) {

		this.mapper = mapper;
		this.manager = manager;
		this.accessor = accessor;
	}

	@Override
	@Cacheable(key="#root.methodName", value=META_CACHE_NAME, unless="#result == null")
	public List<User> search() {
		return mapper.search();
	}

	@Nullable @Override
	public User create(final ServiceReq.UserPrm prm) {

		final User user = User.from(prm);

		if (1 != mapper.create(user)) {
			throw new BaseException(retrieveMessage("CountMismatch", "Create Admin User"));
		}

		clearCache();

		return mapper.searchInfo(user);
	}

	@Override
	public MessageSourceAccessor retrieveAccessor() {
		return accessor;
	}

	private void clearCache() {
		Optional.ofNullable(manager.getCache(META_CACHE_NAME)).ifPresent(Cache::invalidate);
	}
}
