package bus.loona;

import bus.loona.domain.vo.User;
import bus.loona.persistence.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see UserMapperTest
 * started on 2017-05-?? ~
 */

@MybatisTest
@ExtendWith(SpringExtension.class)
class UserMapperTest {

	@Autowired private UserMapper mapper;

	@Test
	void retrieveUserListTest() {
		assertThat(mapper.search()).isNotEmpty();
	}

	@Test
	void createUserTest() {
		assertThat(mapper.create(new User(null, "JUnitJupiterTestUser", null))).isEqualTo(1);
	}

	@Test
	void retrieveAdminUserInfoTest() {
		assertThat(mapper.searchInfo(new User(99L, null, null))).isNull();
	}
}
