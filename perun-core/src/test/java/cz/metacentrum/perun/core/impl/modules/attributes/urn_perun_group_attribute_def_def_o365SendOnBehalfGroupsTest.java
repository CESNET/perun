package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_group_attribute_def_def_o365SendOnBehalfGroupsTest {

  private urn_perun_group_attribute_def_def_o365SendOnBehalfGroups classInstance;
  private Attribute attributeToCheck;
  private Group group = new Group(1, "group1", "Group 1", null, null, null, null, 0, 0);
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_o365SendOnBehalfGroups();
    sess = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
  }

  @Test
  public void testCheckCorrectSyntax() throws Exception {
    System.out.println("testCheckCorrectSyntax");
    List<String> value = new ArrayList<>();
    value.add("123");
    attributeToCheck.setValue(value);

    assertThatNoException().isThrownBy(() -> classInstance.checkAttributeSyntax(sess, group, attributeToCheck));
  }

  @Test
  public void testCheckCorrectSyntaxNull() throws Exception {
    System.out.println("testCheckCorrectSyntaxNull");
    attributeToCheck.setValue(null);

    assertThatNoException().isThrownBy(() -> classInstance.checkAttributeSyntax(sess, group, attributeToCheck));
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckIncorrectSyntaxIdNotNumber() throws Exception {
    System.out.println("testCheckIncorrectSyntaxIdNotNumber");
    List<String> value = new ArrayList<>();
    value.add("ahoj");
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }
}
