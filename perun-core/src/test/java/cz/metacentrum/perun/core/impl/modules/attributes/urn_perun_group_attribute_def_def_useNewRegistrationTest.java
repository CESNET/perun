package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_attribute_def_def_useNewRegistrationTest {

  private urn_perun_group_attribute_def_def_useNewRegistration classInstance;
  private PerunSessionImpl sess;
  private Group group = new Group();
  GroupsManagerBl groupsManagerBl;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_group_attribute_def_def_useNewRegistration();
    sess = mock(PerunSessionImpl.class);

    //perunBl
    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    groupsManagerBl = mock(GroupsManagerBl.class);
    when(perunBl.getGroupsManagerBl()).thenReturn(groupsManagerBl);
  }

  @Test
  public void testCannotEnableNewRegistrarForAutoRegistrationGroup()
          throws Exception {

    Attribute attribute =
            new Attribute(classInstance.getAttributeDefinition());
    attribute.setValue(true);

    when(groupsManagerBl
            .isGroupForAnyAutoRegistration(sess, group))
            .thenReturn(true);

    assertThatThrownBy(
            () -> classInstance.checkAttributeSemantics(
                    sess, group, attribute))
            .isInstanceOf(WrongReferenceAttributeValueException.class)
            .hasMessageContaining("used for auto registration");
  }
}
