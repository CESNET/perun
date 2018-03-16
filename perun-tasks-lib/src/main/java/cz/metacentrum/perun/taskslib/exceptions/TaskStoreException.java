package cz.metacentrum.perun.taskslib.exceptions;


import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Thrown when Task can't be added or removed from a TaskStore.
 *
 * @see cz.metacentrum.perun.taskslib.service.TaskStore
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskStoreException extends PerunException {

	public TaskStoreException(String message) {
		super(message);
	}

	public TaskStoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaskStoreException(Throwable cause) {
		super(cause);
	}

}
