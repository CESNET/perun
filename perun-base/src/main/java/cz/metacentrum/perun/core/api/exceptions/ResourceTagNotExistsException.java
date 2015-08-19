package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ResourceTag;

/**
 * ResourceTag not exists exception.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ResourceTagNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private ResourceTag resourceTag;

	public ResourceTagNotExistsException(String message) {
		super(message);
	}

	public ResourceTagNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceTagNotExistsException(Throwable cause) {
		super(cause);
	}

	public ResourceTagNotExistsException(ResourceTag resourceTag) {
		super(resourceTag.toString());
		this.resourceTag = resourceTag;
	}

	public ResourceTag getResourceTag() {
		return this.resourceTag;
	}
}
