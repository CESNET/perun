package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class NotMemberOfParentGroupException extends EntityNotExistsRuntimeException {
	private String parentGroupName;

	public NotMemberOfParentGroupException() {
		super();
	}

	public NotMemberOfParentGroupException(String parentGroupName) {
		super();
		this.parentGroupName = parentGroupName;
	}

	public NotMemberOfParentGroupException(Throwable cause) {
		super(cause);
	}

	public NotMemberOfParentGroupException(Throwable cause, String parentGroupName) {
		super(cause);
		this.parentGroupName = parentGroupName;
	}

	public String getParentGroupName() {
		return parentGroupName;
	}
}
