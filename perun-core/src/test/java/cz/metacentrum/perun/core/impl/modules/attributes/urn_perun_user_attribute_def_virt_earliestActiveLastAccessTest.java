package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_virt_earliestActiveLastAccessTest {

  private static urn_perun_user_attribute_def_virt_earliestActiveLastAccess classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static UserExtSource ues1;
  private static UserExtSource ues2;
  private static UserExtSource ues3;
  private static UserExtSource ues4;
  private static AttributeDefinition attrDef;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_earliestActiveLastAccess();
    session = mock(PerunSessionImpl.class);
    user = mock(User.class);

    if (BeansUtils.getCoreConfig() == null) {
      CoreConfig testConfig = new CoreConfig();
      testConfig.setIdpLoginValidity(12);
      BeansUtils.setConfig(testConfig);
    }

    int validity = BeansUtils.getCoreConfig().getIdpLoginValidity();

    ues1 =
        new UserExtSource(10, new ExtSource(100, "earliestAccessExtSource1", ExtSourcesManager.EXTSOURCE_IDP), "login");
    ues2 = new UserExtSource(10, new ExtSource(100, "earliestAccessExtSource2", "nonIdP"), "login");
    ues3 =
        new UserExtSource(10, new ExtSource(100, "earliestAccessExtSource3", ExtSourcesManager.EXTSOURCE_IDP), "login");
    ues4 =
        new UserExtSource(10, new ExtSource(100, "earliestAccessExtSource4", ExtSourcesManager.EXTSOURCE_IDP), "login");

    // try variable length of seconds fractions in last access timestamp
    ues1.setLastAccess(
        LocalDateTime.now().minusMonths(validity + 1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")));
    ues2.setLastAccess(LocalDateTime.now().minusMonths(validity - 1).format(Utils.lastAccessFormatter));
    ues3.setLastAccess(LocalDateTime.now().minusMonths(validity - 2).format(Utils.lastAccessFormatter));
    ues4.setLastAccess(
        LocalDateTime.now().minusMonths(validity - 3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

    attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attrDef.setFriendlyName("earliestActiveLastAccess");

    when(session.getPerunBl()).thenReturn(mock(PerunBl.class));
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(mock(UsersManagerBl.class));
    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        List.of(ues1, ues2, ues3, ues4));
  }

  @Test
  public void testCheckWithAttribute() {
    System.out.println("testCheckWithAttribute()");
    assertEquals(ues3.getLastAccess(), classInstance.getAttributeValue(session, user, attrDef).getValue());
  }
}