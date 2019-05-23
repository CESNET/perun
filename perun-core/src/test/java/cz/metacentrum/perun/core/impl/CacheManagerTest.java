package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Holder;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Cache manager tests.
 *
 * @author Simona Kruppova
 */
public class CacheManagerTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "CacheManager.";

	private static CacheManager cacheManager;

	private static int id = 0;

	private Holder groupHolder;
	private Holder groupHolder1;
	private Holder resourceHolder;
	private Holder resourceHolder1;
	private Holder userHolder;
	private Holder userHolder1;
	private Holder facilityHolder;
	private Holder memberHolder;

	private final String subject = "Test subject";
	private final String subject1 = "Test subject1";

	private final String timeCreated = "2016-04-24";
	private final String timeModified = "2016-04-25";
	private final String creator = "Admin";
	private final String modifier = "Test";

	@Before
	public void setUp() {
		cacheManager = perun.getCacheManager();
		CacheManager.setCacheDisabled(false);

		//CacheManagerTest counts with empty cache
		cacheManager.clearCache();

		this.setUpHolders();
	}

	@AfterClass
	public static void initializeCache() throws Exception {
		//Initialize cache again after this test class ended
		cacheManager.initialize(sess, ((PerunBlImpl)sess.getPerun()).getAttributesManagerImpl());
	}

	private void setUpHolders() {
		this.groupHolder = new Holder(0, Holder.HolderType.GROUP);
		this.groupHolder1 = new Holder(1, Holder.HolderType.GROUP);
		this.resourceHolder = new Holder(0, Holder.HolderType.RESOURCE);
		this.resourceHolder1 = new Holder(1, Holder.HolderType.RESOURCE);
		this.userHolder = new Holder(0, Holder.HolderType.USER);
		this.userHolder1 = new Holder(1, Holder.HolderType.USER);
		this.facilityHolder = new Holder(0, Holder.HolderType.FACILITY);
		this.memberHolder = new Holder(0, Holder.HolderType.MEMBER);
	}




	// GET METHODS TESTS ----------------------------------------------


	@Test
	public void getAllNonEmptyAttributesByPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyAttributesByPrimaryHolder");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		setUpGroupAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		setUpVirtualGroupAttribute();

		List<Attribute> attrs = cacheManager.getAllNonEmptyAttributes(groupHolder);

		assertTrue("result should contain group attribute", attrs.contains(groupAttr));
		assertTrue("result should not contain group-resource attribute", !attrs.contains(groupResourceAttr));
		assertTrue("it should return only 1 attribute", attrs.size() == 1);
	}

	@Test
	public void getAllNonEmptyAttributesByPrimaryHolderEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyAttributesByPrimaryHolderEmpty");

		List<Attribute> attrs = cacheManager.getAllNonEmptyAttributes(groupHolder);

		assertTrue("there should be no returned attributes", attrs.isEmpty());
	}

	@Test
	public void getAllNonEmptyAttributesByHolders() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyAttributesByHolders");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		setUpGroupAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		setUpVirtualGroupResourceAttribute();

		List<Attribute> attrs = cacheManager.getAllNonEmptyAttributes(groupHolder, resourceHolder);

		assertTrue("result should contain group-resource attribute", attrs.contains(groupResourceAttr));
		assertTrue("result should not contain group attribute", !attrs.contains(groupAttr));
		assertTrue("it should return only 1 attribute", attrs.size() == 1);
	}

	@Test
	public void getAllNonEmptyAttributesByHoldersEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyAttributesByHoldersEmpty");

		List<Attribute> attrs = cacheManager.getAllNonEmptyAttributes(groupHolder, resourceHolder);

		assertTrue("there should be no returned attributes", attrs.isEmpty());
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getAllNonEmptyAttributesByStartPartOfName() throws Exception {
		System.out.println(CLASS_NAME + "getAllAttributesByStartPartOfName");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		setUpVirtualGroupAttribute();

		List<Attribute> attrs = cacheManager.getAllAttributesByStartPartOfName("urn:perun:group:attribute-def:opt", groupHolder);

		assertTrue("result should contain group attribute", attrs.contains(groupAttr));
		assertTrue("result should contain attribute definition", attrs.contains(attrDef));
		assertTrue("result should not contain group-resource attribute", !attrs.contains(groupResourceAttr));
		assertTrue("it should return 2 attributes", attrs.size() == 2);
	}

	@Test
	public void getAllNonEmptyAttributesByStartPartOfNameEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyAttributesByStartPartOfNameEmpty");

		List<Attribute> attrs = cacheManager.getAllAttributesByStartPartOfName("urn:perun:group:attribute-def:opt", groupHolder);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@Test
	public void getUserFacilityAttributesForAnyUser() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesForAnyUser");

		Attribute userFacilityAttr = setUpUserFacilityAttribute();
		Attribute userAttr = setUpUserAttribute();
		cacheManager.setAttribute(userFacilityAttr, userHolder, facilityHolder);
		cacheManager.setAttribute(userFacilityAttr, userHolder1, facilityHolder);
		cacheManager.setAttribute(userAttr, userHolder, null);
		setUpGroupAttributeDefinition();
		setUpVirtualUserFacilityAttribute();

		List<Attribute> attrs = cacheManager.getUserFacilityAttributesForAnyUser(facilityHolder.getId());

		assertTrue("result should contain user-facility attribute", attrs.contains(userFacilityAttr));
		assertTrue("it should return 2 attributes", attrs.size() == 2);
	}

	@Test
	public void getUserFacilityAttributesForAnyUserEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getUserFacilityAttributesForAnyUserEmpty");

		List<Attribute> attrs = cacheManager.getUserFacilityAttributesForAnyUser(0);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getAttributesByNamesAndPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByNamesAndPrimaryHolder");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		AttributeDefinition groupAttrDef = setUpGroupAttributeDefinition();
		AttributeDefinition resourceAttrDef = setUpResourceAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);

		List<String> attributeNames = new ArrayList<>();
		attributeNames.add(groupAttr.getName());
		attributeNames.add(groupAttrDef.getName());
		attributeNames.add("attrName");

		List<Attribute> attrs = cacheManager.getAttributesByNames(attributeNames, groupHolder, null);

		assertTrue("result should contain group attribute", attrs.contains(groupAttr));
		assertTrue("result should contain group attribute definition", attrs.contains(groupAttrDef));
		assertTrue("it should return 2 attributes", attrs.size() == 2);
	}

	@Test
	public void getAttributesByNamesAndPrimaryHolderEmpty() {
		System.out.println(CLASS_NAME + "getAttributesByNamesAndPrimaryHolderEmpty");

		List<String> attributeNames = new ArrayList<>();
		List<Attribute> attrs = cacheManager.getAttributesByNames(attributeNames, groupHolder, null);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getAttributesByNamesAndHolders() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByNamesAndHolders");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute memberGroupAttr = setUpMemberGroupAttribute();
		AttributeDefinition memberGroupAttrDef = setUpMemberGroupAttributeDefinition();
		AttributeDefinition groupResourceAttrDef = setUpGroupResourceAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(memberGroupAttr, memberHolder, groupHolder);

		List<String> attributeNames = new ArrayList<>();
		attributeNames.add(memberGroupAttr.getName());
		attributeNames.add(memberGroupAttrDef.getName());
		attributeNames.add("attrName");

		List<Attribute> attrs = cacheManager.getAttributesByNames(attributeNames, memberHolder, groupHolder);

		assertTrue("result should contain member-group attribute", attrs.contains(memberGroupAttr));
		assertTrue("result should contain member-group attribute definition", attrs.contains(memberGroupAttrDef));
		assertTrue("it should return 2 attributes", attrs.size() == 2);
	}

	@Test
	public void getAttributesByNamesAndHoldersEmpty() {
		System.out.println(CLASS_NAME + "getAttributesByNamesAndHoldersEmpty");

		List<String> attributeNames = new ArrayList<>();
		List<Attribute> attrs = cacheManager.getAttributesByNames(attributeNames, groupHolder, resourceHolder);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@Test
	public void getAttributesByAttributeDefinitionWithPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByAttributeDefinitionWithPrimaryHolder");

		AttributeDefinition attrDef = setUpUserAttributeDefinition();
		Attribute userAttr = new Attribute(attrDef);
		Attribute userAttr1 = new Attribute(attrDef);
		userAttr.setValue("value");
		userAttr1.setValue("value1");

		Attribute useFacAttr = setUpUserFacilityAttribute();
		setUpVirtualGroupAttribute();

		cacheManager.setAttribute(userAttr, userHolder, null);
		cacheManager.setAttribute(userAttr1, userHolder1, null);
		cacheManager.setAttribute(useFacAttr, userHolder, resourceHolder);

		List<Attribute> attrs = cacheManager.getAttributesByAttributeDefinition(attrDef);

		assertTrue("result should contain user attribute", attrs.contains(userAttr));
		assertTrue("result should contain user attribute", attrs.contains(userAttr1));
		assertTrue("it should return 2 attributes", attrs.size() == 2);
	}

	@Test
	public void getAttributesByAttributeDefinitionWithHolders() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByAttributeDefinitionWithHolders");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		Attribute entitylessAttr = setUpEntitylessAttribute();
		setUpVirtualGroupResourceAttribute();

		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		cacheManager.setEntitylessAttribute(entitylessAttr, subject);

		List<Attribute> attrs = cacheManager.getAttributesByAttributeDefinition(groupResourceAttr);

		assertTrue("it should return only 1 attribute", attrs.size() == 1);
		assertTrue("result should contain group-resource attribute", attrs.contains(groupResourceAttr));
	}

	@Test
	public void getAttributesByAttributeDefinitionEntityless() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByAttributeDefinitionEntityless");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		Attribute entitylessAttr = setUpEntitylessAttribute();

		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		cacheManager.setEntitylessAttribute(entitylessAttr, subject);

		List<Attribute> attrs = cacheManager.getAttributesByAttributeDefinition(entitylessAttr);

		assertTrue("it should return only 1 attribute", attrs.size() == 1);
		assertTrue("result should contain entityless attribute", attrs.contains(entitylessAttr));
	}

	@Test
	public void getAttributesByAttributeDefinitionEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByAttributeDefinitionEmpty");

		AttributeDefinition attributeDefinition = setUpGroupAttributeDefinition();

		assertTrue("there should be no returned attributes", cacheManager.getAttributesByAttributeDefinition(attributeDefinition).size() == 0);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getVirtualAttributesByPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "getVirtualAttributesByPrimaryHolder");

		AttributeDefinition memberVirtAttr = setUpVirtualMemberAttribute();
		AttributeDefinition memberResourceVirtAttr = setUpVirtualMemberResourceAttribute();
		setUpGroupAttribute();
		setUpMemberGroupAttributeDefinition();

		List<Attribute> attrs = cacheManager.getVirtualAttributes(Holder.HolderType.MEMBER, null);

		assertTrue("result should contain virtual member attribute definition", attrs.contains(memberVirtAttr));
		assertTrue("result should not contain virtual member-resource attribute definition", !attrs.contains(memberResourceVirtAttr));
		assertTrue("it should return only 1 attribute", attrs.size() == 1);
	}

	@Test
	public void getVirtualAttributesByPrimaryHolderEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getVirtualAttributesByPrimaryHolderEmpty");

		List<Attribute> attrs = cacheManager.getVirtualAttributes(Holder.HolderType.MEMBER, null);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getVirtualAttributesByHolders() throws Exception {
		System.out.println(CLASS_NAME + "getVirtualAttributesByHolders");

		AttributeDefinition memberVirtAttr = setUpVirtualMemberAttribute();
		AttributeDefinition memberResourceVirtAttr = setUpVirtualMemberResourceAttribute();
		setUpGroupAttribute();
		setUpMemberGroupAttributeDefinition();

		List<Attribute> attrs = cacheManager.getVirtualAttributes(Holder.HolderType.MEMBER, Holder.HolderType.RESOURCE);

		assertTrue("result should contain virtual member-resource attribute definition", attrs.contains(memberResourceVirtAttr));
		assertTrue("result should not contain virtual member attribute definition", !attrs.contains(memberVirtAttr));
		assertTrue("it should return only 1 attribute", attrs.size() == 1);
	}

	@Test
	public void getVirtualAttributesByHoldersEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getVirtualAttributesByHoldersEmpty");

		List<Attribute> attrs = cacheManager.getVirtualAttributes(Holder.HolderType.MEMBER, Holder.HolderType.RESOURCE);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@Test
	public void getAttributeByNameAndPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByNameAndPrimaryHolder");

		Attribute attr = setUpGroupAttribute();
		cacheManager.setAttribute(attr, groupHolder, null);

		Attribute returnedAttr = cacheManager.getAttributeByName(attr.getName(), groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test
	public void getAttributeByNameAndPrimaryHolderWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByNameAndPrimaryHolderWhenOnlyDefinitionExists");

		AttributeDefinition attr = setUpGroupAttributeDefinition();

		Attribute returnedAttr = cacheManager.getAttributeByName(attr.getName(), groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test(expected = AttributeNotExistsException.class)
	public void getAttributeByNameAndPrimaryHolderNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByNameAndPrimaryHolderNotExists");

		cacheManager.getAttributeByName("urn:perun:group:attribute-def:opt:group-test-attribute", groupHolder, null);
	}

	@Test
	public void getAttributeByNameAndHolders() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByNameAndHolders");

		Attribute attr = setUpGroupResourceAttribute();
		cacheManager.setAttribute(attr, groupHolder, resourceHolder);

		Attribute returnedAttr = cacheManager.getAttributeByName(attr.getName(), groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test
	public void getAttributeByNameAndHoldersWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByNameAndHoldersWhenOnlyDefinitionExists");

		AttributeDefinition attr = setUpGroupResourceAttributeDefinition();

		Attribute returnedAttr = cacheManager.getAttributeByName(attr.getName(), groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test(expected = AttributeNotExistsException.class)
	public void getAttributeByNameAndHoldersNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByNameAndHoldersNotExists");

		cacheManager.getAttributeByName("urn:perun:group_resource:attribute-def:opt:group-resource-test-attribute", groupHolder, resourceHolder);
	}

	@Test
	public void getAttributeByIdAndPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByIdAndPrimaryHolder");

		Attribute attr = setUpGroupAttribute();
		cacheManager.setAttribute(attr, groupHolder, null);

		Attribute returnedAttr = cacheManager.getAttributeById(attr.getId(), groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test
	public void getAttributeByIdAndPrimaryHolderWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByIdAndPrimaryHolderWhenOnlyDefinitionExists");

		AttributeDefinition attr = setUpGroupAttributeDefinition();

		Attribute returnedAttr = cacheManager.getAttributeById(attr.getId(), groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test(expected = AttributeNotExistsException.class)
	public void getAttributeByIdAndPrimaryHolderNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByIdAndPrimaryHolderNotExists");

		cacheManager.getAttributeById(1, groupHolder, null);
	}

	@Test
	public void getAttributeByIdAndHolders() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByIdAndHolders");

		Attribute attr = setUpGroupResourceAttribute();
		cacheManager.setAttribute(attr, groupHolder, resourceHolder);

		Attribute returnedAttr = cacheManager.getAttributeById(attr.getId(), groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test
	public void getAttributeByIdAndHoldersWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByIdAndHoldersWhenOnlyDefinitionExists");

		AttributeDefinition attr = setUpGroupResourceAttributeDefinition();

		Attribute returnedAttr = cacheManager.getAttributeById(attr.getId(), groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test(expected = AttributeNotExistsException.class)
	public void getAttributeByIdAndHoldersNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByIdAndHoldersNotExists");

		cacheManager.getAttributeById(1, groupHolder, resourceHolder);
	}

	@Test
	public void getAllSimilarAttributeNames() {
		System.out.println(CLASS_NAME + "getAllSimilarAttributeNamesEmpty");

		List<AttributeDefinition> attrs = setUpAttributesDefinitions();

		List<String> attrNames = cacheManager.getAllSimilarAttributeNames(attrs.get(0).getNamespace());

		assertTrue("it should return 2 attribute names", attrNames.size() == 2);
		assertTrue(attrNames.contains(attrs.get(0).getName()));
	}

	@Test
	public void getAllSimilarAttributeNamesEmpty() {
		System.out.println(CLASS_NAME + "getAllSimilarAttributeNamesEmpty");

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();

		cacheManager.getAllSimilarAttributeNames(attrDef.getNamespace() + "ABC");
	}

	@Test
	public void getAttributesDefinitions() {
		System.out.println(CLASS_NAME + "getAttributesDefinitions");

		List<AttributeDefinition> attributeDefinitions = setUpAttributesDefinitions();
		List<AttributeDefinition> returnedAttrDefinitions = cacheManager.getAttributesDefinitions();
		assertTrue("Returned attribute definitions don't contain all set attribute definitions.", returnedAttrDefinitions.containsAll(attributeDefinitions));
		assertEquals("Returned list doesn't have same size as expected.", attributeDefinitions.size(), returnedAttrDefinitions.size());
	}

	@Test
	public void getAttributesDefinitionsEmpty() {
		System.out.println(CLASS_NAME + "getAttributesDefinitionsEmpty");

		List<AttributeDefinition> returnedAttrDefinitions = cacheManager.getAttributesDefinitions();

		assertTrue("there should be no returned attributes", returnedAttrDefinitions.isEmpty());
	}

	@Test
	public void getAttributesDefinitionsByNamespace() {
		System.out.println(CLASS_NAME + "getAttributesDefinitionsByNamespace");

		List<AttributeDefinition> attributeDefinitions = setUpAttributesDefinitions();
		String namespace = attributeDefinitions.get(0).getNamespace();

		List<AttributeDefinition> returnedAttrDefinitions = cacheManager.getAttributesDefinitionsByNamespace(namespace);

		assertTrue("it should return 2 attributes", returnedAttrDefinitions.size() == 2);
		assertEquals("namespace should be same", namespace, returnedAttrDefinitions.get(0).getNamespace());
	}

	@Test
	public void getAttributesDefinitionsByNamespaceNotExists() {
		System.out.println(CLASS_NAME + "getAttributesDefinitionsByNamespaceNotExists");

		setUpAttributesDefinitions();
		List<AttributeDefinition> returnedAttrDefinitions = cacheManager.getAttributesDefinitionsByNamespace("urn:perun:groupB:attribute-def:opt");

		assertTrue("it should not return any attribute", returnedAttrDefinitions.size() == 0);
	}

	@Test
	public void getAttributesDefinitionsByIds() {
		System.out.println(CLASS_NAME + "getAttributesDefinitionsByIds");

		List<AttributeDefinition> attributeDefinitions = setUpAttributesDefinitions();

		List<Integer> attributeDefinitionIds = new ArrayList<>();
		for(AttributeDefinition attrDef: attributeDefinitions) {
			attributeDefinitionIds.add(attrDef.getId());
		}

		List<AttributeDefinition> returnedAttrDefinitions = cacheManager.getAttributesDefinitions(attributeDefinitionIds);

		assertEquals("number of attribute definitions set and returned should be the same", attributeDefinitionIds.size(), returnedAttrDefinitions.size());

		for(AttributeDefinition attrDef: attributeDefinitions) {
			assertTrue("returned list should contain saved attribute definition", returnedAttrDefinitions.contains(attrDef));
		}
	}

	@Test
	public void getAttributesDefinitionsByIdsEmpty() {
		System.out.println(CLASS_NAME + "getAttributesDefinitionsByIdsEmpty");

		setUpAttributesDefinitions();
		List<Integer> attributeDefinitionIds = new ArrayList<>();

		List<AttributeDefinition> returnedAttrDefinitions = cacheManager.getAttributesDefinitions(attributeDefinitionIds);

		assertTrue("it should not return any attribute definition", returnedAttrDefinitions.size() == 0);
	}

	@Test
	public void getAttributeDefinitionByName() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinitionByName");

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		AttributeDefinition returnedAttr = cacheManager.getAttributeDefinition(attrDef.getName());

		assertEquals("returned attribute is not same as stored", attrDef, returnedAttr);
	}


	@Test(expected = AttributeNotExistsException.class)
	public void getAttributeDefinitionByNameNotExists() throws Exception{
		System.out.println(CLASS_NAME + "getAttributeDefinitionByNameNotExists");

		cacheManager.getAttributeDefinition("urn:perun:group:attribute-def:opt:group-test-attribute");
	}

	@Test
	public void getAttributeDefinitionById() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinition");

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		AttributeDefinition returnedAttr = cacheManager.getAttributeDefinition(attrDef.getId());

		assertEquals("returned attribute is not same as stored", attrDef, returnedAttr);
	}

	@Test(expected = AttributeNotExistsException.class)
	public void getAttributeDefinitionByIdNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinitionByIdNotExists");

		cacheManager.getAttributeDefinition(0);
	}

	@Test
	public void getAllNonEmptyEntitylessAttributesByKey() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyEntitylessAttributesByKey");

		Attribute entitylessAttr = setUpEntitylessAttribute();
		setUpEntitylessAttributeDefinition();
		setUpGroupAttribute();
		setUpVirtualGroupAttribute();
		cacheManager.setEntitylessAttribute(entitylessAttr, subject);

		List<Attribute> attrs = cacheManager.getAllNonEmptyEntitylessAttributes(subject);

		assertTrue("result should contain entityless attribute", attrs.contains(entitylessAttr));
		assertTrue("it should return only 1 attribute", attrs.size() == 1);
	}

	@Test
	public void getAllNonEmptyEntitylessAttributesByKeyEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyEntitylessAttributesByKeyEmpty");

		List<Attribute> attrs = cacheManager.getAllNonEmptyEntitylessAttributes(subject);

		assertTrue("there should be no returned attributes", attrs.isEmpty());
	}

	@Test
	public void getAllNonEmptyEntitylessAttributesByName() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyEntitylessAttributesByName");

		Attribute entitylessAttr = setUpEntitylessAttribute();
		setUpEntitylessAttributeDefinition();
		setUpGroupAttribute();
		setUpVirtualGroupAttribute();
		cacheManager.setEntitylessAttribute(entitylessAttr, subject);

		List<Attribute> attrs = cacheManager.getAllNonEmptyEntitylessAttributesByName(entitylessAttr.getName());

		assertTrue("result should contain entityless attribute", attrs.contains(entitylessAttr));
		assertTrue("it should return only 1 attribute", attrs.size() == 1);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getAllNonEmptyEntitylessAttributesByNameWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyEntitylessAttributesByNameWhenOnlyDefinitionExists");

		Attribute entitylessAttr = setUpEntitylessAttribute();
		AttributeDefinition entitylessAttrDef = setUpEntitylessAttributeDefinition();
		cacheManager.setEntitylessAttribute(entitylessAttr, subject);

		List<Attribute> attrs = cacheManager.getAllNonEmptyEntitylessAttributesByName(entitylessAttrDef.getName());

		assertTrue("result should not contain entityless attribute definition", !attrs.contains(entitylessAttrDef));
		assertTrue("there should be no returned attributes", attrs.isEmpty());
	}

	@Test
	public void getAllNonEmptyEntitylessAttributesByNameEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAllNonEmptyEntitylessAttributesByNameEmpty");

		List<Attribute> attrs = cacheManager.getAllNonEmptyEntitylessAttributesByName("attr-name");

		assertTrue("there should be no returned attributes", attrs.isEmpty());
	}

	@Test
	public void getEntitylessAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttribute");

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		Attribute returnedAttr = cacheManager.getEntitylessAttribute(attr.getName(), subject);

		assertEquals("returned attribute is not same as stored", attr, returnedAttr);
	}

	@Test
	public void getEntitylessAttributeWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributeWhenOnlyDefinitionExists");

		AttributeDefinition attrDef = setUpEntitylessAttributeDefinition();

		Attribute returnedAttr = cacheManager.getEntitylessAttribute(attrDef.getName(), subject);

		assertEquals("returned attribute is not same as stored", attrDef, returnedAttr);
	}

	@Test(expected = AttributeNotExistsException.class)
	public void getEntitylessAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributeNotExists");

		cacheManager.getEntitylessAttribute("name", subject);
	}

	@Test
	public void getEntitylessAttrValue() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttrValue");

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		String value = cacheManager.getEntitylessAttrValue(attr.getId(), subject);

		assertEquals("returned attribute value is not same as stored", attr.getValue(), value);
	}

	@Test
	public void getEntitylessAttrValueNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttrValueNotExists");

		String value = cacheManager.getEntitylessAttrValue(0, subject);

		assertEquals("returned attribute value is not same as stored", null, value);
	}

	@Test
	public void getEntitylessAttrKeys() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttrKeys");

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);
		cacheManager.setEntitylessAttribute(attr, subject1);

		List<String> keys = cacheManager.getEntitylessAttrKeys(attr.getName());

		assertTrue("result should contain this entityless attribute subject", keys.contains(subject));
		assertTrue("result should contain this entityless attribute subject", keys.contains(subject1));
		assertTrue("it should return 2 attributes", keys.size() == 2);
	}

	@Test
	public void getEntitylessAttrKeysEmpty() {
		System.out.println(CLASS_NAME + "getEntitylessAttrKeysEmpty");

		AttributeDefinition attrDef = setUpEntitylessAttributeDefinition();
		List<String> keys = cacheManager.getEntitylessAttrKeys(attrDef.getName());

		assertTrue("there should be no returned keys", keys.isEmpty());
	}

	@Test
	public void getAllValuesByPrimaryHolderType() throws Exception {
		System.out.println(CLASS_NAME + "getAllValuesByPrimaryHolderType");

		AttributeDefinition attrDef = setUpUserAttributeDefinition();
		Attribute userAttr = new Attribute(attrDef);
		Attribute userAttr1 = new Attribute(attrDef);
		userAttr.setValue("value");
		userAttr1.setValue("value1");

		cacheManager.setAttribute(userAttr, userHolder, null);
		cacheManager.setAttribute(userAttr1, userHolder1, null);

		Attribute attr = setUpUserFacilityAttribute();
		cacheManager.setAttribute(attr, userHolder, facilityHolder);

		List<Object> values = cacheManager.getAllValues(Holder.HolderType.USER, attrDef);

		assertTrue("result should contain this attribute value", values.contains(userAttr.getValue()));
		assertTrue("result should contain this attribute value", values.contains(userAttr1.getValue()));
		assertTrue("it should return 2 attributes", values.size() == 2);
	}

	@Test
	public void getAllValuesByPrimaryHolderTypeEmpty() {
		System.out.println(CLASS_NAME + "getAllValuesByPrimaryHolderTypeEmpty");

		AttributeDefinition attrDef = setUpUserAttributeDefinition();
		List<Object> values = cacheManager.getAllValues(Holder.HolderType.USER, attrDef);

		assertTrue("it should return no attributes", values.isEmpty());
	}

	@Test
	public void getAllValuesByPrimaryAndSecondaryHolderType() throws Exception {
		System.out.println(CLASS_NAME + "getAllValuesByPrimaryAndSecondaryHolderType");

		AttributeDefinition attrDef = setUpGroupResourceAttributeDefinition();
		Attribute groupResourceAttr = new Attribute(attrDef);
		Attribute groupResourceAttr1 = new Attribute(attrDef);
		groupResourceAttr.setValue("value");
		groupResourceAttr1.setValue("value1");

		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		cacheManager.setAttribute(groupResourceAttr1, groupHolder, resourceHolder1);

		Attribute attr = setUpGroupAttribute();
		cacheManager.setAttribute(attr, groupHolder, null);

		List<Object> values = cacheManager.getAllValues(Holder.HolderType.GROUP, Holder.HolderType.RESOURCE, attrDef);

		assertTrue("result should contain this attribute value", values.contains(groupResourceAttr.getValue()));
		assertTrue("result should contain this attribute value", values.contains(groupResourceAttr1.getValue()));
		assertTrue("it should return 2 attributes", values.size() == 2);
	}

	@Test
	public void getAllValuesByPrimaryAndSecondaryHolderTypeEmpty() {
		System.out.println(CLASS_NAME + "getAllValuesByPrimaryAndSecondaryHolderTypeEmpty");

		AttributeDefinition attrDef = setUpGroupResourceAttributeDefinition();
		List<Object> values = cacheManager.getAllValues(Holder.HolderType.GROUP, Holder.HolderType.RESOURCE, attrDef);

		assertTrue("it should return no attributes", values.isEmpty());
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getAttributesByIdsAndPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByIdsAndPrimaryHolder");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		AttributeDefinition groupAttrDef = setUpGroupAttributeDefinition();
		AttributeDefinition resourceAttrDef = setUpResourceAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);

		List<Integer> attributeIds = new ArrayList<>();
		attributeIds.add(groupAttr.getId());
		attributeIds.add(groupResourceAttr.getId());
		attributeIds.add(groupAttrDef.getId());
		attributeIds.add(resourceAttrDef.getId());

		List<Attribute> attrs = cacheManager.getAttributesByIds(attributeIds, groupHolder);

		assertTrue("result should contain group attribute", attrs.contains(groupAttr));
		assertTrue("result should contain group attribute definition", attrs.contains(groupAttrDef));
		assertTrue("it should return 2 attributes", attrs.size() == 2);
	}

	@Test
	public void getAttributesByIdsAndPrimaryHolderEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByIdsAndPrimaryHolderEmpty");

		List<Integer> attributeIds = new ArrayList<>();
		List<Attribute> attrs = cacheManager.getAttributesByIds(attributeIds, groupHolder);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getAttributesByIdsAndHolders() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByIdsAndHolders");

		Attribute groupAttr = setUpGroupAttribute();
		Attribute memberGroupAttr = setUpMemberGroupAttribute();
		AttributeDefinition memberGroupAttrDef = setUpMemberGroupAttributeDefinition();
		AttributeDefinition groupResourceAttrDef = setUpGroupResourceAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(memberGroupAttr, memberHolder, groupHolder);

		List<Integer> attributeIds = new ArrayList<>();
		attributeIds.add(groupAttr.getId());
		attributeIds.add(memberGroupAttr.getId());
		attributeIds.add(memberGroupAttrDef.getId());
		attributeIds.add(groupResourceAttrDef.getId());

		List<Attribute> attrs = cacheManager.getAttributesByIds(attributeIds, memberHolder, groupHolder);

		assertTrue("result should contain member-group attribute", attrs.contains(memberGroupAttr));
		assertTrue("result should contain member-group attribute definition", attrs.contains(memberGroupAttrDef));
		assertTrue("it should return 2 attributes", attrs.size() == 2);
	}

	@Test
	public void getAttributesByIdsAndHoldersEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByIdsAndHoldersEmpty");

		List<Integer> attributeIds = new ArrayList<>();
		List<Attribute> attrs = cacheManager.getAttributesByIds(attributeIds, groupHolder, resourceHolder);

		assertTrue("there should be no returned attributes", attrs.size() == 0);
	}

	@Test
	public void checkAttributeExists() {
		System.out.println(CLASS_NAME + "checkAttributeExists");

		AttributeDefinition attrDef = setUpEntitylessAttributeDefinition();

		assertTrue("attribute should exist", cacheManager.checkAttributeExists(attrDef));
	}









// SET METHODS TESTS ----------------------------------------------


	@Test
	public void setAttributeWithPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "setAttributeWithPrimaryHolder");

		Attribute attribute = setUpGroupAttribute();
		cacheManager.setAttribute(attribute, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeById(attribute.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeByName(attribute.getName(), groupHolder, null));
	}

	@Test
	public void setAttributeWithHolders() throws Exception {
		System.out.println(CLASS_NAME + "setAttributeWithHolders");

		Attribute attribute = setUpGroupResourceAttribute();
		cacheManager.setAttribute(attribute, groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeById(attribute.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeByName(attribute.getName(), groupHolder, resourceHolder));
	}

	@Test
	public void setAttributeWithExistenceCheckWithPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "setAttributeWithExistenceCheckWithPrimaryHolder");

		Attribute attribute = setUpGroupAttribute();
		cacheManager.setAttributeWithExistenceCheck(attribute, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeById(attribute.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeByName(attribute.getName(), groupHolder, null));

		String createdAt = attribute.getValueCreatedAt();
		String createdBy = attribute.getValueCreatedBy();

		attribute.setValueCreatedAt(timeModified);
		attribute.setValueCreatedBy(modifier);
		attribute.setValueModifiedAt(timeModified);
		attribute.setValueModifiedBy(modifier);
		cacheManager.setAttributeWithExistenceCheck(attribute, groupHolder, null);

		Attribute attributeById = cacheManager.getAttributeById(attribute.getId(), groupHolder, null);
		Attribute attributeByName = cacheManager.getAttributeByName(attribute.getName(), groupHolder, null);

		assertEquals("returned attribute is not same as stored", attribute, attributeById);
		assertEquals("returned attribute is not same as stored", attribute, attributeByName);
		assertEquals("returned attribute valueModifiedAt is not same as stored", timeModified, attributeById.getValueModifiedAt());
		assertEquals("returned attribute valueModifiedAt is not same as stored", timeModified, attributeByName.getValueModifiedAt());
		assertEquals("returned attribute valueModifiedBy is not same as stored", modifier, attributeById.getValueModifiedBy());
		assertEquals("returned attribute valueModifiedBy is not same as stored", modifier, attributeByName.getValueModifiedBy());
		assertEquals("returned attribute valueCreatedAt is not same as stored", createdAt, attributeById.getValueCreatedAt());
		assertEquals("returned attribute valueCreatedAt is not same as stored", createdAt, attributeByName.getValueCreatedAt());
		assertEquals("returned attribute valueCreatedBy is not same as stored", createdBy, attributeById.getValueCreatedBy());
		assertEquals("returned attribute valueCreatedBy is not same as stored", createdBy, attributeByName.getValueCreatedBy());
	}

	@Test
	public void setAttributeWithExistenceCheckWithHolders() throws Exception {
		System.out.println(CLASS_NAME + "setAttributeWithExistenceCheckWithHolders");

		Attribute attribute = setUpGroupAttribute();
		cacheManager.setAttributeWithExistenceCheck(attribute, groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeById(attribute.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeByName(attribute.getName(), groupHolder, resourceHolder));

		String createdAt = attribute.getValueCreatedAt();
		String createdBy = attribute.getValueCreatedBy();

		attribute.setValueCreatedAt(timeModified);
		attribute.setValueCreatedBy(modifier);
		attribute.setValueModifiedAt(timeModified);
		attribute.setValueModifiedBy(modifier);
		cacheManager.setAttributeWithExistenceCheck(attribute, groupHolder, resourceHolder);

		Attribute attributeById = cacheManager.getAttributeById(attribute.getId(), groupHolder, resourceHolder);
		Attribute attributeByName = cacheManager.getAttributeByName(attribute.getName(), groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attribute, attributeById);
		assertEquals("returned attribute is not same as stored", attribute, attributeByName);
		assertEquals("returned attribute valueModifiedAt is not same as stored", timeModified, attributeById.getValueModifiedAt());
		assertEquals("returned attribute valueModifiedAt is not same as stored", timeModified, attributeByName.getValueModifiedAt());
		assertEquals("returned attribute valueModifiedBy is not same as stored", modifier, attributeById.getValueModifiedBy());
		assertEquals("returned attribute valueModifiedBy is not same as stored", modifier, attributeByName.getValueModifiedBy());
		assertEquals("returned attribute valueCreatedAt is not same as stored", createdAt, attributeById.getValueCreatedAt());
		assertEquals("returned attribute valueCreatedAt is not same as stored", createdAt, attributeByName.getValueCreatedAt());
		assertEquals("returned attribute valueCreatedBy is not same as stored", createdBy, attributeById.getValueCreatedBy());
		assertEquals("returned attribute valueCreatedBy is not same as stored", createdBy, attributeByName.getValueCreatedBy());
	}

	@Test
	public void setAttributeDefinition() throws Exception {
		System.out.println(CLASS_NAME + "setAttributeDefinition");

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();

		assertEquals("returned attribute definition is not same as stored", attrDef, cacheManager.getAttributeDefinition(attrDef.getId()));
		assertEquals("returned attribute definition is not same as stored", attrDef, cacheManager.getAttributeDefinition(attrDef.getName()));
	}

	@Test
	public void setEntitylessAttribute() throws Exception {
		System.out.println(CLASS_NAME + "setEntitylessAttribute");

		Attribute entitylessAttr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(entitylessAttr, subject);

		assertEquals("returned entityless attribute is not same as stored", entitylessAttr, cacheManager.getEntitylessAttribute(entitylessAttr.getName(), subject));
		assertEquals("returned entityless attribute value is not same as stored", entitylessAttr.getValue(), cacheManager.getEntitylessAttrValue(entitylessAttr.getId(), subject));
	}

	@Test
	public void setEntitylessAttributeWithExistenceCheck() throws Exception {
		System.out.println(CLASS_NAME + "setEntitylessAttributeWithExistenceCheck");

		Attribute entitylessAttr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttributeWithExistenceCheck(entitylessAttr, subject);

		assertEquals("returned entityless attribute is not same as stored", entitylessAttr, cacheManager.getEntitylessAttribute(entitylessAttr.getName(), subject));
		assertEquals("returned entityless attribute value is not same as stored", entitylessAttr.getValue(), cacheManager.getEntitylessAttrValue(entitylessAttr.getId(), subject));

		String createdAt = entitylessAttr.getValueCreatedAt();
		String createdBy = entitylessAttr.getValueCreatedBy();

		entitylessAttr.setValueCreatedAt(timeModified);
		entitylessAttr.setValueCreatedBy(modifier);
		entitylessAttr.setValueModifiedAt(timeModified);
		entitylessAttr.setValueModifiedBy(modifier);
		cacheManager.setEntitylessAttributeWithExistenceCheck(entitylessAttr, subject);

		Attribute attrByNameAndKey = cacheManager.getEntitylessAttribute(entitylessAttr.getName(), subject);

		assertEquals("returned attribute is not same as stored", entitylessAttr, attrByNameAndKey);
		assertEquals("returned attribute valueModifiedAt is not same as stored", timeModified, attrByNameAndKey.getValueModifiedAt());
		assertEquals("returned attribute valueModifiedBy is not same as stored", modifier, attrByNameAndKey.getValueModifiedBy());
		assertEquals("returned attribute valueCreatedAt is not same as stored", createdAt, attrByNameAndKey.getValueCreatedAt());
		assertEquals("returned attribute valueCreatedBy is not same as stored", createdBy, attrByNameAndKey.getValueCreatedBy());
	}

	@Test
	public void removeAttributeWithPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "removeAttributeWithPrimaryHolder");

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Test");
		cacheManager.setAttribute(attribute, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeById(attribute.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeByName(attribute.getName(), groupHolder, null));

		cacheManager.removeAttribute(attribute, groupHolder, null);

		assertEquals("should return only attribute definition", attrDef, cacheManager.getAttributeById(attribute.getId(), groupHolder, null));
		assertEquals("should return only attribute definition", attrDef, cacheManager.getAttributeByName(attribute.getName(), groupHolder, null));
	}

	@Test
	public void removeAttributeWithHolders() throws Exception {
		System.out.println(CLASS_NAME + "removeAttributeWithHolders");

		AttributeDefinition attrDef = setUpGroupResourceAttributeDefinition();
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Test");
		cacheManager.setAttribute(attribute, groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeById(attribute.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getAttributeByName(attribute.getName(), groupHolder, resourceHolder));

		cacheManager.removeAttribute(attribute, groupHolder, resourceHolder);

		assertEquals("should return only attribute definition", attrDef, cacheManager.getAttributeById(attribute.getId(), groupHolder, resourceHolder));
		assertEquals("should return only attribute definition", attrDef, cacheManager.getAttributeByName(attribute.getName(), groupHolder, resourceHolder));
	}

	@Test
	public void removeEntitylessAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeEntitylessAttribute");

		AttributeDefinition attrDef = setUpEntitylessAttributeDefinition();
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Test");
		cacheManager.setEntitylessAttribute(attribute, subject);

		assertEquals("returned attribute is not same as stored", attribute, cacheManager.getEntitylessAttribute(attribute.getName(), subject));

		cacheManager.removeEntitylessAttribute(attribute, subject);

		assertEquals("should return only attribute definition", attrDef, cacheManager.getEntitylessAttribute(attribute.getName(), subject));
	}

	@Test
	public void updateAttributeDefinition() throws Exception {
		System.out.println(CLASS_NAME + "updateAttributeDefinition");

		AttributeDefinition attrDef = setUpGroupAttributeDefinition();

		String createdAt = attrDef.getCreatedAt();
		String createdBy = attrDef.getCreatedBy();

		attrDef.setCreatedAt(timeModified);
		attrDef.setCreatedBy(modifier);
		attrDef.setModifiedAt(timeModified);
		attrDef.setModifiedBy(modifier);
		cacheManager.updateAttributeDefinition(attrDef);

		AttributeDefinition attrDefById = cacheManager.getAttributeDefinition(attrDef.getId());
		AttributeDefinition attrDefByName = cacheManager.getAttributeDefinition(attrDef.getName());

		assertEquals("returned attribute definition is not same as stored", attrDef, attrDefById);
		assertEquals("returned attribute definition is not same as stored", attrDef, attrDefByName);
		assertEquals("returned attribute definition modifiedAt is not same as stored", timeModified, attrDefById.getModifiedAt());
		assertEquals("returned attribute definition modifiedAt is not same as stored", timeModified, attrDefByName.getModifiedAt());
		assertEquals("returned attribute definition modifiedBy is not same as stored", modifier, attrDefById.getModifiedBy());
		assertEquals("returned attribute definition modifiedBy is not same as stored", modifier, attrDefByName.getModifiedBy());
		assertEquals("returned attribute definition createdAt is not same as stored", createdAt, attrDefById.getCreatedAt());
		assertEquals("returned attribute definition createdAt is not same as stored", createdAt, attrDefByName.getCreatedAt());
		assertEquals("returned attribute definition createdBy is not same as stored", createdBy, attrDefById.getCreatedBy());
		assertEquals("returned attribute definition createdBy is not same as stored", createdBy, attrDefByName.getCreatedBy());
	}

	@Test
	public void removeAllAttributesByPrimaryHolder() throws Exception {
		System.out.println(CLASS_NAME + "removeAllAttributesByPrimaryHolder");

		AttributeDefinition groupAttrDef = setUpGroupAttributeDefinition();
		AttributeDefinition groupAttrDef1 = setUpGroupAttributeDefinition1();
		Attribute groupAttr = new Attribute(groupAttrDef);
		groupAttr.setValue("value");
		Attribute groupAttr1 = new Attribute(groupAttrDef1);
		groupAttr1.setValue("value1");
		Attribute groupResourceAttr = setUpGroupResourceAttribute();
		AttributeDefinition virtAttr = setUpVirtualGroupAttribute();
		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupAttr1, groupHolder, null);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr1, cacheManager.getAttributeById(groupAttr1.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr1, cacheManager.getAttributeByName(groupAttr1.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", virtAttr, cacheManager.getAttributeDefinition(virtAttr.getName()));

		cacheManager.removeAllAttributes(groupHolder);

		assertEquals("only attribute definition should exist", groupAttrDef, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("only attribute definition should exist", groupAttrDef, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("only attribute definition should exist", groupAttrDef1, cacheManager.getAttributeById(groupAttr1.getId(), groupHolder, null));
		assertEquals("only attribute definition should exist", groupAttrDef1, cacheManager.getAttributeByName(groupAttr1.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", virtAttr, cacheManager.getAttributeDefinition(virtAttr.getName()));
	}

	@Test
	public void removeAllAttributesByHolders() throws Exception {
		System.out.println(CLASS_NAME + "removeAllAttributesByHolders");

		AttributeDefinition groupResourceAttrDef = setUpGroupResourceAttributeDefinition();
		Attribute groupResourceAttr = new Attribute(groupResourceAttrDef);
		groupResourceAttr.setValue("value");
		Attribute groupAttr = setUpGroupAttribute();
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		cacheManager.setAttribute(groupAttr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));

		cacheManager.removeAllAttributes(groupHolder, resourceHolder);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));
	}

	@Test
	public void removeAllAttributesByPrimaryHolderAndSecondaryHolderType() throws Exception {
		System.out.println(CLASS_NAME + "removeAllAttributesByPrimaryHolderAndSecondaryHolderType");

		AttributeDefinition groupResourceAttrDef = setUpGroupResourceAttributeDefinition();
		Attribute groupResourceAttr = new Attribute(groupResourceAttrDef);
		groupResourceAttr.setValue("value");
		Attribute groupAttr = setUpGroupAttribute();
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder1);
		cacheManager.setAttribute(groupAttr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder1));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder1));

		cacheManager.removeAllAttributes(groupHolder, Holder.HolderType.RESOURCE);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder1));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder1));
	}

	@Test
	public void removeAllAttributesBySecondaryHolderAndPrimaryHolderType() throws Exception {
		System.out.println(CLASS_NAME + "removeAllAttributesBySecondaryHolderAndPrimaryHolderType");

		AttributeDefinition groupResourceAttrDef = setUpGroupResourceAttributeDefinition();
		Attribute groupResourceAttr = new Attribute(groupResourceAttrDef);
		groupResourceAttr.setValue("value");
		Attribute groupAttr = setUpGroupAttribute();
		cacheManager.setAttribute(groupResourceAttr, groupHolder, resourceHolder);
		cacheManager.setAttribute(groupResourceAttr, groupHolder1, resourceHolder);
		cacheManager.setAttribute(groupAttr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder1, resourceHolder));
		assertEquals("returned attribute is not same as stored", groupResourceAttr, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder1, resourceHolder));

		cacheManager.removeAllAttributes(Holder.HolderType.GROUP, resourceHolder);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder, resourceHolder));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder, resourceHolder));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeById(groupResourceAttr.getId(), groupHolder1, resourceHolder));
		assertEquals("only attribute definition should exist", groupResourceAttrDef, cacheManager.getAttributeByName(groupResourceAttr.getName(), groupHolder1, resourceHolder));
	}







// PRIVATE METHODS ----------------------------------------------


	private List<AttributeDefinition> setUpAttributesDefinitions() {

		List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(setUpGroupAttributeDefinition());
		attributeDefinitions.add(setUpGroupAttributeDefinition1());
		attributeDefinitions.add(setUpResourceAttributeDefinition());

		return attributeDefinitions;
	}

	private AttributeDefinition setUpGroupAttributeDefinition() {
		return setUpAttributeDefinition(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute-definition");
	}

	private AttributeDefinition setUpGroupAttributeDefinition1() {
		return setUpAttributeDefinition(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute-definition1");
	}

	private AttributeDefinition setUpUserAttributeDefinition() {
		return setUpAttributeDefinition(AttributesManager.NS_USER_ATTR_OPT, "user-test-attribute-definition");
	}

	private AttributeDefinition setUpResourceAttributeDefinition() {
		return setUpAttributeDefinition(AttributesManager.NS_RESOURCE_ATTR_OPT, "resource-test-attribute-definition");
	}

	private AttributeDefinition setUpGroupResourceAttributeDefinition() {
		return setUpAttributeDefinition(AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT, "group-resource-test-attribute-definition");
	}

	private AttributeDefinition setUpMemberGroupAttributeDefinition() {
		return setUpAttributeDefinition(AttributesManager.NS_MEMBER_GROUP_ATTR_OPT, "member-group-test-attribute-definition");
	}

	private AttributeDefinition setUpEntitylessAttributeDefinition() {
		return setUpAttributeDefinition(AttributesManager.NS_ENTITYLESS_ATTR_DEF, "entityless-test-attribute-definition");
	}

	private AttributeDefinition setUpVirtualMemberAttribute() {
		return setUpAttributeDefinition(AttributesManager.NS_MEMBER_ATTR_VIRT, "member-test-virtual-attribute");
	}

	private AttributeDefinition setUpVirtualGroupAttribute() {
		return setUpAttributeDefinition(AttributesManager.NS_GROUP_ATTR_VIRT, "group-test-virtual-attribute");
	}

	private AttributeDefinition setUpVirtualMemberResourceAttribute() {
		return setUpAttributeDefinition(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT,"member-resource-test-virtual-attribute");
	}

	private AttributeDefinition setUpVirtualGroupResourceAttribute() {
		return setUpAttributeDefinition(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT, "group-resource-test-virtual-attribute");
	}

	private AttributeDefinition setUpVirtualUserFacilityAttribute() {
		return setUpAttributeDefinition(AttributesManager.NS_USER_FACILITY_ATTR_VIRT, "user-facility-test-virtual-attribute");
	}

	private Attribute setUpGroupAttribute() {
		return setUpAttribute(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute", "GroupAttribute");
	}

	private Attribute setUpUserAttribute() {
		return setUpAttribute(AttributesManager.NS_USER_ATTR_OPT, "user-test-attribute", "UserAttribute");
	}

	private Attribute setUpGroupResourceAttribute() {
		return setUpAttribute(AttributesManager.NS_GROUP_RESOURCE_ATTR_OPT, "group-resource-test-attribute", "GroupResourceAttribute");
	}

	private Attribute setUpMemberGroupAttribute() {
		return setUpAttribute(AttributesManager.NS_MEMBER_GROUP_ATTR_OPT, "member-group-test-attribute", "MemberGroupAttribute");
	}

	private Attribute setUpUserFacilityAttribute() {
		return setUpAttribute(AttributesManager.NS_USER_FACILITY_ATTR_OPT, "user-facility-test-attribute", "UserFacilityAttribute");
	}

	private Attribute setUpEntitylessAttribute() {
		return setUpAttribute(AttributesManager.NS_ENTITYLESS_ATTR_DEF, "entityless-test-attribute", "EntitylessAttribute");
	}

	private AttributeDefinition setUpAttributeDefinition(String namespace, String friendlyName) {

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(namespace);
		attr.setFriendlyName(friendlyName);
		attr.setType(String.class.getName());
		attr.setId(id);
		id++;

		attr.setCreatedAt(timeCreated);
		attr.setCreatedBy(creator);
		attr.setModifiedAt(timeCreated);
		attr.setModifiedBy(creator);

		cacheManager.setAttributeDefinition(attr);

		return attr;
	}

	private Attribute setUpAttribute(String namespace, String friendlyName, String value) {

		AttributeDefinition attributeDefinition = setUpAttributeDefinition(namespace, friendlyName);
		Attribute attr = new Attribute(attributeDefinition);

		attr.setValue(value);
		attr.setValueCreatedAt(timeCreated);
		attr.setValueCreatedBy(creator);
		attr.setValueModifiedAt(timeCreated);
		attr.setValueModifiedBy(creator);

		return attr;
	}
}
