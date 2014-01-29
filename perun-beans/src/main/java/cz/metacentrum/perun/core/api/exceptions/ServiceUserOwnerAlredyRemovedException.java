package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of ServiceUserOwnerAlredyRemovedException.
 *
 * @author Michal Stava
 */
public class ServiceUserOwnerAlredyRemovedException extends PerunException {
    static final long serialVersionUID = 0;

    public ServiceUserOwnerAlredyRemovedException(String message) {
        super(message);
    }

    public ServiceUserOwnerAlredyRemovedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceUserOwnerAlredyRemovedException(Throwable cause) {
        super(cause);
    }

}
