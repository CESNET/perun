package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.QuotaNotInAllowedLimitException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_namespace_GIDRanges;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_member_resource_attribute_def_def_dataQuotasOverride;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_member_resource_attribute_def_def_fileQuotasOverride;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_resource_attribute_def_def_defaultDataQuotas;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_resource_attribute_def_def_defaultFileQuotas;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_resource_attribute_def_def_maxUserDataQuotas;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_resource_attribute_def_def_maxUserFileQuotas;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests of ModulesUtils
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class ModulesUtilsEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private ModulesUtilsBl modulesUtilsBl;
	private final static String CLASS_NAME = "ModulesUtilsBl.";

	// these are in DB only when setUp"Type"() and must be used in correct (this) order
	private Vo vo;
	private Service service;
	private Host host;
	private Member member;
	private Facility facility;
	private Resource resource;
	private List<Attribute> attributes; // always have just 1 attribute we setUp"AttrType"()
	private Group group;

	private final String namespace = "someNamespace";

	@Before
	public void setUp() {
		modulesUtilsBl = perun.getModulesUtilsBl();
	}

	@Test
	public void haveTheSameAttributeWithTheSameNamespace1ForResource() throws Exception {
		System.out.println(CLASS_NAME + "haveTheSameAttributeWithTheSameNamespace1");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		List<Attribute> attrs = setUpResourceAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, resource, attrs.get(0));

		assertEquals(0, modulesUtilsBl.haveTheSameAttributeWithTheSameNamespace((PerunSessionImpl) sess, resource, attrs.get(0)));
	}

	@Test
	public void haveTheSameAttributeWithTheSameNamespace2ForResource() throws Exception {
		System.out.println(CLASS_NAME + "haveTheSameAttributeWithTheSameNamespace2");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		List<Attribute> attrs = setUpResourceAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, resource, attrs.get(0));
		Attribute attr = attrs.get(0);
		attr.setValue("Jina value");

		assertEquals(1, modulesUtilsBl.haveTheSameAttributeWithTheSameNamespace((PerunSessionImpl) sess, resource, attrs.get(0)));
	}

	@Test
	public void haveTheSameAttributeWithTheSameNamespace3ForResource() throws Exception {
		System.out.println(CLASS_NAME + "haveTheSameAttributeWithTheSameNamespace3");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		List<Attribute> attrs = setUpResourceAttribute();

		assertEquals(-1, modulesUtilsBl.haveTheSameAttributeWithTheSameNamespace((PerunSessionImpl) sess, resource, attrs.get(0)));
	}

	@Test
	public void haveTheSameAttributeWithTheSameNamespace1ForGroup() throws Exception {
		System.out.println(CLASS_NAME + "haveTheSameAttributeWithTheSameNamespace1ForGroup");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		List<Attribute> attrs = setUpGroupAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, group, attrs.get(0));

		assertEquals(0, modulesUtilsBl.haveTheSameAttributeWithTheSameNamespace((PerunSessionImpl) sess, group, attrs.get(0)));
	}

	@Test
	public void haveTheSameAttributeWithTheSameNamespace2ForGroup() throws Exception {
		System.out.println(CLASS_NAME + "haveTheSameAttributeWithTheSameNamespace2ForGroup");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		List<Attribute> attrs = setUpGroupAttribute();
		perun.getAttributesManagerBl().setAttribute(sess, group, attrs.get(0));
		Attribute attr = attrs.get(0);
		attr.setValue("Jina value");

		assertEquals(1, modulesUtilsBl.haveTheSameAttributeWithTheSameNamespace((PerunSessionImpl) sess, group, attrs.get(0)));
	}

	@Test
	public void haveTheSameAttributeWithTheSameNamespace3ForGroup() throws Exception {
		System.out.println(CLASS_NAME + "haveTheSameAttributeWithTheSameNamespace3ForGroup");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		List<Attribute> attrs = setUpGroupAttribute();

		assertEquals(-1, modulesUtilsBl.haveTheSameAttributeWithTheSameNamespace((PerunSessionImpl) sess, group, attrs.get(0)));
	}

	@Test
	public void getCommonGIDOfResourcesWithSameNameInSameNamespace() throws Exception {
		System.out.println(CLASS_NAME + "getCommonGIDOfResourcesWithSameNameInSameNamespace");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		List<Resource> resources = new ArrayList<>();
		resources.add(resource);

		Attribute gidRanges = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges"));
		Map<String, String> gidRangesValue = new LinkedHashMap<>();
		gidRangesValue.put("100", "100500");
		gidRanges.setValue(gidRangesValue);

		perun.getAttributesManagerBl().setAttribute(sess, namespace, gidRanges);

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		Attribute resourceGID = null;
		for(Attribute a: attributes) {
			if(a.getName().equals(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace)) {

				resourceGID = a;
				break;
			}
		}
		perun.getAttributesManagerBl().setAttribute(sess, resource, resourceGID);

		assertEquals(new Integer(112), modulesUtilsBl.getCommonGIDOfResourcesWithSameNameInSameNamespace((PerunSessionImpl) sess, resources, namespace, null));
	}

	@Test
	public void getCommonGIDOfGroupsWithSameNameInSameNamespace() throws Exception {
		System.out.println(CLASS_NAME + "getCommonGIDOfGroupsWithSameNameInSameNamespace");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		List<Group> groups = new ArrayList<>();
		groups.add(group);

		Attribute gidRanges = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges"));
		Map<String, String> gidRangesValue = new LinkedHashMap<>();
		gidRangesValue.put("100", "100500");
		gidRanges.setValue(gidRangesValue);

		perun.getAttributesManagerBl().setAttribute(sess, namespace, gidRanges);

		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		Attribute groupGID = null;
		for(Attribute a: attributes) {
			if(a.getName().equals(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)) {

				groupGID = a;
				break;
			}
		}
		perun.getAttributesManagerBl().setAttribute(sess, group, groupGID);

		assertEquals(new Integer(111), modulesUtilsBl.getCommonGIDOfGroupsWithSameNameInSameNamespace((PerunSessionImpl) sess, groups, namespace, null));
	}

	@Test
	public void getFreeGID() throws Exception {
		System.out.println(CLASS_NAME + "getFreeGID");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		Attribute gidRanges = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges"));
		Map<String, String> gidRangesValue = new LinkedHashMap<>();
		gidRangesValue.put("100", "100500");
		gidRanges.setValue(gidRangesValue);

		perun.getAttributesManagerBl().setAttribute(sess, namespace, gidRanges);

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		Attribute groupGID = null;
		Attribute resourceGID = null;
		for(Attribute a: attributes) {
			if(a.getName().equals(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace)) {
				groupGID = a;
			} else if(a.getName().equals(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace)) {
				resourceGID = a;
			}
		}

		Integer freeGIDForGroupAttribute = modulesUtilsBl.getFreeGID((PerunSessionImpl) sess, resourceGID);
		Integer freeGIDForResourceAttribute = modulesUtilsBl.getFreeGID((PerunSessionImpl) sess, groupGID);
		assertTrue(freeGIDForGroupAttribute != null);
		assertTrue(freeGIDForResourceAttribute != null);
		assertEquals(freeGIDForGroupAttribute, freeGIDForResourceAttribute);
	}

	@Test
	public void checkIfGIDIsWithinRange() throws Exception {
		System.out.println(CLASS_NAME + "checkIfGIDIsWithinRange");

		Attribute gidRanges = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges"));
		Map<String, String> gidRangesValue = new LinkedHashMap<>();
		gidRangesValue.put("100000", "100500");
		gidRanges.setValue(gidRangesValue);

		perun.getAttributesManagerBl().setAttribute(sess, namespace, gidRanges);

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		int i = 0;
		for(Attribute a: attributes) {
			if(a.getFriendlyName().startsWith("unixGID-namespace")) {
				if(i == 0) a.setValue(2000);
				else a.setValue(100005);
				try {
					modulesUtilsBl.checkIfGIDIsWithinRange((PerunSessionImpl) sess, a);
				} catch (WrongReferenceAttributeValueException ex) {
					i++;
				}
			}
		}
		assertEquals(1, i);
	}

	@Test
	public void haveRightToWriteAttributeInAnyGroupOrResource() throws Exception {
		System.out.println(CLASS_NAME + "haveRightToWriteAttributeInAnyGroupOrResource");

		Attribute gidRanges = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges"));
		Map<String, String> gidRangesValue = new LinkedHashMap<>();
		gidRangesValue.put("100", "100500");
		gidRanges.setValue(gidRangesValue);
		perun.getAttributesManagerBl().setAttribute(sess, namespace, gidRanges);

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		List<Group> groups = new ArrayList<>();
		List<Resource> resources = new ArrayList<>();
		groups.add(group);
		resources.add(resource);

		assertTrue(modulesUtilsBl.haveRightToWriteAttributeInAnyGroupOrResource((PerunSessionImpl) sess, groups, null, gidRanges, gidRanges));
		assertTrue(modulesUtilsBl.haveRightToWriteAttributeInAnyGroupOrResource((PerunSessionImpl) sess, null, resources, gidRanges, gidRanges));
		assertTrue(modulesUtilsBl.haveRightToWriteAttributeInAnyGroupOrResource((PerunSessionImpl) sess, groups, resources, gidRanges, gidRanges));
		assertFalse(modulesUtilsBl.haveRightToWriteAttributeInAnyGroupOrResource((PerunSessionImpl) sess, null, null, gidRanges, gidRanges));
	}

	@Test
	public void getListOfResourceGIDsFromListOfGroupGIDs() throws Exception {
		System.out.println(CLASS_NAME + "getListOfResourceGIDsFromListOfGroupGIDs");

		List<Attribute> groupAttr = new ArrayList<>();
		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();

		for(Attribute a: attributes) {
			if(a.getName().startsWith(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID")) {
				groupAttr.add(a);
				break;
			}
		}

		List<Attribute> resourceAttr = modulesUtilsBl.getListOfResourceGIDsFromListOfGroupGIDs((PerunSessionImpl) sess, groupAttr);

		assertEquals(1, resourceAttr.size());
		assertTrue(resourceAttr.get(0).getName().equals(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace));
	}

	@Test
	public void getListOfGroupGIDsFromListOfResourceGIDs() throws Exception {
		System.out.println(CLASS_NAME + "getListOfGroupGIDsFromListOfResourceGIDs");

		List<Attribute> resourceAttr = new ArrayList<>();
		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();

		for(Attribute a: attributes) {
			if(a.getName().startsWith(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID")) {
				resourceAttr.add(a);
				break;
			}
		}

		List<Attribute> groupAttr = modulesUtilsBl.getListOfGroupGIDsFromListOfResourceGIDs((PerunSessionImpl) sess, resourceAttr);

		assertEquals(1, groupAttr.size());
		assertTrue(groupAttr.get(0).getName().equals(AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace));
	}

	@Test
	public void getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace() throws Exception {
		System.out.println(CLASS_NAME + "getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		List<Facility> facilities = new ArrayList<>();
		facilities.add(facility);

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		List<Attribute> facilityAttributes = setUpFacilityGroupNameAndGIDNamespaceAttributes();
		for(Attribute a: facilityAttributes) {
			perun.getAttributesManagerBl().setAttribute(sess, facility, a);
		}

		for(Attribute a: attributes) {
			if(a.getFriendlyName().startsWith("unixGroupName")) {
				Set<String> groupNamesSet = modulesUtilsBl.getSetOfGIDNamespacesWhereFacilitiesHasTheSameGroupNameNamespace((PerunSessionImpl) sess, facilities, a);
				assertEquals(1, groupNamesSet.size());
				assertTrue(groupNamesSet.contains(namespace));
			}
		}
	}

	@Test
	public void getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace() throws Exception {
		System.out.println(CLASS_NAME + "getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		List<Facility> facilities = new ArrayList<>();
		facilities.add(facility);

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		List<Attribute> facilityAttributes = setUpFacilityGroupNameAndGIDNamespaceAttributes();
		for(Attribute a: facilityAttributes) {
			perun.getAttributesManagerBl().setAttribute(sess, facility, a);
		}

		for(Attribute a: attributes) {
			if(a.getFriendlyName().startsWith("unixGID")) {
				Set<String> groupNamesSet = modulesUtilsBl.getSetOfGroupNameNamespacesWhereFacilitiesHasTheSameGIDNamespace((PerunSessionImpl) sess, facilities, a);
				assertEquals(1, groupNamesSet.size());
				assertTrue(groupNamesSet.contains(namespace));
			}
		}
	}

	@Test
	public void checkReservedNames() throws Exception {
		System.out.println(CLASS_NAME + "checkReservedNames");

		String goodName = "cokoliv";
		String badName = "sys";

		Attribute attr = new Attribute();
		attr.setValue(goodName);

		modulesUtilsBl.checkReservedUnixGroupNames(attr);

		attr.setValue(badName);
		boolean ok = false;

		try {
			modulesUtilsBl.checkReservedUnixGroupNames(attr);
		} catch (WrongAttributeValueException ex) {
			ok = true;
		}

		assertTrue(ok);
	}

	@Test
	public void getUnixGroupNameAndGIDNamespaceAttributeWithNotNullValue() throws Exception {
		System.out.println(CLASS_NAME + "getUnixGroupNameAndGIDNamespaceAttributeWithNotNullValue");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		List<Attribute> facilityAttributes = setUpFacilityGroupNameAndGIDNamespaceAttributes();
		for(Attribute a: facilityAttributes) {
			perun.getAttributesManagerBl().setAttribute(sess, facility, a);
		}

		Attribute unixGroupNameNamespace = modulesUtilsBl.getUnixGroupNameNamespaceAttributeWithNotNullValue((PerunSessionImpl) sess, resource);
		Attribute unixGIDNamespace = modulesUtilsBl.getUnixGIDNamespaceAttributeWithNotNullValue((PerunSessionImpl) sess, resource);

		assertTrue(facilityAttributes.size() == 2);
		assertTrue(facilityAttributes.contains(unixGroupNameNamespace));
		assertTrue(facilityAttributes.contains(unixGIDNamespace));
	}

	@Test
	public void isGroupUnixGIDNamespaceFillable() throws Exception {
		System.out.println(CLASS_NAME + "isGroupUnixGIDNamespaceFillable");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		group = setUpGroup();

		Attribute gidRanges = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-GIDRanges"));
		Map<String, String> gidRangesValue = new LinkedHashMap<>();
		gidRangesValue.put("100", "100500");
		gidRanges.setValue(gidRangesValue);
		perun.getAttributesManagerBl().setAttribute(sess, namespace, gidRanges);

		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		List<Attribute> attributes = setUpGroupNamesAndGIDForGroupAndResource();
		List<Attribute> facilityAttributes = setUpFacilityGroupNameAndGIDNamespaceAttributes();
		for(Attribute a: facilityAttributes) {
			perun.getAttributesManagerBl().setAttribute(sess, facility, a);
		}
		Attribute unixGIDNamespace = null;

		for(Attribute a: attributes) {
			if(a.getNamespace().equals(AttributesManager.NS_GROUP_ATTR_DEF)) {
				perun.getAttributesManagerBl().setAttribute(sess, group, a);
				if(a.getFriendlyName().startsWith("unixGID")) unixGIDNamespace = a;
			}
		}
		assertTrue(modulesUtilsBl.isGroupUnixGIDNamespaceFillable((PerunSessionImpl) sess, group, unixGIDNamespace));
	}

	@Test
	public void isNameOfEmailValid() {
		System.out.println(CLASS_NAME + "isNameOfEmailValid");

		assertTrue(modulesUtilsBl.isNameOfEmailValid((PerunSessionImpl) sess, "user@domain.com"));
		assertTrue(modulesUtilsBl.isNameOfEmailValid((PerunSessionImpl) sess, "first.second@domain.second.com"));
		assertFalse(modulesUtilsBl.isNameOfEmailValid((PerunSessionImpl) sess, "first.seconddomain.second.com"));
		assertFalse(modulesUtilsBl.isNameOfEmailValid((PerunSessionImpl) sess, "first.second@domain.second..com"));
	}

	@Test
	public void testShellValue() throws Exception {
		System.out.println(CLASS_NAME + "testShellValue");

		Attribute attr = new Attribute();
		String shell = "/bin/csh";

		modulesUtilsBl.checkFormatOfShell(shell, attr);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testShellValueWrongFormat() throws Exception {
		System.out.println(CLASS_NAME + "testShellValueWrongFormat");

		Attribute attr = new Attribute();
		String shell = "/bin/bash/";

		modulesUtilsBl.checkFormatOfShell(shell, attr);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void testShellValueWrongFormatInvalidCharacter() throws Exception {
		System.out.println(CLASS_NAME + "testShellValueWrongFormatInvalidCharacter");

		Attribute attr = new Attribute();
		String shell = "/\n";

		modulesUtilsBl.checkFormatOfShell(shell, attr);
	}

	@Test
	public void checkAndConvertGIDRangesWhenAllIsOK() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenAllIsOK");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		Map<Integer, Integer> convertedMap = modulesUtilsBl.checkAndConvertGIDRanges(attribute);
		assertEquals(value.size(), convertedMap.size());
		for(Integer minimum : convertedMap.keySet()) {
			assertTrue(value.containsKey(minimum.toString()));
			assertEquals(value.get(minimum.toString()), convertedMap.get(minimum).toString());
		}
	}

	@Test(expected=InternalErrorException.class)
	public void checkAndConvertGIDRangesWhenNullAttribute() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenNullAttribute");
		modulesUtilsBl.checkAndConvertGIDRanges(null);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenEmptyValue() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenEmptyValue");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("7", "");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenNullValue() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenNullValue");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("7", null);
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenEmptyKey() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenEmptyKey");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("", "7");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenNullKey() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenNullKey");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put(null, "7");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenBadMinimum1() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenBadMinimum1");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("0", "0");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenBadMinimum2() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenBadMinimum2");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("-5", "-1");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenMaximumLessThanMinimum() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenMaximumLessThanMinimum");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("9", "8");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenKeyIsNotNumber() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenKeyIsNotNumber");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("6s", "7");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenValueIsNotNumber() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenValueIsNotNumber");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("6", "s7");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAndConvertGIDRangesWhenOverlapExist() throws Exception {
		System.out.println(CLASS_NAME + "checkAndConvertGIDRangesWhenValueIsNotNumber");
		Attribute attribute = getGIDRangesAttributeWithValidValue();
		Map<String, String> value = (LinkedHashMap) attribute.getValue();
		value.put("2000", "3000");
		attribute.setValue(value);
		modulesUtilsBl.checkAndConvertGIDRanges(attribute);
	}

	@Test
	public void checkIfQuotasIsInLimit() throws Exception {
		System.out.println(CLASS_NAME + "checkIfQuotasIsInLimit");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute fileQuotasAttribute = getFileQuotasAttribute();
		Attribute maxFileQuotasAttribute = getMaxFileQuotasAttribute();
		modulesUtilsBl.checkIfQuotasIsInLimit(
				modulesUtilsBl.checkAndTransferQuotas(fileQuotasAttribute, resource, null, false),
				modulesUtilsBl.checkAndTransferQuotas(maxFileQuotasAttribute, resource, null, false));
		Attribute dataQuotasAttribute = getDataQuotasAttribute();
		Attribute maxDataQuotasAttribute = getMaxDataQuotasAttribute();
		modulesUtilsBl.checkIfQuotasIsInLimit(
				modulesUtilsBl.checkAndTransferQuotas(dataQuotasAttribute, resource, null, true),
				modulesUtilsBl.checkAndTransferQuotas(maxDataQuotasAttribute, resource, null, true));
	}

	@Test(expected= QuotaNotInAllowedLimitException.class)
	public void checkIfFileQuotasIsNotInLimit() throws Exception {
		System.out.println(CLASS_NAME + "QuotaNotInAllowedLimitException");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute fileQuotasAttribute = getFileQuotasAttribute();
		Attribute maxFileQuotasAttribute = getMaxFileQuotasAttribute();
		((LinkedHashMap<String,String>) maxFileQuotasAttribute.getValue()).put("/var/log/test/", "1000:2000");
		modulesUtilsBl.checkIfQuotasIsInLimit(
				modulesUtilsBl.checkAndTransferQuotas(fileQuotasAttribute, resource, null, false),
				modulesUtilsBl.checkAndTransferQuotas(maxFileQuotasAttribute, resource, null, false));
	}

	@Test(expected= QuotaNotInAllowedLimitException.class)
	public void checkIfDataQuotasIsNotInLimit() throws Exception {
		System.out.println(CLASS_NAME + "QuotaNotInAllowedLimitException");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute dataQuotasAttribute = getDataQuotasAttribute();
		((LinkedHashMap<String,String>) dataQuotasAttribute.getValue()).put("/var/log/test/", "1G:0");
		Attribute maxDataQuotasAttribute = getMaxDataQuotasAttribute();
		modulesUtilsBl.checkIfQuotasIsInLimit(
				modulesUtilsBl.checkAndTransferQuotas(dataQuotasAttribute, resource, null, true),
				modulesUtilsBl.checkAndTransferQuotas(maxDataQuotasAttribute, resource, null, true));
	}

	@Test
	public void checkQuotas() throws Exception {
		System.out.println(CLASS_NAME + "checkQuotas");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute fileQuotasAttribute = getFileQuotasAttribute();
		Attribute dataQuotasAttribute = getDataQuotasAttribute();
		modulesUtilsBl.checkAndTransferQuotas(fileQuotasAttribute, resource, null, false);
		modulesUtilsBl.checkAndTransferQuotas(dataQuotasAttribute, resource, null, true);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkQuotasErrorInMetric() throws Exception {
		System.out.println(CLASS_NAME + "checkQuotasErrorInMetric");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute dataQuotasAttribute = getDataQuotasAttribute();
		((LinkedHashMap) dataQuotasAttribute.getValue()).put("/new/path/", "1Z:1");
		modulesUtilsBl.checkAndTransferQuotas(dataQuotasAttribute, resource, null, true);
	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkQuotasErrorInValue() throws Exception {
		System.out.println(CLASS_NAME + "WrongAttributeValueException");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute dataQuotasAttribute = getDataQuotasAttribute();
		((LinkedHashMap) dataQuotasAttribute.getValue()).put("/new/path/", "b20:1");
		modulesUtilsBl.checkAndTransferQuotas(dataQuotasAttribute, resource, null, true);
	}

	@Test(expected= QuotaNotInAllowedLimitException.class)
	public void checkIfQuotasContainsDifferentPathThanMaxQuotas() throws Exception {
		System.out.println(CLASS_NAME + "checkIfQuotasContainsDifferentPathThanMaxQuotas");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute dataQuotasAttribute = getDataQuotasAttribute();
		((LinkedHashMap) dataQuotasAttribute.getValue()).put("/new/path/", "1:1");
		Attribute maxDataQuotasAttribute = getMaxDataQuotasAttribute();
		modulesUtilsBl.checkIfQuotasIsInLimit(
				modulesUtilsBl.checkAndTransferQuotas(dataQuotasAttribute, resource, null, true),
				modulesUtilsBl.checkAndTransferQuotas(maxDataQuotasAttribute, resource, null, true));
	}

	@Test
	public void transferQuotasBackToAttributeValue() throws Exception {
		System.out.println(CLASS_NAME + "transferQuotasBackToAttributeValue");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute fileQuotasAttribute = getFileQuotasAttribute();
		assertEquals(
				fileQuotasAttribute.getValue(),
				modulesUtilsBl.transferQuotasBackToAttributeValue(modulesUtilsBl.checkAndTransferQuotas(fileQuotasAttribute, resource, null, false), false));
		Attribute dataQuotasAttribute = getDataQuotasAttribute();
		assertEquals(
				dataQuotasAttribute.getValue(),
				modulesUtilsBl.transferQuotasBackToAttributeValue(modulesUtilsBl.checkAndTransferQuotas(dataQuotasAttribute, resource, null, true), true));
	}

	@Test
	public void mergeMemberAndResourceTransferredQuotas() throws Exception {
		System.out.println(CLASS_NAME + "mergeMemberAndResourceTransferredQuotas");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute resourceFileQuotasAttribute = getFileQuotasAttribute();
		Attribute memberResourceFileQuotasAttribute = getMaxFileQuotasAttribute();
		Attribute fileQuotasOverrideAttribute = getOverrideFileQuotasAttribute();
		//Override has the highest priority, then member-resource settings and the last priority has resource settings
		Map<String, String> mergedMapOfQuotas = modulesUtilsBl.transferQuotasBackToAttributeValue(
				modulesUtilsBl.mergeMemberAndResourceTransferredQuotas(
						modulesUtilsBl.checkAndTransferQuotas(resourceFileQuotasAttribute, resource, null, false),
						modulesUtilsBl.checkAndTransferQuotas(memberResourceFileQuotasAttribute, resource, null, false),
						modulesUtilsBl.checkAndTransferQuotas(fileQuotasOverrideAttribute, resource, null, false)),
				false );
		Map<String, String> expectedResult = new HashMap<>((HashMap<String,String>) memberResourceFileQuotasAttribute.getValue());
		expectedResult.put("/var/log/test/","1:0");
		assertEquals(expectedResult, mergedMapOfQuotas);
	}

	@Test
	public void countUserFacilityQuotas() throws Exception {
		System.out.println(CLASS_NAME + "countUserFacilityQuotas");
		Resource resource = new Resource(10, "test", "test", 10, 10);
		Attribute resourceFileQuotasAttribute = getFileQuotasAttribute();
		Attribute memberResourceFileQuotasAttribute = getMaxFileQuotasAttribute();
		Attribute fileQuotasOverrideAttribute = getOverrideFileQuotasAttribute();
		//All quotas will be the sum for every unique path in any map
		List<Map<String, Pair<BigDecimal, BigDecimal>>> countOfQuotas = new ArrayList<>();
		countOfQuotas.add(modulesUtilsBl.checkAndTransferQuotas(resourceFileQuotasAttribute, resource, null, false));
		countOfQuotas.add(modulesUtilsBl.checkAndTransferQuotas(memberResourceFileQuotasAttribute, resource, null, false));
		countOfQuotas.add(modulesUtilsBl.checkAndTransferQuotas(fileQuotasOverrideAttribute, resource, null, false));
		Map<String, String> result = modulesUtilsBl.transferQuotasBackToAttributeValue(modulesUtilsBl.countUserFacilityQuotas(countOfQuotas), false);
		Map<String, String> expectedResult = new HashMap<>();
		expectedResult.put("/var/log/test/", "3001:0");
		expectedResult.put("/etc/test/", "11000:110000");
		expectedResult.put("/usr/test/", "300:0");
		expectedResult.put("/home/test/", "0:0");
		expectedResult.put("/tmp/test/", "2:3");
		expectedResult.put("/dev/zero/", "0:0");
		assertEquals(expectedResult, result);
	}

	// private methods ------------------------------------------------------------------

	private Attribute getOverrideFileQuotasAttribute() {
		Attribute attribute = new Attribute((new urn_perun_member_resource_attribute_def_def_fileQuotasOverride()).getAttributeDefinition());
		Map<String, String> value = new LinkedHashMap<>();
		String path1 = "/var/log/test/";
		String value1 = "1:0";
		String path2 = "/dev/zero";
		String value2 = "0:0";
		value.put(path1, value1);
		value.put(path2, value2);
		attribute.setValue(value);
		return attribute;
	}

	private Attribute getOverrideDataQuotasAttribute() {
		Attribute attribute = new Attribute((new urn_perun_member_resource_attribute_def_def_dataQuotasOverride()).getAttributeDefinition());
		Map<String, String> value = new LinkedHashMap<>();
		String path1 = "/var/log/test/";
		String value1 = "1:0";
		String path2 = "/dev/zero";
		String value2 = "0:0";
		value.put(path1, value1);
		value.put(path2, value2);
		attribute.setValue(value);
		return attribute;
	}

	private Attribute getFileQuotasAttribute() {
		Attribute attribute = new Attribute((new urn_perun_resource_attribute_def_def_defaultFileQuotas()).getAttributeDefinition());
		Map<String, String> value = new LinkedHashMap<>();
		String path1 = "/var/log/test/";
		String path2 = "/etc/test/";
		String path3 = "/usr/test/";
		String path4 = "/home/test/";
		String value1 = "1000:10000";
		String value2 = "5000:50000";
		String value3 = "100:0";
		String value4 = "0:0";
		value.put(path1, value1);
		value.put(path2, value2);
		value.put(path3, value3);
		value.put(path4, value4);
		attribute.setValue(value);
		return attribute;
	}

	private Attribute getMaxFileQuotasAttribute() {
		Attribute attribute = new Attribute((new urn_perun_resource_attribute_def_def_maxUserFileQuotas()).getAttributeDefinition());
		Map<String, String> value = new LinkedHashMap<>();
		String path1 = "/var/log/test/";
		String path2 = "/etc/test/";
		String path3 = "/usr/test/";
		String path4 = "/home/test/";
		String path5 = "/tmp/test/";
		String value1 = "2000:11000";
		String value2 = "6000:60000";
		String value3 = "200:0";
		String value4 = "0:0";
		String value5 = "2:3";
		value.put(path1, value1);
		value.put(path2, value2);
		value.put(path3, value3);
		value.put(path4, value4);
		value.put(path5, value5);
		attribute.setValue(value);
		return attribute;
	}

	private Attribute getDataQuotasAttribute() {
		Attribute attribute = new Attribute((new urn_perun_resource_attribute_def_def_defaultDataQuotas()).getAttributeDefinition());
		Map<String, String> value = new LinkedHashMap<>();
		String path1 = "/var/log/test/";
		String path2 = "/etc/test/";
		String path3 = "/usr/test/";
		String path4 = "/home/test/";
		String value1 = "1G:10G";
		String value2 = "5G:50G";
		String value3 = "100K:0";
		String value4 = "0:0";
		value.put(path1, value1);
		value.put(path2, value2);
		value.put(path3, value3);
		value.put(path4, value4);
		attribute.setValue(value);
		return attribute;
	}

	private Attribute getMaxDataQuotasAttribute() {
		Attribute attribute = new Attribute((new urn_perun_resource_attribute_def_def_maxUserDataQuotas()).getAttributeDefinition());
		Map<String, String> value = new LinkedHashMap<>();
		String path1 = "/var/log/test/";
		String path2 = "/etc/test/";
		String path3 = "/usr/test/";
		String path4 = "/home/test/";
		String path5 = "/tmp/test/";
		String value1 = "2G:11G";
		String value2 = "6G:51G";
		String value3 = "101K:0";
		String value4 = "0:0";
		String value5 = "2T:3T";
		value.put(path1, value1);
		value.put(path2, value2);
		value.put(path3, value3);
		value.put(path4, value4);
		value.put(path5, value5);
		attribute.setValue(value);
		return attribute;
	}

	private Attribute getGIDRangesAttributeWithValidValue() {

		Attribute attribute = new Attribute((new urn_perun_entityless_attribute_def_def_namespace_GIDRanges()).getAttributeDefinition());

		Map<String, String> gidRanges = new LinkedHashMap<>();
		gidRanges.put("1000", "10000");
		gidRanges.put("10001", "10002");
		gidRanges.put("5", "5");
		gidRanges.put("1", "1");
		attribute.setValue(gidRanges);

		return attribute;
	}

	private Vo setUpVo() throws Exception {

		Vo vo = new Vo();
		vo.setName("AttributesMangerTestVo");
		vo.setShortName("AMTVO");
		assertNotNull("unable to create VO",perun.getVosManager().createVo(sess, vo));
		return vo;

	}

	private Member setUpMember() throws Exception {

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource userExtSource = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());

		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private Service setUpService() throws Exception {

		Service service = new Service();
		service.setName("AttributesManagerTestService");

		perun.getServicesManager().createService(sess, service);

		return service;

	}

	private Facility setUpFacility() throws Exception {

		facility = new Facility();
		facility.setName("AttributesManagerTestFacility");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, facility));
		return facility;

	}

	private Resource setUpResource() throws Exception {

		Resource resource = new Resource();
		resource.setName("AttributesManagerTestResource");
		resource.setDescription("testing resource");
		assertNotNull("unable to create resource",perun.getResourcesManager().createResource(sess, resource, vo, facility));

		return resource;

	}

	private Group setUpGroup() throws Exception {

		Group group = perun.getGroupsManager().createGroup(sess, vo, new Group("AttrTestGroup","AttrTestGroupDescription"));
		assertNotNull("unable to create a group",group);
		return group;

	}

	private List<Attribute> setUpRequiredAttributes() throws Exception {

		List<Attribute> attrList = new ArrayList<>();

		attrList.add(setUpFacilityAttribute().get(0));
		attrList.add(setUpVoAttribute().get(0));
		attrList.add(setUpFacilityUserAttribute().get(0));
		attrList.add(setUpResourceAttribute().get(0));
		attrList.add(setUpMemberAttribute().get(0));
		attrList.add(setUpMemberResourceAttribute().get(0));
		attrList.add(setUpUserAttribute().get(0));
		attrList.add(setUpHostAttribute().get(0));
		attrList.add(setUpGroupResourceAttribute().get(0));
		attrList.add(setUpGroupAttribute().get(0));

		perun.getServicesManager().addRequiredAttributes(sess, service, attrList);

		return attrList;

	}

	private List<Attribute> setUpFacilityUserAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user_facility:attribute-def:opt");
		attr.setFriendlyName("user-facility-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("UserFacilityAttribute");

		assertNotNull("unable to create user_facility attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new facility-user attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpHostAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:host:attribute-def:opt");
		attr.setFriendlyName("host-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("HostAttribute");

		assertNotNull("unable to create host attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new host attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpFacilityAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:facility:attribute-def:opt");
		attr.setFriendlyName("facility-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("FacilityAttribute");
		assertNotNull("unable to create facility attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new facility attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpEntitylessAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("entityless_test_attribute");
		attr.setType(String.class.getName());
		attr.setValue("EntitylessAttribute");
		assertNotNull("unable to create facility attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		//create new entityless attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it
		return attributes;
	}

	private List<Attribute> setUpVoAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:vo:attribute-def:opt");
		attr.setFriendlyName("vo-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("VoAttribute");
		assertNotNull("unable to create vo attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new vo attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpResourceAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:resource:attribute-def:opt");
		attr.setFriendlyName("resource-test-attribute:" + namespace);
		attr.setType(String.class.getName());
		attr.setValue("ResourceAttribute");
		assertNotNull("unable to create resource attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpFacilityGroupNameAndGIDNamespaceAttributes() throws Exception {
		Attribute facilityGroupNameNamespace = new Attribute();
		Attribute facilityGIDNamespace = new Attribute();

		List<Attribute> attributes = new ArrayList<>();

		try {
			facilityGIDNamespace = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace"));
			facilityGIDNamespace.setValue(namespace);
		} catch (AttributeNotExistsException ex) {
			facilityGIDNamespace.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
			facilityGIDNamespace.setFriendlyName("unixGID-namespace");
			facilityGIDNamespace.setType(String.class.getName());
			facilityGIDNamespace.setValue(namespace);
			assertNotNull("unable to create group attribute",perun.getAttributesManagerBl().createAttribute(sess, facilityGIDNamespace));
		}
		attributes.add(facilityGIDNamespace);

		try {
			facilityGroupNameNamespace = new Attribute(perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace"));
			facilityGroupNameNamespace.setValue(namespace);
		} catch (AttributeNotExistsException ex) {
			facilityGroupNameNamespace.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
			facilityGroupNameNamespace.setFriendlyName("unixGroupName-namespace");
			facilityGroupNameNamespace.setType(String.class.getName());
			facilityGroupNameNamespace.setValue(namespace);
			assertNotNull("unable to create group attribute",perun.getAttributesManagerBl().createAttribute(sess, facilityGroupNameNamespace));
		}
		attributes.add(facilityGroupNameNamespace);

		return attributes;
	}

	private List<Attribute> setUpGroupNamesAndGIDForGroupAndResource() throws Exception {
		Attribute groupUnixGroupName = new Attribute();
		Attribute resourceUnixGroupName = new Attribute();
		Attribute groupGID = new Attribute();
		Attribute resourceGID = new Attribute();

		List<Attribute> attributes = new ArrayList<>();

		groupUnixGroupName.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		groupUnixGroupName.setFriendlyName("unixGroupName-namespace:" + namespace);
		groupUnixGroupName.setType(String.class.getName());
		groupUnixGroupName.setValue("Group_unixGroupName-namespace_" + namespace);
		assertNotNull("unable to create group attribute",perun.getAttributesManagerBl().createAttribute(sess, groupUnixGroupName));
		attributes.add(groupUnixGroupName);

		resourceUnixGroupName.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		resourceUnixGroupName.setFriendlyName("unixGroupName-namespace:" + namespace);
		resourceUnixGroupName.setType(String.class.getName());
		resourceUnixGroupName.setValue("Resource_unixGroupName-namespace_" + namespace);
		assertNotNull("unable to create group attribute",perun.getAttributesManagerBl().createAttribute(sess, resourceUnixGroupName));
		attributes.add(resourceUnixGroupName);

		groupGID.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		groupGID.setFriendlyName("unixGID-namespace:" + namespace);
		groupGID.setType(Integer.class.getName());
		groupGID.setValue(111);
		assertNotNull("unable to create group attribute",perun.getAttributesManagerBl().createAttribute(sess, groupGID));
		attributes.add(groupGID);

		resourceGID.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		resourceGID.setFriendlyName("unixGID-namespace:" + namespace);
		resourceGID.setType(Integer.class.getName());
		resourceGID.setValue(112);
		assertNotNull("unable to create group attribute",perun.getAttributesManagerBl().createAttribute(sess, resourceGID));
		attributes.add(resourceGID);

		return attributes;
	}

	private List<Attribute> setUpResourceUnixGIDNamespaceAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("unixGID-namespace:" + namespace);
		attr.setType(String.class.getName());
		attr.setValue("111");
		assertNotNull("unable to create resource attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);

		return attributes;
	}

	private List<Attribute> setUpMemberResourceAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member_resource:attribute-def:opt");
		attr.setFriendlyName("member-resource-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberResourceAttribute");
		assertNotNull("unable to create member-resource attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpUserAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("UserAttribute");
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpUserLargeAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-large-test-attribute");
		attr.setType(LinkedHashMap.class.getName());
		Map<String, String> value = new LinkedHashMap<>();
		value.put("UserLargeAttribute", "test value");
		attr.setValue(value);
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpResourceLargeAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:resource:attribute-def:opt");
		attr.setFriendlyName("resource_large_test_attribute");
		attr.setType(LinkedHashMap.class.getName());
		Map<String, String> value = new LinkedHashMap<>();
		value.put("ResourceLargeAttribute", "test value");
		value.put("ResourceTestLargeAttr", "test value 2");
		attr.setValue(value);
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it
		return attributes;

	}

	private List<Attribute> setUpMemberAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:member:attribute-def:opt");
		attr.setFriendlyName("member-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("MemberAttribute");

		assertNotNull("unable to create member attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}


	private List<Attribute> setUpGroupAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:group:attribute-def:opt");
		attr.setFriendlyName("group-test-attribute:" + namespace);
		attr.setType(String.class.getName());
		attr.setValue("GroupAttribute");

		assertNotNull("unable to create group attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new group attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	private List<Attribute> setUpGroupAttributes() throws Exception {

		Attribute attr = new Attribute();
		String namespace = "group-test-uniqueattribute:specialNamespace";
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_OPT);
		attr.setFriendlyName(namespace + "1");
		attr.setType(String.class.getName());
		attr.setValue("GroupAttribute");

		List<Attribute> attributes = new ArrayList<>();
		assertNotNull("unable to create group attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));
		attributes.add(attr);

		Attribute attr2 = new Attribute(attr);
		attr2.setFriendlyName(namespace + "2");
		assertNotNull("unable to create group attribute", perun.getAttributesManagerBl().createAttribute(sess, attr2));
		attributes.add(attr2);

		Attribute attr3 = new Attribute(attr);
		attr3.setFriendlyName(namespace + "3");
		assertNotNull("unable to create group attribute", perun.getAttributesManagerBl().createAttribute(sess, attr3));
		attributes.add(attr3);

		//And one attribute with other name
		Attribute attr4 = new Attribute(attr);
		attr4.setFriendlyName("group-test-uniqueEattribute:specialNamespace");
		assertNotNull("unable to create group attribute", perun.getAttributesManagerBl().createAttribute(sess, attr4));

		return attributes;
	}

	private List<Attribute> setUpResourceAttributes() throws Exception {

		Attribute attr = new Attribute();
		String namespace = "resource-test-uniqueattribute:specialNamespace";
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_OPT);
		attr.setFriendlyName(namespace + "1");
		attr.setType(String.class.getName());
		attr.setValue("ResourceAttribute");

		List<Attribute> attributes = new ArrayList<>();
		assertNotNull("unable to create resource attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));
		attributes.add(attr);

		Attribute attr2 = new Attribute(attr);
		attr2.setFriendlyName(namespace + "2");
		assertNotNull("unable to create resource attribute", perun.getAttributesManagerBl().createAttribute(sess, attr2));
		attributes.add(attr2);

		Attribute attr3 = new Attribute(attr);
		attr3.setFriendlyName(namespace + "3");
		assertNotNull("unable to create resource attribute", perun.getAttributesManagerBl().createAttribute(sess, attr3));
		attributes.add(attr3);

		//And one attribute with other name
		Attribute attr4 = new Attribute(attr);
		attr4.setFriendlyName("resource-test-uniqueEattribute:specialNamespace");
		assertNotNull("unable to create resource attribute", perun.getAttributesManagerBl().createAttribute(sess, attr4));

		return attributes;
	}

	private List<Attribute> setUpGroupResourceAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:group_resource:attribute-def:opt");
		attr.setFriendlyName("group-resource-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("GroupResourceAttribute");

		assertNotNull("unable to create Group_Resource attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new group resource attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;

	}

	public Attribute setAttributeInNamespace(String namespace) throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(namespace);
		attrDef.setDescription("Test attribute description");
		attrDef.setFriendlyName("testingAttribute");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Testing value");
		return attribute;
	}

}
