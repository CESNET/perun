package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.PerunPrincipal;

/**
 * Class containing information about PerunRequest.
 *
 * @author michalp
 */
public class PerunRequest {

	private long startTime;
	private long endTime = -1;
	private PerunPrincipal perunPrincipal;
	private String callbackId;
	private String manager;
	private String method;
	private String params;
	private Object result;

	public PerunRequest(PerunPrincipal perunPrincipal, String callbackId, String manager, String method, String params) {
		this.startTime = System.currentTimeMillis();
		this.perunPrincipal = perunPrincipal;
		this.callbackId = callbackId;
		this.manager = manager;
		this.method = method;
		this.params = params;
	}

	public long getStartTime() {
		return startTime;
	}

	public PerunPrincipal getPerunPrincipal() {
		return perunPrincipal;
	}

	public String getCallbackId() {
		return callbackId;
	}

	public void setCallbackId(String callbackId) {
		this.callbackId = callbackId;
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
		result = prime * result + ((callbackId == null) ? 0 : callbackId.hashCode());
		result = prime * result + ((manager == null) ? 0 : manager.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((perunPrincipal == null) ? 0 : perunPrincipal.hashCode());
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
		if (callbackId == null) {
			if (other.callbackId != null)
				return false;
		} else if (!callbackId.equals(other.callbackId))
			return false;
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
		if (startTime != other.startTime)
			return false;
		return true;
	}

}