package bus.loona.handler;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.data.redis.connection.MessageListener
 * started on 2017-05-?? ~
 */

public interface RedisEventHandler {
	void handleEvent(final String source);
}
