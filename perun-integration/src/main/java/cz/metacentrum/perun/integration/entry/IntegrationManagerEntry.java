package cz.metacentrum.perun.integration.entry;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.integration.api.IntegrationManager;
import cz.metacentrum.perun.integration.bl.IntegrationManagerBl;

public class IntegrationManagerEntry implements IntegrationManager {

	private IntegrationManagerBl integrationManagerBl;
	private Perun perun;

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
