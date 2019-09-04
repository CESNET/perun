package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_groupStructureSynchronizationTimesTest {

	private urn_perun_group_attribute_def_def_groupStructureSynchronizationTimes classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private Attribute syncInterval;
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_groupStructureSynchronizationTimes();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		syncInterval = new Attribute(classInstance.getAttributeDefinition());
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);

		GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
		when(sess.getPerunBl().getGroupsManagerBl()).thenReturn(groupsManagerBl);
		when(sess.getPerunBl().getGroupsManagerBl().isGroupSynchronizedFromExternallSource(sess, group)).thenReturn(false);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUP_STRUCTURE_SYNCHRO_INTERVAL_ATTRNAME)).thenReturn(syncInterval);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testMissingReqAttribute() throws Exception {
		System.out.println("testMissingReqAttribute()");
		attributeToCheck.setValue(Collections.singleton("08:50"));
		syncInterval.setValue("true");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testWrongSyntax() throws Exception {
		System.out.println("testWrongSyntax()");
		List<String> value = new ArrayList<>();
		value.add("08:51");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		List<String> value = new ArrayList<>();
		value.add("08:50");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

}
