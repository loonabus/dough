package bus.loona.view


import bus.loona.common.Constants

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see HtmlTemplates
 * started on 2017-05-?? ~
 */

final class HtmlTemplates {

	static final String retrieveGlobalErrorHtmlTemplate() {
		return GLOBAL_ERROR_HTML
	}

	static final String retrieveTestViewHtmlTemplate() {
		return TEST_VIEW_HTML
	}

	private static final String GLOBAL_ERROR_HTML = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta charset="UTF-8" />
				<title>Error!!</title>
			</head>
			<body>
				<header>Unexpected Error Occurred</header>
				<script type="text/javascript">
					window.onload = function() { 
						console.log('status : #status# (#error#)');
						console.log('message : #message# (#trace#)');
					};
				</script>
			</body>
		</html>"""

	private static final String TEST_VIEW_HTML = """
		<!DOCTYPE html>
		<html>
			<head>
				<meta charset="UTF-8" />
				<title>Test page</title>
			</head>
			<body>
				<header>Test view</header>
				<div style="height: 50px;"><p>Your Input Is #code#</p></div>
			</body>
		</html>"""

	private HtmlTemplates() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE as String)
	}
}