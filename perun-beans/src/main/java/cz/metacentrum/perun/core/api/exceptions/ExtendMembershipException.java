package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checked version of ExtendMembershipException.
 *
 * @author Michal Prochazka
 */
public class ExtendMembershipException extends PerunException {

	static final long serialVersionUID = 0;
	private final static Logger log = LoggerFactory.getLogger(ExtendMembershipException.class);
	private Reason reason;
	private String expirationDate;

	public ExtendMembershipException(Reason reason, String message) {
		super(message);

		this.reason = reason;

		log.error("Internal Error Exception:", this);
	}

	public ExtendMembershipException(Reason reason, String expirationDate, String message) {
		super(message);

		this.expirationDate = expirationDate;
		this.reason = reason;

		log.error("Internal Error Exception:", this);
	}

	public ExtendMembershipException(String message, Throwable cause) {
		super(message, cause);

		log.error("Internal Error Exception:", this);
	}

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
