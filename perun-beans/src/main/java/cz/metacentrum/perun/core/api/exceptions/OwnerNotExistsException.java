package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.exceptions.rt.OwnerNotExistsRuntimeException;

/**
 * Checked version of OwnerNotExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.OwnerNotExistsRuntimeException
 * @author Slavek Licehammer
 */
public class OwnerNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Owner owner;

	public OwnerNotExistsException(OwnerNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
