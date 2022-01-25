package bus.loona.redis;

import bus.loona.domain.ServiceReq;

import java.util.List;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisCommandProvider
 * started on 2017-05-?? ~
 */

public interface RedisCommandProvider {

	BaseScCommand createCommand(final ServiceReq.RedisPrm prm);
	BaseRcCommand<List<ServiceReq.IpAddress>> searchCommand(final ServiceReq.RedisPrm prm);
}
