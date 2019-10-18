package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thrown when the membership could not be extended. The reason is sent as a parameter of the exception.
 *
 * @author Michal Prochazka
 */
public class ExtendMembershipException extends PerunException {

	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(ExtendMembershipException.class);
	private Reason reason;
	private String expirationDate;

	/**
	 * Constructor with the reason and the message
	 * @param reason reason why the membership could not be extended
	 * @param message message with details about the cause
	 */
	public ExtendMembershipException(Reason reason, String message) {
		super(message);

		this.reason = reason;

		log.error("Internal Error Exception:", this);
	}

	/**
	 * Constructor with the reason, expiration date and the message
	 * @param reason reason why the membership could not be extended
	 * @param expirationDate expiration date of the membership
	 * @param message message with details about the cause
	 */
	public ExtendMembershipException(Reason reason, String expirationDate, String message) {
		super(message);

		this.expirationDate = expirationDate;
		this.reason = reason;

		log.error("Internal Error Exception:", this);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtendMembershipException(String message, Throwable cause) {
		super(message, cause);

		log.error("Internal Error Exception:", this);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ExtendMembershipException(Throwable cause) {
		super(cause);

		log.error("Internal Error Exception:", this);
	}

	/**
	 * Return reason why member can't extends his membership.
	 *
	 * @see cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException.Reason
	 *
	 * @return Reason why member can't extend membership
	 */
	public Reason getReason() {
		return this.reason;
	}

	/**
	 * Return string value of member's attribute "membership expiration date"
	 * or null when expiration is not set.
	 *
	 * It's filled only when Reason is OUTSIDEEXTENSIONPERIOD.
	 *
	 * @return String value of membership expiration date
	 */
	public String getExpirationDate() {
		return this.expirationDate;
	}

	public enum Reason {
		NOUSERLOA, // User do not have LoA defined, byt VO has rules for membership expiration per LoA
		INSUFFICIENTLOA, // User has LoA which is not allowed in the VO
		INSUFFICIENTLOAFOREXTENSION, // User cannot extend membership because has insufficient LoA
		OUTSIDEEXTENSIONPERIOD, // We are not in grace period, so do not allow extension
	}

}
