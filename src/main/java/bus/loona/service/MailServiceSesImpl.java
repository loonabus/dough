package bus.loona.service;

import bus.loona.common.BaseException;
import bus.loona.config.BaseProperties;
import bus.loona.domain.ServiceReq;
import bus.loona.service.base.BaseService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see com.amazonaws.services.simpleemail.AmazonSimpleEmailService
 * started on 2017-05-?? ~
 */

@Service
@EnableConfigurationProperties(BaseProperties.AwsSes.class)
public class MailServiceSesImpl extends AbstractMailService implements BaseService {

	private static final Set<Integer> SUCCESS_HTTP_CODES = ImmutableSet.of(HttpStatus.OK.value());

	private final BaseProperties.AwsSes props;
	private final AmazonSimpleEmailService service;
	private final MessageSourceAccessor accessor;

	@Autowired
	public MailServiceSesImpl(final BaseProperties.AwsSes prop,
							  final AmazonSimpleEmailService service, final MessageSourceAccessor accessor) {
		this.props = prop; this.service = service; this.accessor = accessor;
	}

	@Nullable @Override
	Void executeMailProvider(final ServiceReq.MailPrm prm) {

		try {
			final SendEmailResult res = service.sendEmail(createRequest(prm));
			if (!checkStatusIs2xx(res)) {
				throw new BaseException(retrieveMessage("MailSendException", prm.concatenate()));
			}

			return null;
		} catch (final AmazonSimpleEmailServiceException se) {
			throw new BaseException(retrieveMessage("MailSendException", prm.concatenate()), se);
		}
	}

	@Override
	public MessageSourceAccessor retrieveAccessor() {
		return accessor;
	}

	private SendEmailRequest createRequest(final ServiceReq.MailPrm prm) {
		return new SendEmailRequest().withSource(props.getSender())
				.withDestination(new Destination().withToAddresses(prm.getRecipients())).withMessage(createMessage(prm));
	}

	private Content createContent(final String source) {
		return new Content(source).withCharset(StandardCharsets.UTF_8.name());
	}

	private Message createMessage(final ServiceReq.MailPrm prm) {
		return new Message().withSubject(createContent(prm.getTitle())).withBody(new Body().withText(createContent(prm.getContents())));
	}

	private boolean checkStatusIs2xx(final SendEmailResult res) {
		return SUCCESS_HTTP_CODES.contains(res.getSdkHttpMetadata().getHttpStatusCode());
	}
}
