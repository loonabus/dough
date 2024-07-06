package io.doe.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.doe.common.Constants;
import io.doe.config.JacksonConfig;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestReq
 * started on 2017-05-?? ~
 */

public final class TestReq {

	private TestReq() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}

	@Getter
	@ToString
	public static final class TestUserReq implements BaseReq {

		@NotBlank
		@Length(min=5, max=100)
		private final String testUserName;

		@JsonCreator
		private TestUserReq(@JsonProperty("testUserName") @JacksonConfig.XssIgnore final String testUserName) {
			this.testUserName = testUserName;
		}
	}

	public record TestUserPrm(String testUserName) { /* Java record */ }
}

