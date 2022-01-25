package bus.loona.handler.exception;

import bus.loona.common.BaseException;
import bus.loona.common.BaseResCode;
import bus.loona.common.Constants;
import com.amazonaws.SdkBaseException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
 * started on 2017-05-?? ~
 */

@Slf4j
public abstract class AbstractExceptionHandler<T, U extends Exception> {

	private static final String CRLF_REPLACEMENT = "_CRLF_";
	private static final Pattern CRLF_EXPR = Pattern.compile("[\r\n]");

	private final Class<? extends U> exType;

	protected AbstractExceptionHandler(final Class<? extends U> exType) {
		this.exType = exType;
	}

	private static final String BAD_REQUEST_MESSAGE = "Invalid Parameters Found";

	@ExceptionHandler({BindException.class, ConstraintViolationException.class,
			HttpMessageNotReadableException.class,
			MethodArgumentNotValidException.class,
			MissingServletRequestParameterException.class,
			MissingServletRequestPartException.class,
			ServletRequestBindingException.class, TypeMismatchException.class})
	public T resolveBadRequest(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.BAD_REQUEST);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public T resolveResourceNotFound(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.NOT_FOUND);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public T resolveMethodNotAllowed(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ModelAndView resolveMediaNotAcceptable(final Exception e, final WebRequest wr) {
		return new ModelAndView(obtainView(), "res", resolveException(e, wr, BaseResCode.NOT_ACCEPTABLE));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public T resolveUnsupportedMedia(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.UNSUPPORTED_MEDIA_TYPE);
	}

	@ExceptionHandler({HttpMessageNotWritableException.class, IllegalArgumentException.class})
	public T resolveInternalServerError(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.INTERNAL_SERVER_ERROR);
	}

	@Nullable
	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public T resolveServiceUnavailable(final Exception e, final WebRequest wr) {

		if (wr instanceof ServletWebRequest) {
			final NativeWebRequest nwr = (NativeWebRequest)wr;
			final HttpServletResponse res = nwr.getNativeResponse(HttpServletResponse.class);

			if (Objects.nonNull(res) && res.isCommitted()) {
				final HttpServletRequest sr = nwr.getNativeRequest(HttpServletRequest.class);
				if (Objects.nonNull(sr)) {
					log.error("Async Method Timeout {}({})", eraseCRLF(sr.getRequestURI()), eraseCRLF(sr.getMethod()));
				}

				return null;
			}
		}

		return resolveException(e, wr, BaseResCode.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(BaseException.class)
	public T resolveDomainException(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.DOMAIN_EXCEPTION);
	}

	@ExceptionHandler(SdkBaseException.class)
	public T resolveAmazonException(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.AWS_EXCEPTION);
	}

	@ExceptionHandler({DataAccessException.class, TransactionException.class})
	public T resolvePersistenceException(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.PERSISTENCE_EXCEPTION);
	}

	@ExceptionHandler(Exception.class)
	public T resolveRemainderException(final Exception e, final WebRequest wr) {
		return resolveException(e, wr, BaseResCode.GENERAL_OTHER_EXCEPTION);
	}

	abstract View obtainView();

	abstract T resolveException(final Exception e, final WebRequest wr, final BaseResCode code);

	@Nullable abstract String resolveExtraMessage(final U source);

	abstract MessageSourceAccessor retrieveAccessor();

	void writeLog(final Exception e, final BaseResCode code, final String message) {

		if (BaseResCode.BAD_REQUEST == code) {
			log.debug("{}", message);
		} else if (exType.isInstance(e)) {
			log.warn("", e);
			final String extraMessage = resolveExtraMessage(exType.cast(e));
			if (Objects.nonNull(extraMessage)) {
				log.warn("{} -> {}", e.getClass().getSimpleName(), extraMessage);
			}
		} else if (e instanceof BaseException) {
			log.info("{} -> {}", e.getClass().getSimpleName(), message);
			if (Objects.nonNull(e.getCause())) {
				log.warn("", e.getCause());
			}
		} else {
			log.warn("", e);
		}
	}

	String resolveExceptionMessage(final Exception e, final BaseResCode code) {

		String message = "";

		if (BaseResCode.BAD_REQUEST == code) {
			message = resolveBadRequestMessage(e);
		} else if (e instanceof BaseException) {
			message = e.getMessage();
		} else if (e instanceof HttpMessageNotWritableException) {
			message = "Response Message Conversion Failed";
		} else if (e instanceof HttpMediaTypeNotSupportedException) {
			message = ((HttpMediaTypeNotSupportedException)e).getSupportedMediaTypes()
					.stream().map(String::valueOf).collect(Collectors.joining(",", "Supported: ", ""));
		} else if (e instanceof HttpRequestMethodNotSupportedException) {
			message = Optional.ofNullable(((HttpRequestMethodNotSupportedException)e).getSupportedHttpMethods())
					.orElseGet(ImmutableSet::of).stream().map(HttpMethod::name).collect(Collectors.joining(",", "Supported: ", ""));
		}

		if (Strings.isNullOrEmpty(message)) {
			return code.hasReasonPhrase() ? code.getReason() : Constants.DEFAULT_ERROR_MESSAGE;
		}

		return code.hasReasonPhrase() ? (code.getReason() + "(" + message + ")") : message;
	}

	private String resolveBadRequestMessage(final Exception e) {

		if (e instanceof BindException) {
			return extractError(((BindException)e));
		}
		if (e instanceof ConstraintViolationException) {
			return extractError((ConstraintViolationException)e);
		}

		if (e instanceof MethodArgumentTypeMismatchException) {
			final Class<?> required = ((MethodArgumentTypeMismatchException)e).getRequiredType();
			final String requiredName = required == null ? "N/A" : ClassUtils.getShortName(required);

			return retrieveFromMessageSource("MethodArgumentTypeMismatch",
					((MethodArgumentTypeMismatchException)e).getName(), requiredName);
		}

		if (e instanceof MissingServletRequestPartException) {
			return retrieveFromMessageSource("MissingServletRequestPart",
					((MissingServletRequestPartException)e).getRequestPartName());
		}
		if (e instanceof MissingServletRequestParameterException) {
			return retrieveFromMessageSource("MissingServletRequestParameter",
					((MissingServletRequestParameterException)e).getParameterType(),
					((MissingServletRequestParameterException)e).getParameterName());
		}

		if (e instanceof HttpMessageNotReadableException) {
			return "Unreadable Request Data Format";
		}

		return e.getMessage();
	}

	@Nullable
	private String unwrapErrorMessage(final ObjectError error) {

		if (error instanceof FieldError && Objects.equals("typeMismatch", error.getCode())) {
			return retrieveFromMessageSource("FieldTypeMismatchErrorCode",
					((FieldError)error).getField(), String.valueOf(((FieldError)error).getRejectedValue()));
		}

		return error.getDefaultMessage();
	}

	private String extractFirst(final Stream<String> source) {
		return Optional.of(source.filter(StringUtils::hasText).limit(1)
				.collect(Collectors.joining())).filter(StringUtils::hasText).orElse(BAD_REQUEST_MESSAGE);
	}

	private String extractError(final BindException e) {
		return Optional.of(e.getBindingResult().getAllErrors()).map(r ->
				extractFirst(r.stream().map(this::unwrapErrorMessage))).orElse(BAD_REQUEST_MESSAGE);
	}

	private String extractError(final ConstraintViolationException e) {
		return Optional.ofNullable(e.getConstraintViolations()).map(v ->
				extractFirst(v.stream().map(ConstraintViolation::getMessage))).orElse(BAD_REQUEST_MESSAGE);
	}

	private String retrieveFromMessageSource(final String prop, @Nullable final Object... arr) {
		return retrieveAccessor().getMessage(Constants.VALIDATIONS_PREFIX + prop + ".message", arr, LocaleContextHolder.getLocale());
	}

	private String eraseCRLF(final String source) {
		return CRLF_EXPR.matcher(source).replaceAll(CRLF_REPLACEMENT);
	}
}
