package bus.loona.domain.base;

import bus.loona.common.BaseResCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.beans.ConstructorProperties;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see BaseRes
 * started on 2017-05-?? ~
 */

@Getter @ToString
public final class BaseRes<T, U> {

	private final Integer resCode;
	private final String resMessage;
	@Nullable private final T resData;
	@Nullable private final U reqData;

	@ConstructorProperties({"resCode","resMessage","resData","reqData"})
	private BaseRes(final Integer resCode, final String resMessage, @Nullable final T resData, @Nullable final U reqData) {
		this.resCode = resCode; this.resMessage = resMessage; this.resData = resData; this.reqData = reqData;
	}

	public static <T, U> BaseRes<T, U> from(final BaseResCode code, final String message) {
		return new BaseRes<>(code.getValue(), message, null, null);
	}

	public static <T, U> BaseRes<T, U> from(final BaseResCode code, final String message, final T resData, @Nullable final U reqData) {
		return new BaseRes<>(code.getValue(), message, resData, reqData);
	}

	public static <T, U> BaseRes<T, U> success(@Nullable final T resData) {
		return new BaseRes<>(BaseResCode.OK.getValue(), BaseResCode.OK.getReason(), resData, null);
	}
}
