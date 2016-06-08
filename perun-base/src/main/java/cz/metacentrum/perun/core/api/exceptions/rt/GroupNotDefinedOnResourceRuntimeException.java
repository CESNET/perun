package cz.metacentrum.perun.core.api.exceptions.rt;

@SuppressWarnings("serial")
public class GroupNotDefinedOnResourceRuntimeException extends PerunRuntimeException {

	private String groupName;

	public GroupNotDefinedOnResourceRuntimeException() {
		super();
	}

	public GroupNotDefinedOnResourceRuntimeException(Throwable cause) {
		super(cause);
	}

	public GroupNotDefinedOnResourceRuntimeException(String groupName) {
		super(groupName);
		this.groupName = groupName;
	}

	public GroupNotDefinedOnResourceRuntimeException(String groupName, Throwable cause) {
		super(groupName, cause);
		this.groupName = groupName;
	}

	public String getGroupName() {
		return groupName;
	}
}
