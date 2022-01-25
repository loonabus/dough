package bus.loona.service;

import bus.loona.domain.ServiceReq;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see AmazonSimpleEmailService
 * started on 2017-05-?? ~
 */

@Slf4j
public abstract class AbstractMailService implements MailService {

	private static final Function<Throwable, Void> FN;

	static {
		FN = e -> { log.warn("Send Email Failed -> {}", e.getMessage(), e); return null; };
	}

	@Async @Override
	public Future<Void> send(final ServiceReq.MailPrm prm) {
		return CompletableFuture.completedFuture(executeMailProvider(prm)).exceptionally(FN);
	}

	@Nullable
	abstract Void executeMailProvider(final ServiceReq.MailPrm prm);

	Logger obtainLogger() { return log; }
}
