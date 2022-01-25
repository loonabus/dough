package bus.loona.domain;

import bus.loona.common.Constants;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see ServiceRes
 * started on 2017-05-?? ~
 */

public final class ServiceRes {

	private ServiceRes() {
		throw new UnsupportedOperationException(Constants.UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE);
	}

	@Getter @ToString
	public static final class Res {

		private final Collection<ServiceReq.IpAddress> value;

		@Builder @Jacksonized
		private Res(final Collection<ServiceReq.IpAddress> value) { this.value = ImmutableList.copyOf(value); }

	}
}
