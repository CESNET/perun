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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_synchronizationEnabledTest {

	private urn_perun_group_attribute_def_def_synchronizationEnabled classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private Attribute reqAttribute;
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_synchronizationEnabled();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		reqAttribute = new Attribute(classInstance.getAttributeDefinition());
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);

		GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
		when(sess.getPerunBl().getGroupsManagerBl()).thenReturn(groupsManagerBl);
		when(sess.getPerunBl().getGroupsManagerBl().isGroupSynchronizedFromExternallSource(sess, group)).thenReturn(false);

		AttributesManagerBl attributesManagerBl;
		attributesManagerBl = mock(AttributesManagerBl.class);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME)).thenReturn(reqAttribute);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME)).thenReturn(reqAttribute);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testMissingReqAttribute() throws Exception {
		System.out.println("testMissingReqAttribute()");
		attributeToCheck.setValue("true");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("true");
		reqAttribute.setValue("value");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testWrongSyntax() throws Exception {
		System.out.println("testWrongSyntax()");
		attributeToCheck.setValue("value");

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");

		attributeToCheck.setValue("true");
		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);

		attributeToCheck.setValue("false");
		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}


}
