package cz.metacentrum.perun.notif.mail;

import java.util.List;

/**
 * Interface for email message. Contains TO, CC, BCC
 */
public interface EmailMessage extends MimeMessagePreparator {

	/**
	 * Who we send email TO
	 *
	 * @return
	 */
	List<String> getTo();

	/**
	 * Who we send email in CC
	 *
	 * @return
	 */
	List<String> getCc();

	/**
	 * Who we send email in BCC
	 *
	 * @return
	 */
	List<String> getBcc();
}
