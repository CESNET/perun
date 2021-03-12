package cz.metacentrum.perun.core.implApi.modules.pwdmgr;

import org.w3c.dom.Document;

/**
 * This class represents the response returned from the IS. It contains the returned status,
 * error (if present) and the whole response itself, in a Document class.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ISResponseData {
	private String status;
	private String error;
	private Document response;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Document getResponse() {
		return response;
	}

	public void setResponse(Document response) {
		this.response = response;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}