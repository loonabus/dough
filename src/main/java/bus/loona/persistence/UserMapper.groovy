package bus.loona.persistence

import bus.loona.domain.vo.User
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Select
import org.springframework.lang.Nullable

@Mapper
interface UserMapper {

	@Select('''
		SELECT
			U.USER_ID,
			U.USER_NAME,
			U.CREATE_DT
		FROM
			USER U
		ORDER BY
			U.USER_NAME ASC
	''') List<User> search()

	@Insert('''
		INSERT INTO
			USER
			(
				USER_NAME
			)
		SELECT
			LOWER(#{userName})
		FROM
			DUAL
		WHERE
			NOT EXISTS
			(
				SELECT
					1
				FROM
					USER U
				WHERE
					U.USER_NAME = LOWER(#{userName})
				LIMIT 1
			)
	''')
	@Options(useGeneratedKeys=true, keyProperty="userId") Integer create(final User user)

	@Nullable
	@Select('''
		SELECT
			U.USER_ID,
			U.USER_NAME,
			U.CREATE_DT
		FROM
			USER U
		WHERE
			U.USER_ID = #{userId}
	''') User searchInfo(final User user)
}
