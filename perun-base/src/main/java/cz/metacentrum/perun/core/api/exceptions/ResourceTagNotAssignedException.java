package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ResourceTag;

/**
 * ResourceTag not assigned exception.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ResourceTagNotAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private ResourceTag resourceTag;

	public ResourceTagNotAssignedException(String message) {
		super(message);
	}

	public ResourceTagNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceTagNotAssignedException(Throwable cause) {
		super(cause);
	}

	public ResourceTagNotAssignedException(ResourceTag resourceTag) {
		super(resourceTag.toString());
		this.resourceTag = resourceTag;
	}

	public ResourceTag getResourceTag() {
		return resourceTag;
	}
}
