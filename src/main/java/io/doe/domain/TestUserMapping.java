package io.doe.domain;

import org.mapstruct.Mapper;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestUserMapping
 * started on 2017-05-?? ~
 */

@Mapper
public interface TestUserMapping {

	TestReq.TestUserPrm from(final TestReq.TestUserReq req);
	TestRes.TestUser from(final TestReq.TestUserPrm prm);
}
