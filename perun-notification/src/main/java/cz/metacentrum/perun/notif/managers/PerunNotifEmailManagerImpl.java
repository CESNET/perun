package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.notif.dto.PerunNotifEmailMessageToSendDto;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("perunNotifEmailManager")
public class PerunNotifEmailManagerImpl implements PerunNotifEmailManager {

  private static final Logger logger = LoggerFactory.getLogger(PerunNotifEmailManager.class);
  private static final Logger failedEmailLogger = LoggerFactory.getLogger("failedEmailLogger");
  private static final Logger sendMessagesLogger = LoggerFactory.getLogger("sendMessages");
  private boolean sendMessages;

  @PostConstruct
  public void init() throws Exception {
    this.sendMessages = BeansUtils.getCoreConfig().getNotifSendMessages();
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
