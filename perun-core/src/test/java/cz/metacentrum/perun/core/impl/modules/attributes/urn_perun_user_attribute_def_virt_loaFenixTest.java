package cz.metacentrum.perun.core.impl.modules.attributes;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

/**
 * Test module for urn:perun:user:attribute-def:virt:loaFenix
 *
 * @author Petr Vsetecka <vsetecka@cesnet.cz>
 */
public class urn_perun_user_attribute_def_virt_loaFenixTest {

  private static urn_perun_user_attribute_def_virt_loaFenix classInstance;
  private final String VALUE1 = "urn:geant:eduteams.org:fenix:loa:1";
  private final String VALUE2 = "urn:geant:eduteams.org:fenix:loa:2";
  private final String VALUE3 = "urn:geant:eduteams.org:fenix:loa:3";
  private PerunSessionImpl session;
  private User user;
  private UserExtSource ues1;
  private UserExtSource ues2;
  private UserExtSource ues3;
  private Attribute uesAtt1;
  private Attribute uesAtt2;
  private Attribute uesAtt3;

  @Test
  public void getAttributeValueFromAllSources() throws Exception {
    urn_perun_user_attribute_def_virt_loaFenix classInstance = new urn_perun_user_attribute_def_virt_loaFenix();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    String primarySourceAttributeName = classInstance.getSourceAttributeName();

    // USER_EXT_SOURCE
    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2, ues3));
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, ues1, primarySourceAttributeName)).thenReturn(uesAtt1);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, ues2, primarySourceAttributeName)).thenReturn(uesAtt2);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, ues3, primarySourceAttributeName)).thenReturn(uesAtt3);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof String);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") String actual = receivedAttr.valueAsString();
    String expected = VALUE1;
    assertEquals("collected values are incorrect", expected, actual);

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(Collections.emptyList());

    Attribute receivedAttr2 = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertNull(receivedAttr2.getValue());
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());
  }

  @Before
  public void setVariables() {
    classInstance = new urn_perun_user_attribute_def_virt_loaFenix();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    user = new User();
    user.setId(1);

    ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");
    ues3 = new UserExtSource(30, new ExtSource(300, "name3", "type3"), "login3");
    ues1.setUserId(user.getId());
    ues2.setUserId(user.getId());
    ues3.setUserId(user.getId());

    uesAtt1 = new Attribute();
    uesAtt2 = new Attribute();
    uesAtt3 = new Attribute();
    uesAtt1.setValue(VALUE1);
    uesAtt2.setValue(VALUE2);
    uesAtt3.setValue(VALUE3);
  }

}
