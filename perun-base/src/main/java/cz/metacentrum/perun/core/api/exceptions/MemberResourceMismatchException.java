package cz.metacentrum.perun.core.api.exceptions;

/**
 * Member and resource are not in the same VO
 * @author Zdenek Strmiska
 * @date 22.8.2017
 */

public class MemberResourceMismatchException extends PerunException {

    public MemberResourceMismatchException(Throwable cause) {
        super(cause);
    }

    public MemberResourceMismatchException(String message, Throwable cause) {
        super(message,cause);
    }

    public MemberResourceMismatchException(String message) {
        super(message);
    }

    public MemberResourceMismatchException() {
    }
}
