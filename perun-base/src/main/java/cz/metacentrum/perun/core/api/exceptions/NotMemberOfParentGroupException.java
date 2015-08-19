package cz.metacentrum.perun.core.api.exceptions;

/**
 * Raise when member must be in parent group and he's not there.
 *
 * @author Slavek Licehammer
 */
public class NotMemberOfParentGroupException extends RelationExistsException {

	public NotMemberOfParentGroupException(String message) {
		super(message);
	}

	public NotMemberOfParentGroupException(Throwable cause) {
		super(cause);
	}

	public NotMemberOfParentGroupException(String message, Throwable cause) {
		super(message, cause);
	}
}
