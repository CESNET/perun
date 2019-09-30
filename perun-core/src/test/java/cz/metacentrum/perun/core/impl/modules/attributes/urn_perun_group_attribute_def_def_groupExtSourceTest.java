package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_groupExtSourceTest {

	private urn_perun_group_attribute_def_def_groupExtSource classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_groupExtSource();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);

		GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
		when(perunBl.getGroupsManagerBl()).thenReturn(groupsManagerBl);

		Vo groupVo = mock(Vo.class);
		VosManagerBl vosManagerBl = mock(VosManagerBl.class);
		when(perunBl.getVosManagerBl()).thenReturn(vosManagerBl);
		when(sess.getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId())).thenReturn(groupVo);

		ExtSource extSource = new ExtSource(1, "my_example", "type");
		ExtSourcesManagerBl extSourcesManagerBl = mock(ExtSourcesManagerBl.class);
		when(sess.getPerunBl().getExtSourcesManagerBl()).thenReturn(extSourcesManagerBl);
		when(sess.getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, groupVo)).thenReturn(Collections.singletonList(extSource));
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testWrongSemantics() throws Exception {
		System.out.println("testWrongSemantics()");
		attributeToCheck.setValue("my_bad_example");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("my_example");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}
}
