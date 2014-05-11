package cz.metacentrum.perun.notif.mail;

import javax.mail.internet.MimeMessage;

/**
 * Callback interface for the preparation of JavaMail MIME messages.
 *
 * <p>
 * The corresponding <code>send</code> methods of
 * {@link org.springframework.mail.javamail.JavaMailSender} will take care of
 * the actual creation of a {@link MimeMessage} instance, and of proper
 * exception conversion.
 *
 * <p>
 * It is often convenient to use a
 * {@link org.springframework.mail.javamail.MimeMessageHelper} for populating
 * the passed-in MimeMessage, in particular when working with attachments or
 * special character encodings. See
 * {@link org.springframework.mail.javamail.MimeMessageHelper MimeMessageHelper's javadoc}
 * for an example.
 *
 * @author Juergen Hoeller
 * @since 07.10.2003
 * @see org.springframework.mail.javamail.MimeMessageHelper
 */
public interface MimeMessagePreparator {

	/**
	 * Prepare the given new MimeMessage instance.
	 *
	 * @param mimeMessage the message to prepare
	 * @throws javax.mail.MessagingException passing any exceptions thrown
	 * by MimeMessage methods through for automatic conversion to the
	 * EmailException hierarchy
	 * @throws java.io.IOException passing any exceptions thrown by
	 * MimeMessage methods through for automatic conversion to the
	 * EmailException hierarchy
	 * @throws Exception if mail preparation failed, for example when a
	 * Velocity template cannot be rendered for the mail text
	 */
	void prepare(MimeMessage mimeMessage) throws Exception;

}
