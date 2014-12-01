package cz.metacentrum.perun.notif.mail;

import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Message preparator for preparing messages in utf-8
 *
 * inspired from
 * http://opensource.atlassian.com/confluence/spring/display/DISC/Sending+FreeMarker-based+multipart+email+with+Spring
 */
public class MessagePreparator implements MimeMessagePreparator {

	private static final Logger logger = LoggerFactory.getLogger(MessagePreparator.class);

	/**
	 * Email type
	 */
	public static enum EmailType {

		PLAIN, HTML;
	}

	private List<String> to = new ArrayList<String>();
	private List<String> cc = new ArrayList<String>();
	private List<String> bcc = new ArrayList<String>();
	private String content = null;
	private Configuration configuration = null;
	private String from = "";
	private String fromText = "";
	private String subject = "";
	private List<String> fileNames = new ArrayList<String>();
	private EmailType mailType = null;

	/**
	 * Constructor
	 *
	 * @param from
	 * @param subject
	 * @param content
	 * @param mailType
	 */
	public MessagePreparator(String from, String fromText, String subject, String content, EmailType mailType) {
		this.content = content;
		this.from = from;
		this.fromText = fromText;
		this.subject = subject;
		this.mailType = mailType;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.mail.javamail.MimeMessagePreparator#prepare(javax.mail.internet.MimeMessage)
	 */
	public void prepare(MimeMessage mimeMessage) throws Exception {
		MimeMultipart mpRoot = new MimeMultipart("mixed");
		Multipart mp = new MimeMultipart("alternative");

		// Create a body part to house the multipart/alternative Part
		MimeBodyPart contentPartRoot = new MimeBodyPart();
		contentPartRoot.setContent(mp);

		// Add the root body part to the root multipart
		mpRoot.addBodyPart(contentPartRoot);

		// adding recipients, cc and bcc
		if (getTo() != null) {
			for (String to : getTo()) {
				mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}
		}

		if (getCc() != null) {
			for (String cc : getCc()) {
				mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
			}
		}

		if (getBcc() != null) {
			for (String bcc : getBcc()) {
				mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
			}
		}

		// adding from and subject
		mimeMessage.setFrom(new InternetAddress(getFrom(), getFromText()));
		mimeMessage.setSubject(getSubject());

		if (getEmailType().equals(EmailType.HTML)) {
			mp.addBodyPart(createHtmlMessage());
		} else if (getEmailType().equals(EmailType.PLAIN)) {
			mp.addBodyPart(createTextMessage());
		}

		// Create an "ATTACHMENT" - we must put it to mpRoot(mixed content)
		if (getFileNames() != null) {
			for (String filename : getFileNames()) {
				mpRoot.addBodyPart(createAttachment(filename));
			}
		}

		mimeMessage.setContent(mpRoot);

		logger.debug("Message is prepared to send");
	}

	/**
	 * Creates plain text message from freemarker template
	 *
	 * @return
	 * @throws Exception
	 */
	private BodyPart createTextMessage() throws Exception {
		BodyPart textPart = new MimeBodyPart();

		textPart.setDataHandler(createDataHandler(content.getBytes("utf-8"), "text/plain;charset=utf-8"));

		logger.debug("TEXT MESSAGE CREATED ; content: " + content);

		return textPart;
	}

	/**
	 * Creates HTML message from freemarker template
	 *
	 * @return
	 * @throws Exception
	 */
	private BodyPart createHtmlMessage() throws Exception {
		Multipart htmlContent = new MimeMultipart("related");
		BodyPart htmlPage = new MimeBodyPart();

		htmlPage.setDataHandler(createDataHandler(content.getBytes("utf-8"), "text/html;charset=utf-8"));

		htmlContent.addBodyPart(htmlPage);
		BodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(htmlContent);

		logger.debug("HTML MESSAGE CREATED ; content: " + content);

		return htmlPart;
	}

	/**
	 * Creates attachment from filename
	 *
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	private BodyPart createAttachment(String filename) throws Exception {
		BodyPart attachBodypart = new MimeBodyPart();
		File file = new File(filename);
		FileDataSource fds = new FileDataSource(file);
		DataHandler dh = new DataHandler(fds);

		attachBodypart.setFileName(file.getName());
		attachBodypart.setDisposition(Part.ATTACHMENT);
		attachBodypart.setDescription("Attached file: " + file.getName());
		attachBodypart.setDataHandler(dh);

		logger.debug("ATTACHMENT ADDED ; filename: " + filename);

		return attachBodypart;
	}

	/**
	 * Creates datahandler for both plain text and HTML messages
	 *
	 * @param stringBytes
	 * @param contentType
	 * @return
	 */
	private DataHandler createDataHandler(final byte[] stringBytes, final String contentType) {
		return new DataHandler(new DataSource() {

			public InputStream getInputStream() throws IOException {
				return new BufferedInputStream(new ByteArrayInputStream(stringBytes));
			}

			public OutputStream getOutputStream() throws IOException {
				throw new IOException("Read-only data");
			}

			public String getContentType() {
				return contentType;
			}

			public String getName() {
				return "main";
			}
		});
	}

	/**
	 * Add atachment filename
	 *
	 * @param filename
	 */
	public void addFileName(String filename) {
		this.fileNames.add(filename);
	}

	/**
	 * Add recipient bcc
	 *
	 * @param recipientBcc
	 */
	public void addRecipientBcc(String recipientBcc) {
		this.bcc.add(recipientBcc);
	}

	/**
	 * Add recipient cc
	 *
	 * @param recipientCc
	 */
	public void addRecipientCc(String recipientCc) {
		this.cc.add(recipientCc);
	}

	/**
	 * Add recipient to
	 *
	 * @param recipientTo
	 */
	public void addRecipientTo(String recipientTo) {
		this.to.add(recipientTo);
	}

	/**
	 * Clear recipient bcc
	 */
	public void clearRecipientBcc() {
		this.bcc.clear();
	}

	/**
	 * Clear recipient cc
	 */
	public void clearRecipientCc() {
		this.cc.clear();
	}

	/**
	 * Clear filename
	 */
	public void clearRecipientFileName() {
		this.fileNames.clear();
	}

	/**
	 * Clear recipient to
	 */
	public void clearRecipientTo() {
		this.to.clear();
	}

	public List<String> getTo() {
		return to;
	}

	public List<String> getCc() {
		return cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String getFrom() {
		return from;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public EmailType getEmailType() {
		return mailType;
	}

	public String getFromText() {
		return fromText;
	}

	public void setFromText(String fromText) {
		this.fromText = fromText;
	}
}
