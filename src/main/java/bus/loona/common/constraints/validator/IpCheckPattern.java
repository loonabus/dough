package bus.loona.common.constraints.validator;

import bus.loona.common.Constants;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see Pattern
 * started on 2017-05-?? ~
 */

final class IpCheckPattern {

	private static final String EXPR;

	static final Pattern EXPR_IPV4;
	static final Pattern EXPR_IPV6;
	static final List<Pattern> EXPR_IPV4_RESERVED;

	static {
		EXPR = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

		// https://www.owasp.org/index.php/OWASP_Validation_Regex_Repository
		EXPR_IPV4 = Pattern.compile("^" + EXPR + "\\." + EXPR + "\\." + EXPR + "\\." + EXPR + "$");

		/*
		fe80:0000:0000:0000:0204:61ff:fe9d:f156 // full form of Ipv6
		fe80:0:0:0:204:61ff:fe9d:f156 // drop leading zeroes
		fe80::204:61ff:fe9d:f156 // collapse multiple zeroes to :: in the Ipv6 address
		fe80:0000:0000:0000:0204:61ff:254.157.241.86 // Ipv4 dotted quad at the end
		fe80:0:0:0:0204:61ff:254.157.241.86 // drop leading zeroes, Ipv4 dotted quad at the end
		fe80::204:61ff:254.157.241.86 // dotted quad at the end, multiple zeroes collapsed
		*/
		// https://community.helpsystems.com/forums/intermapper/miscellaneous-topics/5acc4fcf-fa83-e511-80cf-0050568460e4
		EXPR_IPV6 = Pattern.compile("^(((?=(?>.*?::)(?!.*::)))(::)" +
				"?([0-9a-fA-F]{1,4}::?){0,5}|([0-9a-fA-F]{1,4}:){6})" +
				"(\\2([0-9a-fA-F]{1,4}(::?|$)){0,2}|((25[0-5]|(2[0-4]|1\\d|[1-9])" +
				"?\\d)(\\.|$)){4}|[0-9a-fA-F]{1,4}:[0-9a-fA-F]{1,4})(?<![^:]:|\\.)\\z");

		// https://en.wikipedia.org/wiki/Reserved_IP_addresses#cite_note-1
		EXPR_IPV4_RESERVED = ImmutableList.<Pattern>builder()
				// 0.0.0.0 – 0.255.255.255 / 10.0.0.0 – 10.255.255.255 / 127.0.0.0 – 127.255.255.255
				.add(Pattern.compile("^" + "(0|10|127)" + "\\." + EXPR + "\\." + EXPR + "\\." + EXPR + "$"))
				// 100.64.0.0 – 100.127.255.255
				.add(Pattern.compile("^" + "100" + "\\." + "(6[4-9]|[789][0-9]|1[01][0-9]|12[0-7])" + "\\." + EXPR + "\\." + EXPR + "$"))
				// 169.254.0.0 – 169.254.255.255
				.add(Pattern.compile("^" + "169" + "\\." + "254" + "\\." + EXPR + "\\." + EXPR + "$"))
				// 172.16.0.0 – 172.31.255.255
				.add(Pattern.compile("^" + "172" + "\\." + "(1[6-9]|2[0-9]|3[01])" + "\\." + EXPR + "\\." + EXPR + "$"))
				// 192.0.0.0 – 192.0.0.255 / 192.0.2.0 – 192.0.2.255
				.add(Pattern.compile("^" + "192" + "\\." + "0" + "\\." + "([02])" + "\\." + EXPR + "$"))
				// 192.88.99.0 – 192.88.99.255
				.add(Pattern.compile("^" + "192" + "\\." + "88" + "\\." + "99" + "\\." + EXPR + "$"))
				// 192.168.0.0 – 192.168.255.255
				.add(Pattern.compile("^" + "192" + "\\." + "168" + "\\." + EXPR + "\\." + EXPR + "$"))
				// 198.18.0.0 – 198.19.255.255
				.add(Pattern.compile("^" + "198" + "\\." + "(18|19)" + "\\." + EXPR + "\\." + EXPR + "$"))
				// 198.51.100.0 – 198.51.100.255
				.add(Pattern.compile("^" + "198" + "\\." + "51" + "\\." + "100" + "\\." + EXPR + "$"))
				// 203.0.113.0 – 203.0.113.255
				.add(Pattern.compile("^" + "203" + "\\." + "0" + "\\." + "113" + "\\." + EXPR + "$"))
				// 224.0.0.0 – 239.255.255.255 / 240.0.0.0 – 255.255.255.254 / 255.255.255.255
				.add(Pattern.compile("^(22[4-9]|2[34][0-9]|25[0-5])" + "\\." + EXPR + "\\." + EXPR + "\\." + EXPR + "$")).build();
	}

	private IpCheckPattern() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}
}
