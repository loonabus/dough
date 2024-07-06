package io.doe.persistence;

import io.doe.domain.TestRes;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.generated.tables.TestUserInfo;
import org.jooq.generated.tables.records.TestUserInfoRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestUserRepositoriesImpl
 * started on 2017-05-?? ~
 */

@Slf4j
@Repository
public class TestUserRepositoriesImpl implements TestUserRepositories<TestRes.TestUser, String> {

	private static final TestUserInfo TABLE = TestUserInfo.TEST_USER_INFO;

	private final DSLContext context;

	@Autowired
	public TestUserRepositoriesImpl(final DSLContext context) {
		this.context = context;
	}

	@Override
	public List<TestRes.TestUser> selectList() {
		context.select(TABLE.TEST_USER_ID, TABLE.TEST_USER_NAME, TABLE.CREATE_DT).from(TABLE).orderBy(TABLE.TEST_USER_NAME.asc()).fetch();
		return context.select(TABLE.TEST_USER_ID, TABLE.TEST_USER_NAME, TABLE.CREATE_DT).from(TABLE).orderBy(TABLE.TEST_USER_NAME.asc()).fetchInto(TestRes.TestUser.class);
	}

	@Nullable
	@Override
	public TestRes.TestUser selectOne(final String uk) {
		return context.select(TABLE.TEST_USER_ID, TABLE.TEST_USER_NAME, TABLE.CREATE_DT).from(TABLE).where(TABLE.TEST_USER_NAME.eq(uk)).fetchOneInto(TestRes.TestUser.class);
	}

	@Nullable
	@Override
	public Integer create(final TestRes.TestUser v) {
		return context.insertInto(TABLE).set(TABLE.TEST_USER_NAME, v.testUserName()).returning().fetchOne(TestUserInfoRecord::getTestUserId);
	}
}
