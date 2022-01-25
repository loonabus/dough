package bus.loona.view;

import bus.loona.common.Constants;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.web.servlet.View
 * started on 2017-05-?? ~
 */

@Slf4j
public abstract class AbstractHtmlView implements View {

	private static final Pattern ERASE_PATTERN;
	private static final String ERROR_MESSAGE_FORMAT;

	static {
		ERASE_PATTERN = Pattern.compile("#", Pattern.LITERAL);
		ERROR_MESSAGE_FORMAT = "Response committed so it could have the wrong status code -> path : %s | exception : %s";
	}

	@Override
	public String getContentType() {
		return MediaType.TEXT_HTML_VALUE;
	}

	@Override
	public void render(@Nullable final Map<String, ?> model,
			final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		if (response.isCommitted()) {
			log.error(extractErrorMessage(model)); return;
		}

		response.setContentType(getContentType());

		FileCopyUtils.copy(createContents(model), response.getOutputStream());
	}

	private String extractErrorMessage(@Nullable final Map<String, ?> model) {

		if (Objects.isNull(model)) {
			return Constants.DEFAULT_ERROR_MESSAGE + " (Model Is Null)";
		}

		return String.format(ERROR_MESSAGE_FORMAT, model.get("path"), model.get("message"));
	}

	private InputStream createContents(@Nullable final Map<String, ?> model) {

		final StringBuffer values = new StringBuffer();
		final Map<String, ?> contents = Optional.ofNullable(model).orElseGet(Maps::newHashMap);
		final Matcher matcher = createMatchResultWithContents();

		while (matcher.find()) {
			matcher.appendReplacement(values, bindValues(matcher, contents));
		}

		return new ByteArrayInputStream(matcher.appendTail(values).toString().getBytes(StandardCharsets.UTF_8));
	}

	private String bindValues(final MatchResult mr, final Map<String, ?> contents) {
		return String.valueOf(contents.get(ERASE_PATTERN.matcher(mr.group()).replaceAll("")));
	}

	abstract Matcher createMatchResultWithContents();
}
