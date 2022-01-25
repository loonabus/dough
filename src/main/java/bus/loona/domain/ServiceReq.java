package bus.loona.domain;

import bus.loona.common.Constants;
import bus.loona.common.RandomGenerator;
import bus.loona.common.constraints.*;
import bus.loona.domain.base.BaseReq;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see ServiceReq
 * started on 2017-05-?? ~
 */

public final class ServiceReq {

	private ServiceReq() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}

	@Getter @ToString
	public static final class UserPrm {

		private final String name;

		public UserPrm(final String name) { this.name = name; }
	}

	@Getter @ToString
	public static final class MailPrm {

		private final String title;
		private final String contents;
		private final List<String> recipients;

		public MailPrm(final String title, final String contents, final List<String> recipients) {

			this.title = title;
			this.contents = contents;
			this.recipients = recipients;
		}

		public String concatenate() {
			return String.join(",", recipients);
		}
	}

	@Getter @ToString
	public static final class RedisPrm {

		private final String code;
		private final Collection<IpAddress> ipAddresses;

		public RedisPrm(final String code, final Collection<IpAddress> ipAddresses) {

			this.code = code;
			this.ipAddresses = ImmutableList.copyOf(ipAddresses);
		}
	}

	@Getter @ToString
	@CidrIpCheck(groups=BaseReq.LowerSequence.class)
	@GroupSequence({IpAddress.class, BaseReq.LowerSequence.class})
	public static final class IpAddress {

		@IpCheck(desc=BaseReq.Props.ADDR_IPV4_DESC) private final String address;

		@NullCheck(name=BaseReq.Props.CIDR_NAME)
		@PatternCheck(name=BaseReq.Props.CIDR_NAME, regexp=BaseReq.Props.CIDR_EXPR, desc=BaseReq.Props.CIDR_DESC)
		private final String cidr;

		@Builder @Jacksonized
		private IpAddress(final String address, final String cidr) {

			this.address = address;
			this.cidr = cidr;
		}
	}

	@Getter @ToString
	public static final class UserReq implements BaseReq {

		@NullCheck(name=Props.USER_NAME)
		@LengthCheck(name=Props.USER_NAME, min=5, max=100)
		private final String name;

		@Builder @Jacksonized
		private UserReq(final String name) { this.name = name; }
	}

	public static UserPrm from(final UserReq req) {
		return new UserPrm(req.getName());
	}

	@Getter @ToString
	@GroupSequence({RedisReq.class, BaseReq.LowerSequence.class})
	public static final class RedisReq implements BaseReq {

		@NullCheck(name=Props.IP_CIDR_NAME)
		@SizeCheck(name=Props.IP_CIDR_NAME, max=10)
		private final Collection<@Valid IpAddress> ipAddresses;

		@Builder @Jacksonized
		private RedisReq(final Collection<@Valid IpAddress> ipAddresses) {
			this.ipAddresses = ImmutableList.copyOf(ipAddresses);
		}
	}

	public static RedisPrm from(final RedisReq req) {
		return new RedisPrm(RandomGenerator.createRandomUUID(), ImmutableList.copyOf(req.getIpAddresses()));
	}

	@Getter @ToString
	public static final class ViewReq implements BaseReq {

		@NullCheck(name=Props.CODE_NAME)
		@PatternCheck(name=Props.CODE_NAME, desc=Props.CODE_DESC, regexp=Props.CODE_EXPR)
		private final String shortCode;

		public ViewReq(final String shortCode) {
			this.shortCode = shortCode;
		}
	}
}

