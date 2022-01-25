package bus.loona.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see AbstractHtmlView
 * started on 2017-05-?? ~
 */

public class TestHtmlView extends AbstractHtmlView {

	private static final Pattern TARGET_FIELDS = Pattern.compile("#code#", Pattern.LITERAL);

	@Override
	Matcher createMatchResultWithContents() {
		return TARGET_FIELDS.matcher(HtmlTemplates.retrieveTestViewHtmlTemplate());
	}
}
