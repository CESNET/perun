package cz.metacentrum.perun.core.impl.modules.attributes;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testing class for login-namespace elixir persistent shadow attribute
 */
public class urn_perun_user_attribute_def_def_login_namespace_bbmriid_persistent_shadowTest {

  private static final CoreConfig mockedCoreConfig = mock(CoreConfig.class);
  private static urn_perun_user_attribute_def_def_login_namespace_bbmriid_persistent_shadow classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static CoreConfig originalCoreConfig;

  @BeforeClass
  public static void setUpCoreConfig() {
    originalCoreConfig = BeansUtils.getCoreConfig();
    BeansUtils.setConfig(mockedCoreConfig);
    when(mockedCoreConfig.getGeneratedLoginNamespaces()).thenReturn(emptyList());
  }

  @AfterClass
  public static void resetCoreConfig() {
    BeansUtils.setConfig(originalCoreConfig);
  }

  @Before
  public void SetUp() {
    classInstance = new urn_perun_user_attribute_def_def_login_namespace_bbmriid_persistent_shadow();
    session = mock(PerunSessionImpl.class);
    user = new User();
    user.setId(123456);
  }

  @Test
  public void testFillAttributeValueBbmriIdNamespace() {
    System.out.println("testFillAttributeValue()");

    Attribute attribute = new Attribute();
    attribute.setFriendlyName("login-namespace:bbmriid-persistent-shadow");

    Attribute outputOne = classInstance.fillAttribute(session, user, attribute);
    Attribute outputTwo = classInstance.fillAttribute(session, user, attribute);
    assertNotNull(outputOne.valueAsString());
    assertNotNull(outputTwo.valueAsString());
    assertNotEquals(outputOne.valueAsString(), outputTwo.valueAsString());
  }
}
