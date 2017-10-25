package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;

import java.util.Arrays;
import java.util.List;

/**
 * Representing client user (or machine) using to communicate with perun.
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class PerunClient {

	public static final String SCOPE_ALL = "all";

	private String id;
	private List<String> scopes;
	private Type type;

	/**
	 * Create OAuth / OIDC client.
	 *
	 * @param id "Client id" in OAuth terminology
	 * @param scopes Domains represent clinet rights. "Scopes" in OAuth terminology. "Claims" in OpenID terminology
	 */
	public PerunClient(String id, List<String> scopes) {
		if (id == null) throw new InternalErrorRuntimeException(new NullPointerException("id is null"));
		if (scopes == null) throw new InternalErrorRuntimeException(new NullPointerException("scopes are null"));

		this.id = id;
		this.scopes = scopes;
		this.type = Type.OAUTH;
	}

	/**
	 * Create trustful internal client. E.g. GUI, test or client for internal components.
	 */
	public PerunClient() {
		this.scopes = Arrays.asList(SCOPE_ALL);
		this.type = Type.INTERNAL;
	}


	public String getId() {
		return id;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append(":[")
				.append("id='").append(id).append("', ")
				.append("scopes='").append(scopes).append("']").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((scopes == null) ? 0 : scopes.hashCode());
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
		PerunClient other = (PerunClient) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (scopes == null) {
			if (other.scopes != null)
				return false;
		} else if (!scopes.equals(other.scopes))
			return false;
		return true;
	}

	public enum Type {
		INTERNAL, // Trustful client. E.g. Perun GUI, client for internal components or tests. No privileges are checked.
		OAUTH;  // Untrustful client. Privileges (scopes) should be checked. Not all methods are allowed to access.
	}

	public static final String PERUN_API_SCOPE = "perun_api"; //scope for calling RPC and delegation all roles except PERUNADMIN
	public static final String PERUN_ADMIN_SCOPE = "perun_admin"; //scope fo delagating even the admin role
}
