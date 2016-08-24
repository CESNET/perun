package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Resource;

/**
 * Checked version of ResourceExistsException.
 *
 * @author Oliver Mrázik
 */
public class ResourceExistsException extends PerunException {

	private static final long serialVersionUID = -255958501797585251L;

	private Resource resource;
	
	public ResourceExistsException(String message) {
		super(message);
	}

	public ResourceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceExistsException(Throwable cause) {
		super(cause);
	}

	public ResourceExistsException(Resource resource) {
		super("Resource with name \"" + resource.getName() + "\" already exists in Facility and Vo.");
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}
}
