package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ResourceTag;
/**
 * ResourceTag already assigned exception.
 *
 * @author Stava Michal <stavamichal@gmail.com>
 */
public class ResourceTagAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private ResourceTag resourceTag;

	public ResourceTagAlreadyAssignedException(String message) {
		super(message);
	}

	public ResourceTagAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceTagAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	public ResourceTagAlreadyAssignedException(ResourceTag resourceTag) {
		super(resourceTag.toString());
		this.resourceTag = resourceTag;
	}

	public ResourceTagAlreadyAssignedException(String message, ResourceTag resourceTag) {
		super(message);
		this.resourceTag = resourceTag;
	}

	public ResourceTag getResourceTag() {
		return resourceTag;
	}
}
