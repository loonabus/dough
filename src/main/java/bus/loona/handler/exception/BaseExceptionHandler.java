package bus.loona.handler.exception;

import bus.loona.common.BaseResCode;
import bus.loona.domain.base.BaseRes;
import bus.loona.handler.AwsStsHandler;
import com.amazonaws.AmazonServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.util.WebUtils;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
 * started on 2017-05-?? ~
 */

@RestControllerAdvice
@ResponseStatus(HttpStatus.OK)
public class BaseExceptionHandler extends AbstractExceptionHandler<BaseRes<?, ?>, AmazonServiceException> {

	private final AwsStsHandler handler;

	private final MappingJackson2JsonView view;
	private final MessageSourceAccessor accessor;

	@Autowired
	public BaseExceptionHandler(final AwsStsHandler handler,
								final MappingJackson2JsonView view, final MessageSourceAccessor accessor) {
		super(AmazonServiceException.class);
		this.handler = handler; this.view = view; this.accessor = accessor;
	}

	@Override
	View obtainView() {
		return view;
	}

	@Override
	BaseRes<?, ?> resolveException(final Exception e, final WebRequest wr, final BaseResCode code) {

		final String message = resolveExceptionMessage(e, code);

		writeLog(e, code, message);

		if (BaseResCode.oneOfServerSideError(code)) {
			wr.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, e, SCOPE_REQUEST);
		}

		return BaseRes.from(code, message);
	}

	@Nullable @Override
	String resolveExtraMessage(final AmazonServiceException source) {
		return handler.decodeFailureMessage(source.getErrorMessage());
	}

	@Override
	MessageSourceAccessor retrieveAccessor() {
		return accessor;
	}
}
