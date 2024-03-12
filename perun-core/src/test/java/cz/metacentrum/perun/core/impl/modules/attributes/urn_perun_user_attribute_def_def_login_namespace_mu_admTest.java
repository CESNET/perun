package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.pwdmgr.GenericPasswordManagerModule;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_def_login_namespace_mu_admTest {
  private static urn_perun_user_attribute_def_def_login_namespace_mu_adm classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute attributeToCheck;
  private CoreConfig oldConfig = BeansUtils.getCoreConfig();

  @Before
  public void setUp() throws Exception {
    //prepare core config for this test
    CoreConfig cfNew = new CoreConfig();
    cfNew.setInstanceId("test");
    BeansUtils.setConfig(cfNew);

    classInstance = new urn_perun_user_attribute_def_def_login_namespace_mu_adm();
    session = mock(PerunSessionImpl.class);
    user = new User();
    attributeToCheck = new Attribute();
    attributeToCheck.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeToCheck.setFriendlyName("login-namespace:mu-adm");

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);
    UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(usersManagerBl);
    PasswordManagerModule module = mock(GenericPasswordManagerModule.class);
    when(session.getPerunBl().getUsersManagerBl().getPasswordManagerModule(session, "mu-adm")).thenReturn(module);

    ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
    when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);
  }

  @After
  public void tearDown() throws Exception {
    //return old core config
    BeansUtils.setConfig(oldConfig);
  }

  @Test
  public void testCorrectSemantics() throws Exception {
    System.out.println("testCorrectSemantics()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

}
