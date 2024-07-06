package io.doe.persistence.mapper;

import io.doe.domain.TestRes;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Mapper
 * started on 2017-05-?? ~
 */

@Mapper
public interface TestUserMapper {

	@Select("""
		SELECT
			U.TEST_USER_ID,
			U.TEST_USER_NAME,
			U.CREATE_DT
		FROM
			TEST_USER_INFO U
		ORDER BY
			U.TEST_USER_NAME ASC
			""")
	List<TestRes.TestUser> selectList();

	@Insert("""
		INSERT INTO
			TEST_USER_INFO
			(
				TEST_USER_NAME
			)
		SELECT
			LOWER(#{testUserName}) as TEST
		WHERE
			NOT EXISTS
			(
				SELECT
					1
				FROM
					TEST_USER_INFO U
				WHERE
					U.TEST_USER_NAME = LOWER(#{testUserName})
				LIMIT 1
			)
			""")
	@Options(useGeneratedKeys=true, keyProperty="testUserId") Integer create(final TestRes.TestUser user);

	@Nullable
	@Select("""
		SELECT
			U.TEST_USER_ID,
			U.TEST_USER_NAME,
			U.CREATE_DT
		FROM
			TEST_USER_INFO U
		WHERE
			U.TEST_USER_ID = #{uk}
		""")
	TestRes.TestUser selectOne(final String uk);
}
