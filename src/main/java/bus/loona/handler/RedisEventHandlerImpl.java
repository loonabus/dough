package bus.loona.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see RedisEventHandlerImpl
 * started on 2017-05-?? ~
 */

@Slf4j @Component
public class RedisEventHandlerImpl implements RedisEventHandler {

	@Override
	public void handleEvent(final String source) {
		log.info("Redis Event Message Received -> {}", source);
	}
}
