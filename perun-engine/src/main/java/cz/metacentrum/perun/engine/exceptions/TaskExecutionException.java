package cz.metacentrum.perun.engine.exceptions;


public class TaskExecutionException extends EngineException{
	private int returnCode;
	private String stdout;
	private String stderr;
	private Object id;

	public TaskExecutionException(Object id, String message) {
		super(message);
		this.id = id;
	}

	public TaskExecutionException(Object id, String message, Throwable cause) {
		super(message, cause);
		this.id = id;
	}

	public TaskExecutionException(Object id, Throwable cause) {
		super(cause);
		this.id = id;
	}

	public TaskExecutionException(Object id, int returnCode, String stdout, String stderr) {
		this.returnCode = returnCode;
		this.stdout = stdout;
		this.stderr = stderr;
		this.id = id;
	}

	public Object getId() {
		return id;
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
