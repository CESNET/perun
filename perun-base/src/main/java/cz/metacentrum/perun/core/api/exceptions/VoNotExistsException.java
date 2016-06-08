package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.rt.VoNotExistsRuntimeException;

/**
 * Checked version of VoNotExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.VoNotExistsRuntimeException
 * @author Martin Kuba
 */
public class VoNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private Vo vo;

	public VoNotExistsException(VoNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
