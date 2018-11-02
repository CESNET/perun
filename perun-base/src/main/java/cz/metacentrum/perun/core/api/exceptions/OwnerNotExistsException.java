package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Owner;

/**
 * Checked version of OwnerNotExistsException.
 *
 * @author Slavek Licehammer
 */
public class OwnerNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Owner owner;

	public OwnerNotExistsException(String message) {
		super(message);
	}

	public OwnerNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public OwnerNotExistsException(Throwable cause) {
		super(cause);
	}

	public OwnerNotExistsException(Owner owner) {
		super(owner.toString());
		this.owner = owner;
	}

	public Owner getOwner() {
		return this.owner;
	}
}
