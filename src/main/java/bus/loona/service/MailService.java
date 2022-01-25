package bus.loona.service;

import bus.loona.domain.ServiceReq;

import java.util.concurrent.Future;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.mail.javamail.JavaMailSender
 * @see com.amazonaws.services.simpleemail.AmazonSimpleEmailService
 * started on 2017-05-?? ~
 */

public interface MailService {
	Future<Void> send(final ServiceReq.MailPrm prm);
}
