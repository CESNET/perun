package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.rt.ResourceNotExistsRuntimeException;

/**
 * Checked version of ResourceNotExistsException
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.ResourceNotExistsRuntimeException
 * @author Slavek Licehammer
 */
public class ResourceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Resource resource;

	public ResourceNotExistsException(ResourceNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public ResourceNotExistsException(String message) {
		super(message);
	}

	public ResourceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceNotExistsException(Throwable cause) {
		super(cause);
	}

	public ResourceNotExistsException(Resource resource) {
		super(resource.toString());
		this.resource = resource;
	}

	public Resource getResource() {
		return this.resource;
	}
}
