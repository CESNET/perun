package cz.metacentrum.perun.cabinet.bl;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Basic exception thrown by Cabinet module. Reasons for throwing are
 * specified by ErrorCodes and usually by text message.
 * It extends PerunException to be handled same way as rest of exceptions in Perun.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class CabinetException extends PerunException {

	private static final long serialVersionUID = -5995047742063805226L;
	private ErrorCodes type = ErrorCodes.CODE_NOT_SET;
	//private Logger log = LoggerFactory.getLogger(getClass());

	public CabinetException() {
	}

	public CabinetException(ErrorCodes type) {
		this.type = type;
	}

	public CabinetException(String paramString) {
		super(paramString);
	}

	public CabinetException(String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}

	public CabinetException(Throwable paramThrowable) {
		super(paramThrowable);
	}

	public CabinetException(ErrorCodes type, Throwable paramThrowable) {
		super(paramThrowable);
		this.type = type;
	}

	public CabinetException(String msg, ErrorCodes type, Throwable paramThrowable) {
		super(msg, paramThrowable);
		this.type = type;
	}

	public CabinetException(String msg, ErrorCodes type) {
		super(msg);
		this.type = type;
	}

	public ErrorCodes getType() {
		return this.type;
	}

}
