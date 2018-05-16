package cz.metacentrum.perun.engine.exceptions;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * Exception thrown, when execution of GEN or SEND Task fails for any reason.
 * Contains all relevant data like Task itself, process return code, stdout/err.
 * For SEND Tasks relevant Destination is also present.
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskExecutionException extends EngineException {

	private Task task;
	private int returnCode;
	private String stdout;
	private String stderr;
	private Destination destination;

	public TaskExecutionException(Task task, String message) {
		super(message);
		this.task = task;
	}

	public TaskExecutionException(Task task, Destination destination, String message) {
		this(task, message);
		this.destination = destination;
	}

	public TaskExecutionException(Task task, Throwable cause) {
		super(cause);
		this.task = task;
	}

	public TaskExecutionException(Task task, String message, Throwable cause) {
		super(message, cause);
		this.task = task;
	}

	public TaskExecutionException(Task task, Destination destination, String message, Throwable cause) {
		this(task, message, cause);
		this.destination = destination;
	}

	public TaskExecutionException(Task task, int returnCode, String stdout, String stderr) {
		super();
		this.task = task;
		this.returnCode = returnCode;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public TaskExecutionException(Task task, Destination destination, int returnCode, String stdout, String stderr) {
		this(task, returnCode, stdout, stderr);
		this.destination = destination;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public String getErrorId() {
		return super.getErrorId();
	}

}
