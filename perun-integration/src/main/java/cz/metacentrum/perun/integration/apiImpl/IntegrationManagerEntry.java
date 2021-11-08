package cz.metacentrum.perun.integration.apiImpl;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.integration.api.IntegrationManager;
import cz.metacentrum.perun.integration.bl.IntegrationManagerBl;
import cz.metacentrum.perun.integration.model.GroupMemberRelations;

import java.util.List;

public class IntegrationManagerEntry implements IntegrationManager {

	private IntegrationManagerBl integrationManagerBl;
	private Perun perun;

	@Override
	public List<GroupMemberRelations> getGroupMemberRelations(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.authorizedInternal(sess, "getGroupMemberRelations_policy")) {
			throw new PrivilegeException("getGroupMemberRelations");
		}

		return integrationManagerBl.getGroupMemberRelations(sess);
	}

	public IntegrationManagerBl getIntegrationManagerBl() {
		return integrationManagerBl;
	}

	public void setIntegrationManagerBl(IntegrationManagerBl integrationManagerBl) {
		this.integrationManagerBl = integrationManagerBl;
	}

	public Perun getPerun() {
		return perun;
	}

	public void setPerun(Perun perun) {
		this.perun = perun;
	}
}
