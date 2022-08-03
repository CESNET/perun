package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when call to user info endpoint ends up with error HttpStatus.FORBIDDEN
 *
 * @author Lucie Kureckova <luckureckova@gmail.com>
 */
public class ExpiredTokenException extends PerunException {
	public ExpiredTokenException(String message) {
		super(message);
	}
}
