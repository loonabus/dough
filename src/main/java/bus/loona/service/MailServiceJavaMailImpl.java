package bus.loona.service;

import bus.loona.common.BaseException;
import bus.loona.domain.ServiceReq;
import bus.loona.service.base.BaseService;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see org.springframework.mail.javamail.JavaMailSender
 * started on 2017-05-?? ~
 */

public class MailServiceJavaMailImpl extends AbstractMailService implements BaseService {

	private final MailProperties props;
	private final JavaMailSender sender;
	private final MessageSourceAccessor accessor;

	public MailServiceJavaMailImpl(final MailProperties props,
								   final JavaMailSender sender, final MessageSourceAccessor accessor) {
		this.props = props; this.sender = sender; this.accessor = accessor;
	}

	@Nullable @Override
	Void executeMailProvider(final ServiceReq.MailPrm prm) {

		try {
			sender.send(createRequest(prm)); return null;
		} catch (final MailException me) {
			throw new BaseException(retrieveMessage("MailSendException", prm.concatenate()), me);
		}
	}

	@Override
	public MessageSourceAccessor retrieveAccessor() {
		return accessor;
	}

	private SimpleMailMessage createRequest(final ServiceReq.MailPrm prm) {

		final SimpleMailMessage message = new SimpleMailMessage();

		message.setSubject(prm.getTitle());
		message.setText(prm.getContents());
		message.setFrom(props.getUsername());
		message.setTo(prm.getRecipients().toArray(new String[0]));

		return message;
	}
}
