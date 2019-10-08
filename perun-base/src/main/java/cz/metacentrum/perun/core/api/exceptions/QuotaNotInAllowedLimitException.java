package cz.metacentrum.perun.core.api.exceptions;


import cz.metacentrum.perun.core.api.Pair;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Raised when transferred quotas attribute value is not in defined limit.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class QuotaNotInAllowedLimitException extends InternalErrorException {
	static final long serialVersionUID = 0;

	private Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck;
	private Map<String, Pair<BigDecimal, BigDecimal>> limitQuota;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public QuotaNotInAllowedLimitException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public QuotaNotInAllowedLimitException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public QuotaNotInAllowedLimitException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with quota to check, limit quota and message
	 * @param quotaToCheck quota to be checked
	 * @param limitQuota limit quota
	 * @param message message with details about the cause
	 */
	public QuotaNotInAllowedLimitException(Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck, Map<String, Pair<BigDecimal, BigDecimal>> limitQuota, String message) {
		super(message);
		this.quotaToCheck = quotaToCheck;
		this.limitQuota = limitQuota;
	}

	/**
	 * Constructor with quota to check, limit quota and message
	 * @param quotaToCheck quota to be checked
	 * @param limitQuota limit quota
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public QuotaNotInAllowedLimitException(Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck,
										   Map<String, Pair<BigDecimal, BigDecimal>> limitQuota, String message, Throwable cause) {
		super(message, cause);
		this.quotaToCheck = quotaToCheck;
		this.limitQuota = limitQuota;
	}
}
