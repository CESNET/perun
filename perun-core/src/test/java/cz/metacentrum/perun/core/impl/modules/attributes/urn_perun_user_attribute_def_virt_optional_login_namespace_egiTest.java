package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_virt_optional_login_namespace_egiTest {

  private static urn_perun_user_attribute_def_virt_optional_login_namespace_egi classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static ExtSource extSource;
  private static UserExtSource ues1;
  private static UserExtSource ues2;
  private final String EXTSOURCE_EGI = "https://aai.egi.eu/proxy/saml2/idp/metadata.php";

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_attribute_def_virt_optional_login_namespace_egi();
    session = mock(PerunSessionImpl.class);

    extSource = new ExtSource(EXTSOURCE_EGI, ExtSourcesManager.EXTSOURCE_IDP);

    ues1 = new UserExtSource();
    ues1.setId(1);
    ues1.setExtSource(extSource);
    ues1.setLogin("53358466014af3010b88afe55786aa6249c257049b8526c702f104d344e4d1c8@egi.eu");

    ues2 = new UserExtSource();
    ues2.setId(2);
    ues2.setExtSource(extSource);
    ues2.setLogin("abc58466014af3010b88afe55786aa6249c257049b8349b702f104d344e4d1c8@egi.eu");

    List<UserExtSource> ueses = new ArrayList<>();
    ueses.add(ues1);
    ueses.add(ues2);

    when(session.getPerunBl()).thenReturn(mock(PerunBl.class));
    when(session.getPerunBl().getUsersManagerBl()).thenReturn(mock(UsersManagerBl.class));
    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(ueses);
  }

  @Test
  public void testCheckNull() throws Exception {
    System.out.println("testCheckNull()");
    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(new ArrayList<>());

    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("optional-login-namespace:egi");

    assertNull(classInstance.getAttributeValue(session, user, attr).getValue());
  }

  @Test
  public void testLoginFromExtSource() throws Exception {
    System.out.println("testLoginFromExtSource()");

    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    attr.setFriendlyName("optional-login-namespace:egi");
    // should return EGI identity with lowest UES ID
    assertEquals("53358466014af3010b88afe55786aa6249c257049b8526c702f104d344e4d1c8@egi.eu",
            classInstance.getAttributeValue(session, user, attr).getValue());
  }

}
