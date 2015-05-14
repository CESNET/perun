package cz.metacentrum.perun.dispatcher.exceptions;

/**
 * Checked version of PerunHornetQServerException.
 * 
 * @author Michal Karm Babacek
 */
public class PerunHornetQServerException extends DispatcherException {

	private static final long serialVersionUID = 1L;

	public PerunHornetQServerException(String message) {
		super(message);
	}

	public PerunHornetQServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public PerunHornetQServerException(Throwable cause) {
		super(cause);
	}
}
