package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_attribute_def_def_autoApproveByGroupMembershipTest {
  private urn_perun_group_attribute_def_def_autoApproveByGroupMembership classInstance;
  private Attribute attributeToCheck;
  private PerunSessionImpl sess;
  private Group group;
  private Group autoApproveGroup;
  private Vo vo;
  private ArrayList<String> groupIds;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_autoApproveByGroupMembership();
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());

    sess = mock(PerunSessionImpl.class);
    group = mock(Group.class);
    vo = new Vo(1, "Test Vo", "Test");
    autoApproveGroup = new Group(1, "Test Group", "Test", vo.getId());
    groupIds = new ArrayList<>() {{
      add(String.valueOf(autoApproveGroup.getId()));
    }};

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    VosManagerBl vosManagerBl = mock(VosManagerBl.class);
    when(perunBl.getVosManagerBl()).thenReturn(vosManagerBl);

    GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
    when(perunBl.getGroupsManagerBl()).thenReturn(groupsManagerBl);
  }

  @Test
  public void testCheckCorrectSyntax() throws Exception {
    System.out.println("testCheckCorrectSyntax");
    attributeToCheck.setValue(new ArrayList<>() {{
      add("123");
    }});

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test
  public void testCheckCorrectSyntaxNull() throws Exception {
    System.out.println("testCheckCorrectSyntaxNull");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckIncorrectSyntax() throws Exception {
    System.out.println("testCheckIncorrectSyntax");
    attributeToCheck.setValue(new ArrayList<>() {{
      add("test");
    }});

    classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
  }

  @Test
  public void testSemanticsWithCorrectValue() throws Exception {
    System.out.println("testSemanticsWithCorrectValue()");
    attributeToCheck.setValue(groupIds);

    when(group.getVoId()).thenReturn(vo.getId());
    when(sess.getPerunBl().getVosManagerBl().getVoById(sess, vo.getId())).thenReturn(vo);
    when(sess.getPerunBl().getGroupsManagerBl().getGroups(sess, vo)).thenReturn(List.of(autoApproveGroup));

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithIncorrectValue() throws Exception {
    System.out.println("testSemanticsWithIncorrectValue()");
    attributeToCheck.setValue(groupIds);

    when(group.getVoId()).thenReturn(vo.getId());
    when(sess.getPerunBl().getVosManagerBl().getVoById(sess, vo.getId())).thenReturn(vo);
    when(sess.getPerunBl().getGroupsManagerBl().getGroups(sess, vo)).thenReturn(new ArrayList<>());

    classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
  }
}
