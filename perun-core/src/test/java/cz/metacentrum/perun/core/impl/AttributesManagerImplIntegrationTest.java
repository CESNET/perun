package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AttributesManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = "AttributesManagerImplIntegrationTest.";
	private final Map<Attribute, String> attrMap = new HashMap<>();
	private AttributesManagerImplApi attributesManager;
	private PerunBlImpl perunBlImpl;

	private Service service1;
	private Service service2;
	private Group group;
	private Resource resource;

	@Before
	public void setUp() throws Exception {
		perunBlImpl = ((PerunBlImpl) perun);
		attributesManager = perunBlImpl.getAttributesManagerImpl();

		initializeServiceRequiredAttributes();

		attrMap.put(getArrayAttribute("a", "b"), "a,b,");
		attrMap.put(getArrayAttribute("a,b", "c"), "a\\,b,c,");
		attrMap.put(getArrayAttribute("a,,b", "c"), "a\\,\\,b,c,");
		attrMap.put(getArrayAttribute("a\\b"), "a\\\\b,");

		attrMap.put(getHashAttribute("a", "b", "c", "d"), "a:b,c:d,");
		attrMap.put(getHashAttribute("a,x", "b,,", ",,c", ",,d,,"), "a\\,x:b\\,\\,,\\,\\,c:\\,\\,d\\,\\,,");
		attrMap.put(getHashAttribute("a:x", "b"), "a\\:x:b,");
		attrMap.put(getHashAttribute("a:x", "b", ":", "::"), "a\\:x:b,\\::\\:\\:,");
	}


	@Test
	public void stringToAttributeValue() throws Exception {
		System.out.println(CLASS_NAME + "stringToAttributeValue");
		System.out.println("AttributesManagerImpl.stringToAttributeValue");
		for(Map.Entry<Attribute, String> entry : attrMap.entrySet()) {
			Object value = BeansUtils.stringToAttributeValue(entry.getValue(), entry.getKey().getType());
			assertEquals("Input: " + entry.getValue(), entry.getKey().getValue(), value);
		}
	}

	@Test
	public void attributeValueToString() throws Exception {
		System.out.println(CLASS_NAME + "attributeValueToString");
		for(Map.Entry<Attribute, String> entry : attrMap.entrySet()) {
			String DBValue = BeansUtils.attributeValueToString(entry.getKey());
			assertEquals(entry.getValue(), DBValue);
		}
	}

	@Test
	public void getRequiredAttributes_Services_Group_ForMoreServices() {
		System.out.println(CLASS_NAME + "getRequiredAttributes_Services_Group_ForMoreServices");
		List<Attribute> attributes =
			attributesManager.getRequiredAttributes(sess, Arrays.asList(service1, service2), group);
		assertThat(attributes).hasSize(2);
	}

	@Test
	public void getRequiredAttributes_Services_Group_ForOneService() {
		System.out.println(CLASS_NAME + "getRequiredAttributes_Services_Group_ForOneService");
		List<Attribute> attributes =
			attributesManager.getRequiredAttributes(sess, Collections.singletonList(service1), group);
		assertThat(attributes).hasSize(1);
	}

	@Test
	public void getRequiredAttributes_Service_Group() {
		System.out.println(CLASS_NAME + "getRequiredAttributes_Service_Group");
		List<Attribute> attributes =
			attributesManager.getRequiredAttributes(sess, service1, group);
		assertThat(attributes).hasSize(1);
	}

	@Test
	public void getRequiredAttributes_service_resource_group() {
		System.out.println(CLASS_NAME + "getRequiredAttributes_service_resource_group");
		List<Attribute> attributes =
			attributesManager.getRequiredAttributes(sess, service1, resource, group);
		assertThat(attributes).hasSize(1);
	}

	@Test
	public void getRequiredAttributes_services_resource_group_ForMoreServices() {
		System.out.println(CLASS_NAME + "getRequiredAttributes_services_resource_group_ForMoreServices");
		List<Attribute> attributes =
			attributesManager.getRequiredAttributes(sess, Arrays.asList(service1, service2), resource, group);
		assertThat(attributes).hasSize(2);
	}

	@Test
	public void getRequiredAttributes_services_resource_group_ForOneService() {
		System.out.println(CLASS_NAME + "getRequiredAttributes_services_resource_group_ForOneService");
		List<Attribute> attributes =
			attributesManager.getRequiredAttributes(sess, Collections.singletonList(service1), resource, group);
		assertThat(attributes).hasSize(1);
	}

	@Test
	public void getUninitializedAttributesModule() throws Exception {
		System.out.println(CLASS_NAME + "getUninitializedAttributesModule");

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("IPAddresses");
		attr.setType(ArrayList.class.getName());

		assertNotNull(attributesManager.getUninitializedAttributesModule(sess, attr));

		perun.getAttributesManagerBl().createAttribute(sess, attr);

		assertNull(attributesManager.getUninitializedAttributesModule(sess, attr));
	}

	@Test
	public void getUninitializedAttributesModuleForAttributeWithoutModule() {
		System.out.println(CLASS_NAME + "getUninitializedAttributesModuleForAttributeWithoutModule");

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("SomeNonexistentAttribute");
		attr.setType(ArrayList.class.getName());

		assertNull(attributesManager.getUninitializedAttributesModule(sess, attr));
	}

	@Test
	public void getUninitializedAttributesModuleFromDeletedAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getUninitializedAttributesModuleFromDeletedAttribute");

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("IPAddresses");
		attr.setType(ArrayList.class.getName());

		perun.getAttributesManagerBl().createAttribute(sess, attr);
		assertNull(attributesManager.getUninitializedAttributesModule(sess, attr));

		perun.getAttributesManagerBl().deleteAttribute(sess, attr);
		assertNotNull(attributesManager.getUninitializedAttributesModule(sess, attr));
	}


	/* ################## Private methods ################ */


	private void initializeServiceRequiredAttributes() throws Exception {
		Vo vo = new Vo(-1, "Test vo", "TestVo");
		vo = perunBlImpl.getVosManagerBl().createVo(sess, vo);

		Facility facility = new Facility(-1, "Test Facility");
		facility = perunBlImpl.getFacilitiesManagerBl().createFacility(sess, facility);

		resource = new Resource(-1, "Test", "Test", facility.getId());
		resource = perunBlImpl.getResourcesManagerBl().createResource(sess, resource, vo, facility);

		group = new Group("TestGroup", "Test Group");
		group = perunBlImpl.getGroupsManagerBl().createGroup(sess, vo, group);

		service1 = new Service(-1, "TestService1");
		service1 = perunBlImpl.getServicesManagerBl().createService(sess, service1);

		service2 = new Service(-1, "TestService2");
		service2 = perunBlImpl.getServicesManagerBl().createService(sess, service2);

		AttributeDefinition groupAttribute1 = getGroupAttributeDefinition("Service1attribute");
		groupAttribute1 = perunBlImpl.getAttributesManagerBl().createAttribute(sess, groupAttribute1);

		AttributeDefinition groupAttribute2 = getGroupAttributeDefinition("Service2attribute");
		groupAttribute2 = perunBlImpl.getAttributesManagerBl().createAttribute(sess, groupAttribute2);

		AttributeDefinition groupResourceAttribute1 = getGroupResourceAttributeDefinition("Service1attribute");
		groupResourceAttribute1 = perunBlImpl.getAttributesManagerBl().createAttribute(sess, groupResourceAttribute1);

		AttributeDefinition groupResourceAttribute2 = getGroupResourceAttributeDefinition("Service2attribute");
		groupResourceAttribute2 = perunBlImpl.getAttributesManagerBl().createAttribute(sess, groupResourceAttribute2);

		perunBlImpl.getServicesManagerBl().addRequiredAttribute(sess, service1, groupAttribute1);
		perunBlImpl.getServicesManagerBl().addRequiredAttribute(sess, service1, groupResourceAttribute1);
		perunBlImpl.getServicesManagerBl().addRequiredAttribute(sess, service2, groupResourceAttribute2);
		perunBlImpl.getServicesManagerBl().addRequiredAttribute(sess, service2, groupAttribute2);

		perunBlImpl.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false);
		perunBlImpl.getResourcesManagerBl().assignService(sess, resource, service1);
		perunBlImpl.getResourcesManagerBl().assignService(sess, resource, service2);
	}

	private Attribute getArrayAttribute(String ... value) {
		Attribute attribute = new Attribute();
		attribute.setFriendlyName("test");
		attribute.setNamespace("test");
		attribute.setType(ArrayList.class.getName());
		attribute.setValue(new ArrayList<>(Arrays.asList(value)));
		return attribute;
	}

	private AttributeDefinition getGroupAttributeDefinition(String name) {
		return getAttributeDefinition(name, AttributesManager.NS_GROUP_ATTR_DEF);
	}

	private AttributeDefinition getGroupResourceAttributeDefinition(String name) {
		return getAttributeDefinition(name, AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
	}

	private AttributeDefinition getAttributeDefinition(String name, String nameSpace) {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(nameSpace);
		attr.setFriendlyName(name);
		attr.setDisplayName(name);
		attr.setType(String.class.getName());
		attr.setDescription(name);
		return attr;
	}

	private Attribute getHashAttribute(String ... value) {
		if(value.length % 2 == 1) throw new IllegalArgumentException("value");
		Attribute attribute = new Attribute();
		attribute.setFriendlyName("test");
		attribute.setNamespace("test");
		attribute.setType(LinkedHashMap.class.getName());

		Map<String, String> attrValue = new LinkedHashMap<>();
		for(int i = 0; i < value.length; i += 2) {
			attrValue.put(value[i], value[i+1]);
		}
		attribute.setValue(attrValue);
		return attribute;
	}
}
