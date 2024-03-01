package cz.metacentrum.perun.core.impl.modules.attributes;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test module for urn:perun:user:attribute-def:virt:forwardedVoPersonExternalAffiliation
 *
 * @author Michal Berky
 */
public class urn_perun_user_attribute_def_virt_forwardedVoPersonExternalAffiliationTest {

  private static urn_perun_user_attribute_def_virt_forwardedVoPersonExternalAffiliation classInstance;
  private final String VALUE1 = "11aff11@somewhere.edu";
  private final String VALUE2 = "22aff22@somewhere.edu";
  private final String VALUE3 = "33aff33@somewhere.edu";
  private PerunSessionImpl session;
  private User user;
  private UserExtSource ues1;
  private UserExtSource ues2;
  private Attribute uesAtt1;
  private Attribute uesAtt2;

  private static void prepareCoreConfig() {
    if (BeansUtils.getCoreConfig() == null) {
      CoreConfig testConfig = new CoreConfig();
      BeansUtils.setConfig(testConfig);
    }
  }

  @Test
  public void getAttributeValueOnlyFromUserExtSources() throws Exception {
    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2));

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(uesAtt1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(uesAtt2);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(VALUE1, VALUE2, VALUE3);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }

  @Before
  public void setVariables() {
    classInstance = new urn_perun_user_attribute_def_virt_forwardedVoPersonExternalAffiliation();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    prepareCoreConfig();

    user = new User();
    user.setId(1);

    ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");

    uesAtt1 = new Attribute();
    uesAtt2 = new Attribute();
    uesAtt1.setValue(VALUE1);
    uesAtt2.setValue(VALUE2 + ";" + VALUE3);
  }
}
