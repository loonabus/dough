package io.doe.service.base;

import io.doe.common.Constants;
import jakarta.annotation.Nullable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see BaseService
 * started on 2017-05-?? ~
 */

public interface BaseService {

	default String retrieveMessageFrom(final String k, @Nullable final Object... arr) {
		return retrieveAccessor().getMessage(Constants.BASE_PACKAGE + ".service." + k, arr, LocaleContextHolder.getLocale());
	}

	MessageSourceAccessor retrieveAccessor();
}
