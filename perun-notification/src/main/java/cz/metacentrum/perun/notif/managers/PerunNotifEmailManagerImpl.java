package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.notif.dto.PerunNotifEmailMessageToSendDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

@Service("perunNotifEmailManager")
public class PerunNotifEmailManagerImpl implements PerunNotifEmailManager {

	@Autowired
	private Properties propertiesBean;

	private boolean sendMessages;

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifEmailManager.class);

	private static final Logger failedEmailLogger = LoggerFactory.getLogger("failedEmailLogger");
	private static final Logger sendMessagesLogger = LoggerFactory.getLogger("sendMessages");

	@PostConstruct
	public void init() throws Exception {
		String sendMessages_s = (String) propertiesBean.get("notif.sendMessages");
		this.sendMessages = sendMessages_s != null && sendMessages_s.equals("true");
	}

	@Override
	public void sendMessages(List<PerunNotifEmailMessageToSendDto> list) {
		if (!sendMessages) {
			return;
		}
		try {
			JavaMailSender mailSender = BeansUtils.getDefaultMailSender();
			for (PerunNotifEmailMessageToSendDto dto : list) {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(dto.getSender());
				message.setTo(dto.getReceiver());
				message.setSubject(dto.getSubject());
				message.setText(dto.getMessage());
				try {
					// log to normal logger
					logger.info("Sending email to: {}, from: {}, subject: {}",
						message.getTo(), message.getFrom(), message.getSubject()
					);
					//send message over SMTP
					mailSender.send(message);
					// log successful sending to sendMessages logger
					sendMessagesLogger.info("Email sent  to: {}, from: {}, subject: {}, text: {}",
						message.getTo(), message.getFrom(), message.getSubject(), dto.getMessage()
					);
				} catch (MailException ex) {
					failedEmailLogger.error("cannot send email", ex);
					failedEmailLogger.error("{}", message);
				}
			}
		} catch (Exception ex) {
			failedEmailLogger.error("sending messages failed", ex);
		}
	}

}
