package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This exception serves for detection, that one person has more User identities in Perun.
 * This person needs to be merged to one User identity.
 *
 * @author  Jan Zverina
 */
public class UserDuplicateException extends PerunException {
	static final long serialVersionUID = 0;

	private List<User> users;

	public UserDuplicateException(String message) {
		super(message);
	}

	public UserDuplicateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserDuplicateException(Throwable cause) {
		super(cause);
	}

	public UserDuplicateException(User user1, User user2) {
		super(user1.toString() + " " + user2.toString());
		users = new ArrayList<>();
		users.add(user1);
		users.add(user2);
	}

	public List<User> getUsers() {
		return users;
	}
}
