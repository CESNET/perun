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

	public QuotaNotInAllowedLimitException(String message) {
		super(message);
	}

	public QuotaNotInAllowedLimitException(String message, Throwable cause) {
		super(message, cause);
	}

	public QuotaNotInAllowedLimitException(Throwable cause) {
		super(cause);
	}

	public QuotaNotInAllowedLimitException(Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck, Map<String, Pair<BigDecimal, BigDecimal>> limitQuota, String message) {
		super(message);
		this.quotaToCheck = quotaToCheck;
		this.limitQuota = limitQuota;
	}

	public QuotaNotInAllowedLimitException(Map<String, Pair<BigDecimal, BigDecimal>> quotaToCheck,
										   Map<String, Pair<BigDecimal, BigDecimal>> limitQuota, String message, Throwable cause) {
		super(message, cause);
		this.quotaToCheck = quotaToCheck;
		this.limitQuota = limitQuota;
	}
}
