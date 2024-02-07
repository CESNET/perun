package cz.metacentrum.perun.registrar.model;

import java.util.Objects;


/**
 * Represents a pair of application id along with a result
 * of an operation on the app - null if successful, exception otherwise.
 */
public class ApplicationOperationResult {
	int applicationId;

	Exception error;

	public ApplicationOperationResult() {
	}

	public ApplicationOperationResult(int applicationId, Exception error) {
		this.applicationId = applicationId;
		this.error = error;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public Exception getError() {
		return error;
	}

	public void setError(Exception error) {
		this.error = error;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ApplicationOperationResult that)) return false;
		return applicationId == that.applicationId && Objects.equals(error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(applicationId, error);
	}

	@Override
	public String toString() {
		return "ApplicationOperationResult{" +
			"applicationId=" + applicationId +
			", error=" + error +
			'}';
	}
}
