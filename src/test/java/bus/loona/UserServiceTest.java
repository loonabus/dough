package bus.loona;

import bus.loona.domain.ServiceReq;
import bus.loona.domain.vo.User;
import bus.loona.persistence.UserMapper;
import bus.loona.service.UserService;
import bus.loona.service.UserServiceImpl;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.context.support.MessageSourceAccessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see UserServiceTest
 * started on 2017-05-?? ~
 */

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private static final User TEST_USER = new User(1L, "Test", null);

	private UserService<User> service;

	@Mock UserMapper mapper;
	@Mock CacheManager manager;
	@Mock private MessageSourceAccessor messageSourceAccessor;

	@BeforeEach
	void createAdminUserService() {
		service = new UserServiceImpl(mapper, manager, messageSourceAccessor);
	}

	@Test
	void retrieveAdminUserListTest() {

		Mockito.when(mapper.search()).thenReturn(ImmutableList.of(TEST_USER));

		assertThat(service.search()).isNotNull().isEqualTo(ImmutableList.of(TEST_USER));
	}

	@Test
	void createAdminUserTest() {

		Mockito.when(mapper.create(Mockito.any(User.class))).thenReturn(1);
		Mockito.when(mapper.searchInfo(Mockito.any(User.class))).thenReturn(TEST_USER);
		assertThat(service.create(ServiceReq.from(ServiceReq.UserReq.builder().name("AdminTestUser001").build()))).isNotNull().isEqualTo(TEST_USER);
	}
}
