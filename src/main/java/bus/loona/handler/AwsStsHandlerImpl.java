package bus.loona.handler;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.DecodeAuthorizationMessageRequest;
import com.amazonaws.services.securitytoken.model.InvalidAuthorizationMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see AWSSecurityTokenService
 * started on 2017-05-?? ~
 */

@Slf4j @Component
public class AwsStsHandlerImpl implements AwsStsHandler {

	private static final String MESSAGE_PREFIX;
	private static final Pattern MESSAGE_PREFIX_PATTERN;

	static {
		MESSAGE_PREFIX = "You are not authorized to perform this operation. Encoded authorization failure message: ";
		MESSAGE_PREFIX_PATTERN = Pattern.compile(MESSAGE_PREFIX, Pattern.LITERAL);
	}

	private final AWSSecurityTokenService service;

	@Autowired
	public AwsStsHandlerImpl(final AWSSecurityTokenService service) {
		this.service = service;
	}

	@Override
	@Nullable
	public String decodeFailureMessage(@Nullable final String message) {

		if (!StringUtils.hasText(message)) {
			return null;
		}

		return Optional.of(message).filter(s -> s.startsWith(MESSAGE_PREFIX)).map(this::decode).orElse(message);
	}

	private String decode(final String sourceMessage) {

		final String encodedMessage = MESSAGE_PREFIX_PATTERN.matcher(sourceMessage).replaceFirst("");

		try {
			return MESSAGE_PREFIX + service.decodeAuthorizationMessage(createRequest(encodedMessage)).getDecodedMessage();
		} catch (final InvalidAuthorizationMessageException e) {
			log.info("STS DecodeAuthorizationMessage Failed -> {}", e.getErrorMessage());
			return sourceMessage;
		}
	}

	private DecodeAuthorizationMessageRequest createRequest(final String encoded) {
		return new DecodeAuthorizationMessageRequest().withEncodedMessage(encoded);
	}
}
