package cz.metacentrum.perun.core.api.exceptions;

/**
 * Pattern is not well-formed.
 *
 * Is to be thrown when the generative pattern of hostname e.g. local[00-12]domain has a wrong syntax.
 *
 * @author Jirka Mauritz <jirmauritz@gmail.com>
 */
public class WrongPatternException extends PerunException {
	static final long serialVersionUID = 0;

	public WrongPatternException(String message) {
		super(message);
	}

	public WrongPatternException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongPatternException(Throwable cause) {
		super(cause);
	}
}
