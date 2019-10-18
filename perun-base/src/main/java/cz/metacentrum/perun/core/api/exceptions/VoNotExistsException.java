package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Vo;

/**
 * Thrown when the VO has not been found in the database
 *
 * @author Martin Kuba
 */
public class VoNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Vo vo;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public VoNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public VoNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public VoNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the VO
	 * @param vo which does not exist
	 */
	public VoNotExistsException(Vo vo) {
		super(vo.toString());
		this.vo = vo;
	}


	/**
	 * Getter for the VO
	 * @return vo which does not exist
	 */
	public Vo getVo() {
		return this.vo;
	}
}
