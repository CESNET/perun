package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_associatedUsersMailsTest {

  private static final String A_U_D_preferredMail = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";
  private final urn_perun_user_attribute_def_virt_associatedUsersMails classInstance =
      new urn_perun_user_attribute_def_virt_associatedUsersMails();
  private final User specificUser = new User(10, "Specific", "User", "", "", "");
  private final User user1 = new User(20, "First", "User", "", "", "");
  private final User user2 = new User(30, "Second", "User", "", "", "");
  private final String mailAbc = "abc@mail.com";
  private final String mailDef = "def@mail.com";
  private final Attribute user1MailAttribute = setUpUserPreferredMailAttribute(mailDef);
  private final Attribute user2MailAttribute = setUpUserPreferredMailAttribute(mailAbc);
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    sess = mock(PerunSessionImpl.class);
    PerunBl perunBl = mock(PerunBl.class);
    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    UsersManagerBl usersManagerBl = mock(UsersManagerBl.class);

    when(sess.getPerunBl()).thenReturn(perunBl);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(perunBl.getUsersManagerBl()).thenReturn(usersManagerBl);

    when(sess.getPerunBl().getUsersManagerBl().getUsersBySpecificUser(sess, specificUser))
        .thenReturn(Arrays.asList(user1, user2));
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user1, A_U_D_preferredMail))
        .thenReturn(user1MailAttribute);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user2, A_U_D_preferredMail))
        .thenReturn(user2MailAttribute);
  }

  private Attribute setUpUserPreferredMailAttribute(String mail) {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attrDef.setFriendlyName("preferredMail");
    attrDef.setType(String.class.getName());
    Attribute attr = new Attribute(attrDef);
    attr.setValue(mail);
    return attr;
  }

  @Test
  public void getAttributeValue() {
    List<String> attributeValue =
        classInstance.getAttributeValue(sess, specificUser, classInstance.getAttributeDefinition()).valueAsList();
    assertNotNull(attributeValue);
    assertEquals(2, attributeValue.size());
    // mails should be alphabetically sorted
    assertEquals(attributeValue.get(0), mailAbc);
    assertEquals(attributeValue.get(1), mailDef);
  }

  @Test
  public void getAttributeValueWithNullMail() throws Exception {
    // set user2's preferred mail to null
    Attribute nullUserMailAttribute = setUpUserPreferredMailAttribute(null);
    when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user2, A_U_D_preferredMail))
        .thenReturn(nullUserMailAttribute);

    List<String> attributeValue =
        classInstance.getAttributeValue(sess, specificUser, classInstance.getAttributeDefinition()).valueAsList();
    assertNotNull(attributeValue);
    // value shouldn't contain null mail
    assertEquals(1, attributeValue.size());
    assertTrue(attributeValue.contains(mailDef));
  }
}
