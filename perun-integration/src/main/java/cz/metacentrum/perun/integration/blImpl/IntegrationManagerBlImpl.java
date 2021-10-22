package cz.metacentrum.perun.integration.blImpl;

import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.integration.bl.IntegrationManagerBl;
import cz.metacentrum.perun.integration.implApi.IntegrationManagerImplApi;

public class IntegrationManagerBlImpl implements IntegrationManagerBl {

	private IntegrationManagerImplApi integrationManagerImplApi;
	private PerunBl perun;

	public IntegrationManagerImplApi getIntegrationManagerImplApi() {
		return integrationManagerImplApi;
	}

	public void setIntegrationManagerImplApi(IntegrationManagerImplApi integrationManagerImplApi) {
		this.integrationManagerImplApi = integrationManagerImplApi;
	}

	public PerunBl getPerun() {
		return perun;
	}

	public void setPerun(PerunBl perun) {
		this.perun = perun;
	}
}
