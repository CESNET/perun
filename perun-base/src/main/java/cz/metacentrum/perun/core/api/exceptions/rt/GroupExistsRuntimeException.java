package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class GroupExistsRuntimeException extends EntityExistsRuntimeException {

	private String groupName;

	public GroupExistsRuntimeException() {
		super();
	}

	public GroupExistsRuntimeException(Throwable cause) {
		super(cause);
	}

	public GroupExistsRuntimeException(String groupName) {
		super(groupName);
		this.groupName = groupName;
	}

	public GroupExistsRuntimeException(String groupName, Throwable cause) {
		super(groupName, cause);
		this.groupName = groupName;
	}

	public String getGroupName() {
		return groupName;
	}
}
