package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.OidcConfig;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class PerunOidcConfigLoaderTest extends AbstractPerunIntegrationTest {

	private OidcConfig expectedConfig;

	@Before
	public void setUp() {
		expectedConfig = new OidcConfig();
		expectedConfig.setClientId("363b656e-d139-4290-99cd-ee64eeb830d5");
		expectedConfig.setOidcDeviceCodeUri("https://login.cesnet.cz/oidc/devicecode");
		expectedConfig.setOidcTokenEndpointUri("https://login.cesnet.cz/oidc/token");
		expectedConfig.setOidcTokenRevokeEndpointUri("https://login.cesnet.cz/oidc/revoke");
		expectedConfig.setAcrValues("");
		expectedConfig.setScopes("openid perun_api perun_admin offline_access");
		expectedConfig.setPerunApiEndpoint("https://perun-dev.cesnet.cz/oauth/rpc");
		expectedConfig.setEnforceMfa(false);
	}

	@Test
	public void init() {
		OidcConfig config = perun.getConfigManagerBl().getPerunOidcConfig();
		assertEquals(config, expectedConfig);
	}
}
