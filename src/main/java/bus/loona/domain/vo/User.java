package bus.loona.domain.vo;

import bus.loona.domain.ServiceReq;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Getter @ToString
public final class User {

	@Nullable private final Long userId;
	@Nullable private final String userName;
	@Nullable private final LocalDateTime createDt;

	public User(@Nullable final Long userId, @Nullable final String userName, @Nullable final LocalDateTime createDt) {
		this.userId = userId; this.userName = userName; this.createDt = createDt;
	}

	public static User from(final ServiceReq.UserPrm prm) {
		return new User(null, prm.getName(), null);
	}
}

