package io.doe.common;

import io.doe.domain.BaseRes;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.transaction.TransactionException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.WebUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
 * started on 2017-05-?? ~
 */

@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {

	private static final String TYPE_MISMATCH_CODE = "TypeMismatch";
	private static final String BAD_REQUEST_MESSAGE = "Invalid Parameters Found";

	private final MessageSourceAccessor accessor;

	@Autowired
	public BaseExceptionHandler(final MessageSourceAccessor accessor) {
		this.accessor = accessor;
	}

	@Nullable
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({BindException.class,
			ConstraintViolationException.class, HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
			ServletRequestBindingException.class, TypeMismatchException.class})
	public BaseRes<Void> processBadRequestError(final Exception e, final WebRequest wr) {

		switch (e) {
			case BindException ee -> { return processBindException(ee, wr); }
			case ConstraintViolationException ee -> { return processConstraintViolationException(ee, wr); }
			case HttpMessageNotReadableException ee -> { return processHttpMessageNotReadableException(ee, wr); }
			case MissingServletRequestParameterException ee -> { return processMissingServletRequestParameterException(ee, wr); }
			case MissingServletRequestPartException ee -> { return processMissingServletRequestPartException(ee, wr); }
			case ServletRequestBindingException ee -> { return processServletRequestBindingException(ee, wr); }
			case TypeMismatchException ee -> { return processTypeMismatchException(ee, wr); }
			default -> { return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase())); }
		}
	}

	@Nullable
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({HandlerMethodValidationException.class, MissingPathVariableException.class})
	public BaseRes<Void> processOtherBadRequestError(final Exception e, final WebRequest wr) {

		if (e instanceof MissingPathVariableException ee) {
			return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, "URI path variable " + ee.getVariableName() + " is not present"));
		}

		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase()));
	}

	@Nullable
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
	public BaseRes<Void> processNotFoundError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.NOT_FOUND, e.getMessage()));
	}

	@Nullable
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public BaseRes<Void> processMethodNotAllowedError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage()));
	}

	@Nullable
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public BaseRes<Void> processNotAcceptableError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.NOT_ACCEPTABLE, e.getMessage() + concatMediaTypes((HttpMediaTypeException)e)));
	}

	@Nullable
	@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public BaseRes<Void> processPayloadTooLargeError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum upload size exceeded (" + ((MaxUploadSizeExceededException)e).getMaxUploadSize() + ")"));
	}

	@Nullable
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public BaseRes<Void> processUnsupportedMediaTypeError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage() + concatMediaTypes((HttpMediaTypeException)e)));
	}

	@Nullable
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({ConversionNotSupportedException.class,
			HttpMessageNotWritableException.class, IllegalArgumentException.class, MethodValidationException.class})
	public BaseRes<Void> processInternalServerError(final Exception e, final WebRequest wr) {

		switch (e) {
			case ConversionNotSupportedException ee -> { return processConversionNotSupportedException(ee, wr); }
			case HttpMessageNotWritableException ee -> { return processHttpMessageNotWritableException(ee, wr); }
			case IllegalArgumentException ee -> { return processIllegalArgumentException(ee, wr); }
			case MethodValidationException ee -> { return processMethodValidationException(ee, wr); }
			default -> { return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())); }
		}
	}

	@Nullable
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(BaseException.class)
	public BaseRes<Void> processUserDefinedError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
	}

	@Nullable
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({DataAccessException.class, TransactionException.class})
	public BaseRes<Void> processDatabaseError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
	}

	@Nullable
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public BaseRes<Void> processServiceUnavailableError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.SERVICE_UNAVAILABLE, "Asynchronous request time out"));
	}

	@Nullable
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public BaseRes<Void> processRemainderError(final Exception e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
	}

	@Nullable
	private BaseRes<Void> processBindException(final BindException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, createBindExceptionMessage(e)));
	}

	private String createBindExceptionMessage(final BindException e) {

		if (e.getBindingResult().hasErrors()) {
			final ObjectError error = e.getBindingResult().getAllErrors().getFirst();

			if (error instanceof FieldError f) {
				if (TYPE_MISMATCH_CODE.equalsIgnoreCase(error.getCode())) {
					return retrieveMessage(TYPE_MISMATCH_CODE, f.getField(), Optional.ofNullable(f.getRejectedValue()).map(String::valueOf).orElse(""));
				}

				return f.getField() + Optional.ofNullable(f.getDefaultMessage()).map(v -> " " + v).orElse("");
			}

			return Optional.ofNullable(error.getDefaultMessage()).orElse(BAD_REQUEST_MESSAGE);
		}

		return BAD_REQUEST_MESSAGE;
	}

	@Nullable
	private BaseRes<Void> processConstraintViolationException(final ConstraintViolationException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, CollectionUtils.isEmpty(e.getConstraintViolations()) ?
				BAD_REQUEST_MESSAGE : e.getConstraintViolations().stream().findFirst().map(v -> v.getPropertyPath() + " " + v.getMessage()).orElse(BAD_REQUEST_MESSAGE)));
	}

	@Nullable
	private BaseRes<Void> processHttpMessageNotReadableException(final HttpMessageNotReadableException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, "Failed to read request"));
	}

	@Nullable
	private BaseRes<Void> processMissingServletRequestParameterException(final MissingServletRequestParameterException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, retrieveMessage(e.getClass().getSimpleName(), e.getParameterType(), e.getParameterName())));
	}

	@Nullable
	private BaseRes<Void> processMissingServletRequestPartException(final MissingServletRequestPartException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, retrieveMessage(e.getClass().getSimpleName(), e.getRequestPartName())));
	}

	@Nullable
	private BaseRes<Void> processServletRequestBindingException(final ServletRequestBindingException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST, "Unrecoverable fatal binding exception occurred"));
	}

	@Nullable
	private BaseRes<Void> processTypeMismatchException(final TypeMismatchException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.BAD_REQUEST,retrieveMessage(
				e.getClass().getSimpleName(), e.getRequiredType(), (e instanceof MethodArgumentTypeMismatchException ee) ? ee.getName() : e.getPropertyName(), e.getValue())));
	}

	private String concatMediaTypes(final HttpMediaTypeException e) {
		return e.getSupportedMediaTypes().stream().map(MimeType::getType).collect(Collectors.joining(",", " supported types are ", ""));
	}

	@Nullable
	private BaseRes<Void> processConversionNotSupportedException(final TypeMismatchException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR,
				retrieveMessage(e.getClass().getSimpleName(), e.getRequiredType(), e.getPropertyName(), e.getValue())));
	}

	@Nullable
	private BaseRes<Void> processHttpMessageNotWritableException(final HttpMessageNotWritableException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to write request"));
	}

	@Nullable
	private BaseRes<Void> processIllegalArgumentException(final IllegalArgumentException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
	}

	@Nullable
	private BaseRes<Void> processMethodValidationException(final MethodValidationException e, final WebRequest wr) {
		return response(e, wr, BaseRes.from(HttpStatus.INTERNAL_SERVER_ERROR, "Method validation failed"));
	}

	private void printError(final Exception e, final BaseRes<Void> res) {

		log.debug("status code : {} | message : {}", res.getStatus(), res.getMessage());

		if (HttpStatus.BAD_REQUEST.value() == res.getStatus()) {
			log.debug("", e); return;
		}
		if (HttpStatus.Series.CLIENT_ERROR == HttpStatus.Series.resolve(res.getStatus())) {
			log.info("", e); return;
		}
		if (e instanceof BaseException && Objects.nonNull(e.getCause())) {
			log.warn("", e.getCause()); return;
		}

		log.warn("", e);
	}

	@Nullable
	private BaseRes<Void> response(final Exception e, final WebRequest wr, final BaseRes<Void> res) {

		printError(e, res);

		if (wr instanceof ServletWebRequest swr && Objects.nonNull(swr.getResponse()) && swr.getResponse().isCommitted()) {
			log.warn("Response already committed. Ignoring : {}", e.getClass().getName());
			return null;
		}

		if (HttpStatus.INTERNAL_SERVER_ERROR.value() == res.getStatus()) {
			wr.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, e, SCOPE_REQUEST);
		}

		return res;
	}

	private String retrieveMessage(final String prop, @Nullable final Object... arr) {
		return accessor.getMessage(Constants.BASE_PACKAGE + ".validation.exceptions." + prop + ".message", arr, LocaleContextHolder.getLocale());
	}
}
