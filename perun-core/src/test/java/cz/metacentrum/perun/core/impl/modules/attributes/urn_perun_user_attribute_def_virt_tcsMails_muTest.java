package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_user_attribute_def_virt_tcsMails_muTest {

  private static final Logger LOG = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_tcsMails_muTest.class);

  private final urn_perun_user_attribute_def_virt_tcsMails_mu classInstance =
      new urn_perun_user_attribute_def_virt_tcsMails_mu();
  private final AttributeDefinition tcsMailsAttrDef = classInstance.getAttributeDefinition();

  private final User user = new User(10, "Joe", "Doe", "W.", "", "");

  private final String email1 = "Zemail1@mail.cz";
  Attribute preferredMailAttr = setUpUserAttribute(1, "preferredMail", String.class.getName(), email1);
  private final String email2 = "email2@mail.cz "; // to check for the trim()
  Attribute isMailAttr = setUpUserAttribute(2, "ISMail", String.class.getName(), email2);
  private final String email3 = "email3@mail.cz";
  private final String email4 = "email4@mail.cz";
  Attribute o365MailsAttr = setUpUserAttribute(3, "o365EmailAddresses:mu", ArrayList.class.getName(),
      new ArrayList<>(Arrays.asList(email3, email4)));
  private final String email5 = "email5@mail.cz";
  Attribute privateMailsAttr = setUpUserAttribute(5, "privateAliasMails", ArrayList.class.getName(),
      new ArrayList<>(Arrays.asList(email1, email3, email5)));
  private final String email6 = " Email5@mail.cz"; // to check for the trim() and uniqueness
  Attribute publicMailsAttr = setUpUserAttribute(4, "publicAliasMails", ArrayList.class.getName(),
      new ArrayList<>(Arrays.asList(email4, email5, email6)));
  private final String expectedTestOfMessage = "friendlyName=<tcsMails:mu>";
  private PerunSessionImpl sess;

  @Test
  public void getAttributeValue() {
    ArrayList<String> attributeValue = classInstance.getAttributeValue(sess, user, tcsMailsAttrDef).valueAsList();
    assertNotNull(attributeValue);
    //we want to be sure, that preferredEmail is first (defined by sorting in module)
    assertEquals(email1, attributeValue.get(0));
    assertEquals(email2.trim(), attributeValue.get(1)); // check if was trimmed
    assertEquals(email3, attributeValue.get(2));
    assertEquals(email4, attributeValue.get(3));
    assertEquals(email5, attributeValue.get(4));
    assertEquals(email6.trim().toLowerCase(), attributeValue.get(4)); // check if was trimmed and then equals "email5"
    assertEquals(5, attributeValue.size());

  }

  @Test
  public void resolveVirtualAttributeValueChangeRemoved1() throws Exception {
    AuditEvent userRem = new AttributeRemovedForUser(preferredMailAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeRemoved2() throws Exception {
    AuditEvent userRem = new AttributeRemovedForUser(isMailAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeRemoved3() throws Exception {
    AuditEvent userRem = new AttributeRemovedForUser(publicMailsAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeRemoved4() throws Exception {
    AuditEvent userRem = new AttributeRemovedForUser(privateMailsAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeRemoved5() throws Exception {
    AuditEvent userRem = new AttributeRemovedForUser(o365MailsAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeRemovedAll() throws Exception {
    AuditEvent allRemForUser = new AllAttributesRemovedForUser(user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, allRemForUser);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeSet1() throws Exception {
    AuditEvent userSet = new AttributeSetForUser(preferredMailAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeSet2() throws Exception {
    AuditEvent userSet = new AttributeSetForUser(isMailAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeSet3() throws Exception {
    AuditEvent userSet = new AttributeSetForUser(publicMailsAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeSet4() throws Exception {
    AuditEvent userSet = new AttributeSetForUser(privateMailsAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Test
  public void resolveVirtualAttributeValueChangeSet5() throws Exception {
    AuditEvent userSet = new AttributeSetForUser(o365MailsAttr, user);
    List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
    assertTrue("audit should contain change of tcsMails", msgs.get(0).getMessage().contains(expectedTestOfMessage));
  }

  @Before
  public void setUp() throws Exception {
    tcsMailsAttrDef.setId(100);
    //prepare mocks
    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    AttributesManagerBl am = mock(AttributesManagerBl.class);
    UsersManagerBl um = mock(UsersManagerBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);
    when(perunBl.getAttributesManagerBl()).thenReturn(am);
    when(perunBl.getUsersManagerBl()).thenReturn(um);
    when(sess.getPerunBl().getAttributesManagerBl().getAttributes(sess, user,
        Arrays.asList(preferredMailAttr.getName(), isMailAttr.getName(), o365MailsAttr.getName(),
            publicMailsAttr.getName(), privateMailsAttr.getName()))).thenReturn(
        Arrays.asList(preferredMailAttr, isMailAttr, o365MailsAttr, publicMailsAttr, privateMailsAttr));
    when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, tcsMailsAttrDef.getName())).thenReturn(
        tcsMailsAttrDef);
  }

  private Attribute setUpUserAttribute(int id, String friendlyName, String type, Object value) {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setId(id);
    attrDef.setFriendlyName(friendlyName);
    attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attrDef.setType(type);
    Attribute attr = new Attribute(attrDef);
    attr.setValue(value);
    return attr;
  }
}
