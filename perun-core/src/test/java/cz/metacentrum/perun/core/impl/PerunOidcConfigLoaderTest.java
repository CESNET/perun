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
    expectedConfig.setClientId("1234");
    expectedConfig.setOidcDeviceCodeUri("https://test/devicecode");
    expectedConfig.setOidcTokenEndpointUri("https://test/token");
    expectedConfig.setOidcTokenRevokeEndpointUri("https://test/revoke");
    expectedConfig.setAcrValues("");
    expectedConfig.setScopes("openid perun_api perun_admin offline_access");
    expectedConfig.setPerunApiEndpoint("https://test/rpc");
    expectedConfig.setEnforceMfa(false);
  }

  @Test
  public void init() throws Exception {
    OidcConfig config = perun.getConfigManagerBl()
        .getPerunOidcConfig("https://perun-domain.name.com/non/rpc/json/configManager/getPerunOidcConfig");
    assertEquals(config, expectedConfig);
  }
}
