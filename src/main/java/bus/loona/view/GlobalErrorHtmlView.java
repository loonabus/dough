package bus.loona.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see AbstractHtmlView
 * started on 2017-05-?? ~
 */

public class GlobalErrorHtmlView extends AbstractHtmlView {

	private static final Pattern TARGET_FIELDS;

	static {
		TARGET_FIELDS = Pattern.compile("(#status#|#error#|#message#|#trace#)");
	}

	@Override
	Matcher createMatchResultWithContents() {
		return TARGET_FIELDS.matcher(HtmlTemplates.retrieveGlobalErrorHtmlTemplate());
	}
}
