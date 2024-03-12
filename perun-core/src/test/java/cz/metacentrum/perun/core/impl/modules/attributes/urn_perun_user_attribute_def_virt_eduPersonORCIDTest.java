package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
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
import java.util.List;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class urn_perun_user_attribute_def_virt_eduPersonORCIDTest {

  @Test
  public void getAttributeValue() throws Exception {
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String VALUE1 = orcidAddress + "0000-0002-0305-7446";
    String VALUE2 = orcidAddress + "0000-0002-1111-2222";
    att1.setValue(VALUE1 + ";" + VALUE2);
    Attribute att2 = new Attribute();
    String VALUE3 = orcidAddress + "0000-0002-1111-3333";
    att2.setValue(VALUE3);

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2));

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(att1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(att2);

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

}
