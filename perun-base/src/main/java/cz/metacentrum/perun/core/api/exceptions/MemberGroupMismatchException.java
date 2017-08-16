package cz.metacentrum.perun.core.api.exceptions;

/**
 * Member and Group are not in the same VO
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MemberGroupMismatchException extends PerunException {

	public MemberGroupMismatchException(Throwable cause) {
		super(cause);
	}

	public MemberGroupMismatchException(String message, Throwable cause) {
		super(message,cause);
	}

	public MemberGroupMismatchException(String message) {
		super(message);
	}

	public MemberGroupMismatchException() {
	}

}
