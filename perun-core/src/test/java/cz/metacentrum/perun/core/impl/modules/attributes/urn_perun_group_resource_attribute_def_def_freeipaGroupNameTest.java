package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_resource_attribute_def_def_freeipaGroupNameTest {

	private urn_perun_group_resource_attribute_def_def_freeipaGroupName classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group();
	private Resource resource = new Resource();
	private PerunSessionImpl sess;
	private Attribute reqAttribute;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_resource_attribute_def_def_freeipaGroupName();
		attributeToCheck = new Attribute();
		sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		Facility facility = new Facility();
		reqAttribute = new Attribute();

		Group group2 = new Group();
		List<Group> groups = new ArrayList<>();
		groups.add(group2);
		groups.add(group);

		when(sess.getPerunBl().getResourcesManagerBl().getFacility(sess, resource)).thenReturn(facility);
		when(sess.getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource)).thenReturn(groups);
		when(sess.getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility)).thenReturn(Collections.singletonList(resource));
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group2, AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF + ":freeipaGroupName")).thenReturn(reqAttribute);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testWrongValue() throws Exception {
		System.out.println("testWrongValue()");
		attributeToCheck.setValue("bad@value");

		classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue("correct_value");

		classInstance.checkAttributeSyntax(sess, group, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testSemanticsWithNullValue() throws Exception {
		System.out.println("testSemanticsWithNullValue()");
		attributeToCheck.setValue(null);

		classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testSemanticsMultipleGroupsWithSameName() throws Exception {
		System.out.println("testSemanticsMultipleGroupsWithSameName()");
		attributeToCheck.setValue("correct_value");
		reqAttribute.setValue("correct_value");

		classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("correct_value");
		reqAttribute.setValue("another_correct_value");

		classInstance.checkAttributeSemantics(sess, group, resource, attributeToCheck);
	}
}
