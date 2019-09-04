package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_unixGroupName_namespaceTest {
	private urn_perun_group_attribute_def_def_unixGroupName_namespace classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_unixGroupName_namespace();
		attributeToCheck = new Attribute();
		attributeToCheck.setFriendlyName("unixGID-namespace");

		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:")).thenReturn(attributeToCheck);

		GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
		when(sess.getPerunBl().getGroupsManagerBl()).thenReturn(groupsManagerBl);
		when(sess.getPerunBl().getGroupsManagerBl().isGroupSynchronizedFromExternallSource(sess, group)).thenReturn(false);

		ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
		when(sess.getPerunBl().getModulesUtilsBl()).thenReturn(modulesUtilsBl);

		ResourcesManagerBl resourcesManagerBl = mock(ResourcesManagerBl.class);
		when(sess.getPerunBl().getResourcesManagerBl()).thenReturn(resourcesManagerBl);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testGroupWithSameGID() throws Exception {
		System.out.println("testGroupWithSameGID()");
		attributeToCheck.setValue("my name");
		Group group2 = new Group(2,"group2","Group 2",null,null,null,null,0,0);
		List<Group> listOfGroups = new ArrayList<>();
		listOfGroups.add(group2);
		when(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, attributeToCheck)).thenReturn(listOfGroups);
		when(sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:")).thenReturn(Collections.singletonList(attributeToCheck));
		when(sess.getPerunBl().getModulesUtilsBl().haveRightToWriteAttributeInAnyGroupOrResource(sess, listOfGroups, new ArrayList<>(), attributeToCheck, attributeToCheck)).thenReturn(true);
		when(sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, group2, attributeToCheck)).thenReturn(2);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testResourceWithSameGID() throws Exception {
		System.out.println("testResourceWithSameGID()");
		attributeToCheck.setValue("my name");
		Resource resource = mock(Resource.class);
		when(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, attributeToCheck)).thenReturn(Collections.singletonList(resource));
		when(sess.getPerunBl().getModulesUtilsBl().getListOfResourceGIDsFromListOfGroupGIDs(sess, new ArrayList<>())).thenReturn(Collections.singletonList(attributeToCheck));
		when(sess.getPerunBl().getModulesUtilsBl().haveRightToWriteAttributeInAnyGroupOrResource(sess, new ArrayList<>(), Collections.singletonList(resource), attributeToCheck, attributeToCheck)).thenReturn(true);
		when(sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, resource, attributeToCheck)).thenReturn(2);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue("my name");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}
}
