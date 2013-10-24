package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of PasswordDeletionFailedException.
 *
 * @author Michal Prochazka
 * @version $Id$
 */
public class PasswordDeletionFailedException extends PerunException {
    static final long serialVersionUID = 0;

    public PasswordDeletionFailedException(String message) {
        super(message);
    }

    public PasswordDeletionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordDeletionFailedException(Throwable cause) {
        super(cause);
    }
}
