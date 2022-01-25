package bus.loona.service;

import bus.loona.domain.ServiceReq;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see UserService
 * started on 2017-05-?? ~
 */

public interface UserService<T> {

	List<T> search();
	@Nullable T create(final ServiceReq.UserPrm prm);
}
