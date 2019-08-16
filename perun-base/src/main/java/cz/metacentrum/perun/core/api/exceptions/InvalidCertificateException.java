package cz.metacentrum.perun.core.api.exceptions;

/**
 * Certificate is not valid - for a lots of different reasons (like unsupported subject, expired etc.)
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class InvalidCertificateException extends PerunException {

	static final long serialVersionUID = 0;

	public InvalidCertificateException(String message) {
		super(message);
	}

	public InvalidCertificateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCertificateException(Throwable cause) {
		super(cause);
	}
}
