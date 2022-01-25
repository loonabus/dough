package bus.loona.service.base;

import bus.loona.common.Constants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see BaseService
 * started on 2017-05-?? ~
 */

public interface BaseService {

	default String retrieveMessage(final String prop, @Nullable final Object... arr) {
		return retrieveAccessor().getMessage(Constants.DOMAIN_PREFIX + prop + ".message", arr, LocaleContextHolder.getLocale());
	}

	MessageSourceAccessor retrieveAccessor();
}
