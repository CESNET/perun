package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.rt.SubGroupCannotBeRemovedRuntimeException;

/**
 * Checked version of SubGroupCannotBeRemovedException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.SubGroupCannotBeRemovedRuntimeException
 * @author Slavek Licehammer
 */
public class SubGroupCannotBeRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	private Group group;

	public SubGroupCannotBeRemovedException(SubGroupCannotBeRemovedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public SubGroupCannotBeRemovedException(String message) {
		super(message);
	}

	public SubGroupCannotBeRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubGroupCannotBeRemovedException(Throwable cause) {
		super(cause);
	}

	public SubGroupCannotBeRemovedException(Group group) {
		super(group.toString());
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}
}
