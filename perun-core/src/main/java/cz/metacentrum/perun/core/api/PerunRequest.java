package cz.metacentrum.perun.core.api;

/**
 * Class containing information about PerunRequest.
 *
 * @author michalp
 */
public class PerunRequest {

	private long startTime;
	private long endTime = -1;
	private String sessionId;
	private PerunPrincipal perunPrincipal;
	private String callbackName;
	private String manager;
	private String method;
	private String params;
	private Object result;

	public PerunRequest(String sessionId, PerunPrincipal perunPrincipal, String callbackName, String manager, String method, String params) {
		this.startTime = System.currentTimeMillis();
		this.sessionId = sessionId;
		this.perunPrincipal = perunPrincipal;
		this.callbackName = callbackName;
		this.manager = manager;
		this.method = method;
		this.params = params;
	}

	public String getSessionId() {
		return sessionId;
	}

	public long getStartTime() {
		return startTime;
	}

	public PerunPrincipal getPerunPrincipal() {
		return perunPrincipal;
	}

	public String getCallbackName() {
		return callbackName;
	}

	public void setCallbackName(String callbackName) {
		this.callbackName = callbackName;
	}

	public String getManager() {
		return manager;
	}

	public String getMethod() {
		return method;
	}

	public String getParams() {
		return params;
	}

	public long getEndTime() {
		return endTime;
	}

	public Object getResult() {
		return result;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((manager == null) ? 0 : manager.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result
				+ ((perunPrincipal == null) ? 0 : perunPrincipal.hashCode());
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PerunRequest other = (PerunRequest) obj;
		if (manager == null) {
			if (other.manager != null)
				return false;
		} else if (!manager.equals(other.manager))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (perunPrincipal == null) {
			if (other.perunPrincipal != null)
				return false;
		} else if (!perunPrincipal.equals(other.perunPrincipal))
			return false;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		if (startTime != other.startTime)
			return false;
		return true;
	}

}