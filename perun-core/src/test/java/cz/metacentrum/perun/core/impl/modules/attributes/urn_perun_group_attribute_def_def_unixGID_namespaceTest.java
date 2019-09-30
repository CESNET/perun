/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_unixGID_namespaceTest {
	private urn_perun_group_attribute_def_def_unixGID_namespace classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private Attribute reqAttribute;
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_unixGID_namespace();
		attributeToCheck = new Attribute();
		attributeToCheck.setFriendlyName("friendly name");
		reqAttribute = new Attribute();
		reqAttribute.setFriendlyName("friendly name");
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);

		GroupsManagerBl groupsManagerBl = mock(GroupsManagerBl.class);
		when(sess.getPerunBl().getGroupsManagerBl()).thenReturn(groupsManagerBl);
		when(sess.getPerunBl().getGroupsManagerBl().isGroupSynchronizedFromExternallSource(sess, group)).thenReturn(false);

		AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace" + ":" + attributeToCheck.getNamespace())).thenReturn(reqAttribute);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, "", AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":usedGids")).thenReturn(reqAttribute);
		when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:")).thenReturn(reqAttribute);
		when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace:")).thenReturn(reqAttribute);

		ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
		when(sess.getPerunBl().getModulesUtilsBl()).thenReturn(modulesUtilsBl);

		ResourcesManagerBl resourcesManagerBl = mock(ResourcesManagerBl.class);
		when(sess.getPerunBl().getResourcesManagerBl()).thenReturn(resourcesManagerBl);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testUnixGroupNameSet() throws Exception {
		System.out.println("testUnixGroupNameSet()");
		Set<String> set = new HashSet<>();
		set.add(attributeToCheck.getNamespace());
		when(sess.getPerunBl().getModulesUtilsBl().getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace(sess, new ArrayList<>(), attributeToCheck)).thenReturn(set);
		reqAttribute.setValue("value");

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testDepletedGIDValue() throws Exception {
		System.out.println("testDepletedGIDValue()");
		attributeToCheck.setValue(5);
		Map<String, String> map = new LinkedHashMap<>();
		map.put("D5", "value");
		reqAttribute.setValue(map);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testGroupWithSameGID() throws Exception {
		System.out.println("testGroupWithSameGID()");
		attributeToCheck.setValue(5);
		Group group2 = new Group(2,"group2","Group 2",null,null,null,null,0,0);
		when(sess.getPerunBl().getGroupsManagerBl().getGroupsByAttribute(sess, attributeToCheck)).thenReturn(Arrays.asList(group, group2));
		when(sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:")).thenReturn(Collections.singletonList(attributeToCheck));
		when(sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, group2, attributeToCheck)).thenReturn(2);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testResourceWithSameGID() throws Exception {
		System.out.println("testResourceWithSameGID()");
		attributeToCheck.setValue(5);
		Resource resource = mock(Resource.class);
		when(sess.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(sess, attributeToCheck)).thenReturn(Collections.singletonList(resource));
		when(sess.getPerunBl().getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace:")).thenReturn(Collections.singletonList(attributeToCheck));
		when(sess.getPerunBl().getModulesUtilsBl().haveTheSameAttributeWithTheSameNamespace(sess, resource, attributeToCheck)).thenReturn(2);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		attributeToCheck.setValue(5);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSyntax() throws Exception {
		System.out.println("testCorrectSyntax()");
		attributeToCheck.setValue(5);

		classInstance.checkAttributeSyntax(sess, group, attributeToCheck);
	}
}
