package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class urn_perun_user_attribute_def_virt_eduPersonORCIDTest {
  private static final String A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE =
      AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":eduPersonORCIDConfig";

  @Test
  public void getAttributeValueNoConfig() throws Exception {
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String value1 = orcidAddress + "0000-0002-0305-7446";
    String value2 = orcidAddress + "0000-0002-1111-2222";
    att1.setValue(value1 + ";" + value2);
    Attribute att2 = new Attribute();
    String value3 = orcidAddress + "0000-0002-1111-3333";
    att2.setValue(value3);

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2));

    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(eq(session), anyString())).thenReturn(
        new ArrayList<>());

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(att1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(att2);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(value1, value2, value3);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }

  @Test
  public void getAttributeValueSourceAttributeSet() throws Exception {
    String sourceAttributeName = "eduPersonDifferentOrcid";
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");
    UserExtSource ues3 = new UserExtSource(10, new ExtSource(100, "name3", "type2"), "login1");
    UserExtSource ues4 = new UserExtSource(20, new ExtSource(200, "name4", "type2"), "login2");
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String value1 = orcidAddress + "0000-0002-0305-7446";
    String value2 = orcidAddress + "0000-0002-1111-2222";
    att1.setValue(value1 + ";" + value2);
    Attribute att2 = new Attribute();
    String value3 = orcidAddress + "0000-0002-1111-3333";
    att2.setValue(value3);
    Attribute att3 = new Attribute();
    String value4 = orcidAddress + "1111-2222-3333-4444";
    att3.setValue(value4);
    Attribute eduPersonOrcidConfigAttribute = new Attribute();
    eduPersonOrcidConfigAttribute.setValue(new LinkedHashMap<>(Map.of(
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.SOURCE_ATTRIBUTE_KEY, sourceAttributeName)));

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2, ues3, ues4));

    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(eq(session),
        eq(A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE))).thenReturn(List.of(eduPersonOrcidConfigAttribute));

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(att1);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, ues2, AttributesManager.NS_UES_ATTR_DEF + ":" + sourceAttributeName)).thenReturn(
        att2);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, ues3, AttributesManager.NS_UES_ATTR_DEF + ":" + sourceAttributeName)).thenReturn(
        att2);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, ues4, AttributesManager.NS_UES_ATTR_DEF + ":" + sourceAttributeName)).thenReturn(
        att3);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(value3, value4);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }

  @Test
  public void getAttributeValueExtSourceFilterSet() throws Exception {
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name1", "type2"), "login2");
    UserExtSource ues3 = new UserExtSource(10, new ExtSource(100, "name1", "type2"), "login1");
    UserExtSource ues4 = new UserExtSource(20, new ExtSource(200, "name4", "type2"), "login2");
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String value1 = orcidAddress + "0000-0002-0305-7446";
    String value2 = orcidAddress + "0000-0002-1111-2222";
    att1.setValue(value1 + ";" + value2);
    Attribute att2 = new Attribute();
    String value3 = orcidAddress + "0000-0002-1111-3333";
    att2.setValue(value3);
    Attribute att3 = new Attribute();
    String value4 = orcidAddress + "1111-2222-3333-4444";
    att3.setValue(value4);
    Attribute eduPersonOrcidConfigAttribute = new Attribute();
    eduPersonOrcidConfigAttribute.setValue(new LinkedHashMap<>(Map.of(
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.ES_NAME_KEY, "name1",
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.ES_TYPE_KEY, "type2")));

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2, ues3, ues4));

    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(eq(session),
        eq(A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE))).thenReturn(List.of(eduPersonOrcidConfigAttribute));

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(att1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(att2);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues3, attributeName)).thenReturn(att2);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues4, attributeName)).thenReturn(att3);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(value3);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }

  @Test
  public void getAttributeValueGetAlsoExtLoginSet() throws Exception {
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    String login1 = "login1";
    String login2 = "login2";
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), login1);
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), login2);
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String value1 = orcidAddress + "0000-0002-0305-7446";
    String value2 = orcidAddress + "0000-0002-1111-2222";
    att1.setValue(value1 + ";" + value2);
    Attribute att2 = new Attribute();
    String value3 = orcidAddress + "0000-0002-1111-3333";
    att2.setValue(value3);
    Attribute eduPersonOrcidConfigAttribute = new Attribute();
    eduPersonOrcidConfigAttribute.setValue(new LinkedHashMap<>(Map.of(
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.GET_EXT_LOGIN_KEY, "true")));

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2));

    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(eq(session),
        eq(A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE))).thenReturn(List.of(eduPersonOrcidConfigAttribute));

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(att1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(att2);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(value1, value2, value3, login1, login2);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }

  @Test
  public void getAttributeValueValueFilterSet() throws Exception {
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name1", "type2"), "login2");
    UserExtSource ues3 = new UserExtSource(10, new ExtSource(100, "name1", "type2"), "login1");
    UserExtSource ues4 = new UserExtSource(20, new ExtSource(200, "name4", "type2"), "login2");
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String value1 = orcidAddress + "0000-0002-0305-7446";
    String value2 = orcidAddress + "0000-0002-1111match-2222";
    att1.setValue(value1 + ";" + value2);
    Attribute att2 = new Attribute();
    String value3 = orcidAddress + "0000-0002-1111match-3333";
    att2.setValue(value3);
    Attribute att3 = new Attribute();
    String value4 = orcidAddress + "1111-2222-3333-4444";
    att3.setValue(value4);
    Attribute eduPersonOrcidConfigAttribute = new Attribute();
    eduPersonOrcidConfigAttribute.setValue(new LinkedHashMap<>(Map.of(
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.VALUE_FILTER_KEY, ".*match.*")));

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2, ues3, ues4));

    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(eq(session),
        eq(A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE))).thenReturn(List.of(eduPersonOrcidConfigAttribute));

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(att1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(att2);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues3, attributeName)).thenReturn(att2);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues4, attributeName)).thenReturn(att3);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(value2, value3);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }

  @Test
  public void getAttributeValueValueTransformationSet() throws Exception {
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name1", "type2"), "login2");
    UserExtSource ues3 = new UserExtSource(10, new ExtSource(100, "name1", "type2"), "login1");
    UserExtSource ues4 = new UserExtSource(20, new ExtSource(200, "name4", "type2"), "login2");
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String value1 = orcidAddress + "0000-examplePattern-0002-0305-7446";
    String value1Modified = orcidAddress + "0000-exampleReplacement-0002-0305-7446";
    String value2 = orcidAddress + "0000-0002-1111-2222";
    att1.setValue(value1 + ";" + value2);
    Attribute att2 = new Attribute();
    String value3 = orcidAddress + "0000-examplePattern-0002-1111-3333";
    String value3Modified = orcidAddress + "0000-exampleReplacement-0002-1111-3333";
    att2.setValue(value3);
    Attribute att3 = new Attribute();
    String value4 = orcidAddress + "1111-2222-3333-4444";
    att3.setValue(value4);
    Attribute eduPersonOrcidConfigAttribute = new Attribute();
    eduPersonOrcidConfigAttribute.setValue(
        new LinkedHashMap<>(Map.of(
            urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.PATTERN_KEY, "examplePattern",
            urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.REPLACEMENT_KEY, "exampleReplacement")));

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2, ues3, ues4));

    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(eq(session),
        eq(A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE))).thenReturn(List.of(eduPersonOrcidConfigAttribute));

    String attributeName = classInstance.getSourceAttributeName();
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(att1);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(att2);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues3, attributeName)).thenReturn(att2);
    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues4, attributeName)).thenReturn(att3);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(value1Modified, value2, value3Modified, value4);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }

  @Test
  public void getAttributeValueValueAllConfigAttributesSet() throws Exception {
    String sourceAttributeName = "eduPersonDifferentOrcid";
    String wantedUesType = "wantedType";
    urn_perun_user_attribute_def_virt_eduPersonORCID classInstance =
        new urn_perun_user_attribute_def_virt_eduPersonORCID();
    PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    User user = new User();
    user.setId(1);
    String loginToMatch = "login_match_examplePattern";
    String loginToMatchModified = "login_match_exampleReplacement";
    UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
    UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name2", wantedUesType), "login2");
    UserExtSource ues3 = new UserExtSource(10, new ExtSource(100, "name3", wantedUesType), loginToMatch);
    UserExtSource ues4 = new UserExtSource(20, new ExtSource(200, "name4", wantedUesType), "login4");
    Attribute att1 = new Attribute();
    String orcidAddress = "http://orcid.org/";
    String value1 = orcidAddress + "0000-0002-1111-2222match";
    att1.setValue(value1);
    Attribute att2 = new Attribute();
    String value2 = orcidAddress + "0000-examplePattern-0002-0305-7446match;filter_out";
    String value2Modified = orcidAddress + "0000-exampleReplacement-0002-0305-7446match";
    att2.setValue(value2);
    Attribute att3 = new Attribute();
    String value3 = orcidAddress + "0000-examplePattern-0002-1111-3333";
    att3.setValue(value3);
    Attribute att4 = new Attribute();
    String value4 = orcidAddress + "match0000-2222-1111-3333";
    att4.setValue(value4);

    Attribute eduPersonOrcidConfigAttribute = new Attribute();
    eduPersonOrcidConfigAttribute.setValue(new LinkedHashMap<>(Map.of(
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.SOURCE_ATTRIBUTE_KEY, sourceAttributeName,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.GET_EXT_LOGIN_KEY, "1",
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.VALUE_FILTER_KEY, ".*match.*",
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.ES_TYPE_KEY, wantedUesType,
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.PATTERN_KEY, "examplePattern",
        urn_perun_entityless_attribute_def_def_eduPersonORCIDConfig.REPLACEMENT_KEY, "exampleReplacement")));

    when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
        Arrays.asList(ues1, ues2, ues3, ues4));

    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributes(eq(session),
        eq(A_E_EDU_PERSON_ORCID_CONFIG_ATTRIBUTE))).thenReturn(List.of(eduPersonOrcidConfigAttribute));

    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, ues1, AttributesManager.NS_UES_ATTR_DEF + ":" + sourceAttributeName)).thenReturn(
        att1);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, ues2, AttributesManager.NS_UES_ATTR_DEF + ":" + sourceAttributeName)).thenReturn(
        att2);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, ues3, AttributesManager.NS_UES_ATTR_DEF + ":" + sourceAttributeName)).thenReturn(
        att3);
    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, ues4, AttributesManager.NS_UES_ATTR_DEF + ":" + sourceAttributeName)).thenReturn(
        att4);

    Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
    assertTrue(receivedAttr.getValue() instanceof List);
    assertEquals("destination attribute name wrong", classInstance.getDestinationAttributeFriendlyName(),
        receivedAttr.getFriendlyName());

    @SuppressWarnings("unchecked") List<String> actual = (List<String>) receivedAttr.getValue();
    Collections.sort(actual);
    List<String> expected = Arrays.asList(value2Modified, value4, loginToMatchModified);
    Collections.sort(expected);
    assertEquals("collected values are incorrect", expected, actual);
  }
}
