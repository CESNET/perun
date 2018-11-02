package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Vo;

/**
 * Checked version of VoNotExistsException.
 *
 * @author Martin Kuba
 */
public class VoNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Vo vo;

	public VoNotExistsException(String message) {
		super(message);
	}

	public VoNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public VoNotExistsException(Throwable cause) {
		super(cause);
	}

	public VoNotExistsException(Vo vo) {
		super(vo.toString());
		this.vo = vo;
	}


	public Vo getVo() {
		return this.vo;
	}
}
