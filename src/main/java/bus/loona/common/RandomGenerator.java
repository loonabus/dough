package bus.loona.common;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see UUID
 * started on 2017-05-?? ~
 */

public final class RandomGenerator {

	private static final Pattern PATTERN = Pattern.compile("-", Pattern.LITERAL);

	public static String createRandomUUID() {
		return PATTERN.matcher(UUID.randomUUID().toString()).replaceAll("");
	}

	private RandomGenerator() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}
}
