package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.notif.dto.PerunNotifEmailMessageToSendDto;
import cz.metacentrum.perun.notif.mail.Authenticator;
import cz.metacentrum.perun.notif.mail.EmailPreparationException;
import cz.metacentrum.perun.notif.mail.PerunNotifPlainMessage;
import cz.metacentrum.perun.notif.mail.exception.EmailAuthenticationException;
import cz.metacentrum.perun.notif.mail.exception.EmailException;
import cz.metacentrum.perun.notif.mail.exception.EmailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@org.springframework.stereotype.Service("perunNotifEmailManager")
public class PerunNotifEmailManagerImpl implements PerunNotifEmailManager {

	@Autowired
	private Properties propertiesBean;

	/**
	 * The default protocol: 'smtp'
	 */
	public static final String DEFAULT_PROTOCOL = "smtp";
	private String protocol = DEFAULT_PROTOCOL;

	private Session session;
	private String mailSmtpAuth;
	private String username;
	private String password;
	private String smtpHost;
	private int port;
	private String emailFrom;
	private String fromText;
	private boolean sendMessages;
	private boolean startTls;
	private boolean mailDebug;

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifEmailManager.class);

	private static final Logger failedEmailLogger = LoggerFactory.getLogger("failedEmailLogger");
	private static final Logger sendMessagesLogger = LoggerFactory.getLogger("sendMessages");

	@PostConstruct
	public void init() throws Exception {

		System.setProperty("file.encoding", "utf-8");
		System.setProperty("mail.mime.charset", "utf-8");
		System.setProperty("client.encoding", "utf-8");

		// Load properties file notif.properties
		this.mailSmtpAuth = (String) propertiesBean.get("notif.mailSmtpAuth");
		this.username = (String) propertiesBean.get("notif.username");
		this.password = (String) propertiesBean.get("notif.password");
		this.smtpHost = (String) propertiesBean.get("notif.smtpHost");
		try {
			this.port = Integer.parseInt((String) propertiesBean.get("notif.port"));
		} catch (NumberFormatException e) {
			this.port = 25;
		}
		this.emailFrom = (String) propertiesBean.get("notif.emailFrom");
		this.fromText = (String) propertiesBean.get("notif.fromText");
		this.emailFrom = (String) propertiesBean.get("notif.emailFrom");
		String sendMessages_s = (String) propertiesBean.get("notif.sendMessages");
		this.sendMessages = sendMessages_s == null ? false : (sendMessages_s.equals("true") ? true : false);
		String startTls_s = (String) propertiesBean.get("notif.starttls");
		this.startTls = startTls_s == null ? false : (startTls_s.equals("true") ? true : false);
		this.mailDebug = Boolean.valueOf((String) propertiesBean.get("mail.debug"));

		createSession();
	}

	private void createSession() {
		Authenticator authenticator = null;
		if (mailSmtpAuth != null && mailSmtpAuth.equals("true")) {
			authenticator = new Authenticator(username, password);
		} else {
			username = null;
			password = null;
		}

		Properties properties = new Properties();

		properties.setProperty("mail.smtp.submitter", "");
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.setProperty("mail.smtp.port", String.valueOf(port));
		properties.setProperty("mail.smtp.auth", mailSmtpAuth);
		properties.setProperty("mail.smtp.starttls.enable", String.valueOf(startTls));
		properties.setProperty("mail.debug", String.valueOf(mailDebug));

		session = Session.getInstance(properties, authenticator);
	}

	@Override
	public void sendMessages(List<PerunNotifEmailMessageToSendDto> list) {

		List<PerunNotifPlainMessage> emailList = new ArrayList<PerunNotifPlainMessage>();
		for (PerunNotifEmailMessageToSendDto emailMessage : list) {
			PerunNotifPlainMessage message = new PerunNotifPlainMessage(emailMessage.getSender(), null, emailMessage.getSubject(), emailMessage.getMessage());
			message.addRecipientTo(emailMessage.getReceiver());

			emailList.add(message);
		}

		sendEmailsInBatch(emailList);
		logger.info("Message successfully sended.");
	}

	private void sendEmailsInBatch(List<PerunNotifPlainMessage> messageList) {

		if (!sendMessages) {
			return;
		}

		List<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
		// for logging purposes
		List<String> messagesContents = new ArrayList<String>();

		for (PerunNotifPlainMessage emailMessage : messageList) {
			logger.debug("SENDING PLAIN MESSAGE ; to: " + emailMessage.getTo() + " ; cc: " + emailMessage.getCc() + " ; bcc: "
				+ emailMessage.getBcc());

			MimeMessage mimeMessage = createMimeMessage();
			try {
				emailMessage.prepare(mimeMessage);
				mimeMessages.add(mimeMessage);

				messagesContents.add(emailMessage.getContent());
			} catch (Exception ex) {
				failedEmailLogger.error(emailMessage.toString());
				logger.error("Preparing message to send failed.", ex);
			}
		}

		try {
			doSend(mimeMessages.toArray(new MimeMessage[mimeMessages.size()]), messagesContents);
		} catch (EmailSendException ex) {
			Map<Object, Exception> failedMessages = ex.getFailedMessages();
			if (failedMessages != null && !failedMessages.isEmpty()) {
				for (Object key : failedMessages.keySet()) {
					try {
						MimeMessage message = (MimeMessage) key;
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						message.writeTo(out);
						byte[] charData = out.toByteArray();
						String str = new String(charData, Charset.forName("UTF-8"));
						failedEmailLogger.error(str);
					} catch (Exception e) {
						logger.error("Failed to write log about unsended email.", ex);
					}
				}
			}
			logger.error("Sending of the email failed.", ex);
		} catch (Exception ex) {
			throw new EmailPreparationException(ex);
		}
	}

	/**
	 * Actually send the given array of MimeMessages via JavaMail.
	 *
	 * @param mimeMessages MimeMessage objects to send
	 * @throws EmailAuthenticationException in case of authentication
	 * failure
	 * @throws EmailSendException in case of failure when sending a message
	 */
	protected void doSend(MimeMessage[] mimeMessages, List<String> contents) throws EmailException {
		Map<Object, Exception> failedMessages = new LinkedHashMap<Object, Exception>();

		Transport transport;
		try {
			transport = getTransport(getSession());
			transport.connect(smtpHost, port, username, password);
		} catch (AuthenticationFailedException ex) {
			throw new EmailAuthenticationException(ex);
		} catch (MessagingException ex) {
			// Effectively, all messages failed...
			for (int i = 0; i < mimeMessages.length; i++) {
				failedMessages.put(mimeMessages[i], ex);
			}
			throw new EmailSendException("Mail server connection failed", ex, failedMessages);
		}

		try {
			for (int i = 0; i < mimeMessages.length; i++) {
				MimeMessage mimeMessage = mimeMessages[i];
				String content = contents.get(i);
				try {
					if (mimeMessage.getSentDate() == null) {
						mimeMessage.setSentDate(new Date());
					}
					mimeMessage.saveChanges();
					transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());


					sendMessagesLogger.info("Email sent in {} with receivers: "
						+  Arrays.toString(mimeMessage.getRecipients(Message.RecipientType.TO))
						+ " senders: " + Arrays.toString(mimeMessage.getFrom())
						+ " subject: " + mimeMessage.getSubject()
						+ " content: "
						+ content,
						new Date());
				} catch (MessagingException ex) {
					try {
						logger.error("Error during send of email for: {}", mimeMessage.getRecipients(Message.RecipientType.TO), ex);
					} catch (Exception ex2) {
						logger.error("Cannot send email.", ex);
					}
					failedMessages.put(mimeMessage, ex);
				}
			}
		} finally {
			try {
				transport.close();
			} catch (MessagingException ex) {
				if (!failedMessages.isEmpty()) {
					throw new EmailSendException("Failed to close server connection after message failures", ex, failedMessages);
				} else {
					throw new EmailSendException("Failed to close server connection after message sending", ex);
				}
			}
		}

		if (!failedMessages.isEmpty()) {
			throw new EmailSendException(failedMessages);
		}
	}

	/**
	 * Obtain a Transport object from the given JavaMail Session, using the
	 * configured protocol.
	 * <p>
	 * Can be overridden in subclasses, e.g. to return a mock Transport
	 * object.
	 *
	 * @see javax.mail.Session#getTransport(String)
	 * @see #getProtocol()
	 */
	protected Transport getTransport(Session session) throws NoSuchProviderException {
		return session.getTransport(protocol);
	}

	public MimeMessage createMimeMessage() {
		return new MimeMessage(getSession());
	}

	private Session getSession() {

		return session;
	}

	public String getMailSmtpAuth() {
		return mailSmtpAuth;
	}

	public void setMailSmtpAuth(String mailSmtpAuth) {
		this.mailSmtpAuth = mailSmtpAuth;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public String getFromText() {
		return fromText;
	}

	public void setFromText(String fromText) {
		this.fromText = fromText;
	}

	public boolean isSendMessage() {
		return sendMessages;
	}

	public void setSendMessage(boolean sendMessage) {
		this.sendMessages = sendMessage;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
