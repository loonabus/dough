package bus.loona.domain.base;

import bus.loona.common.Constants;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see BaseReq
 * started on 2017-05-?? ~
 */

public interface BaseReq {

	final class Props {

		public static final String IP_CIDR_NAME = "Ip address + Cidr set";
		public static final String ADDR_IPV4_DESC = "(Invalid IPv4 Address)";

		public static final String USER_NAME = "User name";

		public static final String CIDR_NAME = "CIDR";
		public static final String CIDR_EXPR = "(/32|/24|/16|/8)";
		public static final String CIDR_DESC = "(/8 OR /16 OR /24 OR /32)";

		public static final String CODE_NAME = "Code";
		public static final String CODE_EXPR = "^[A-Za-z0-9]{5,10}$";
		public static final String CODE_DESC = "(AlphaNumeric 5 ~ 10 Characters)";

		private Props() {
			throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
		}
	}

	interface LowerSequence { /* Interface for Validation Ordering */ }

	interface FromTo {
		@Nullable ZonedDateTime getFrom();
		ZonedDateTime getTo();
	}
}
