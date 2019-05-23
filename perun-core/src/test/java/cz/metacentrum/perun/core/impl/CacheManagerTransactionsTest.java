package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Holder;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Cache manager tests with transactions.
 * Tests if the methods work as expected in transactions, especially in nested transactions.
 *
 * @author Simona Kruppova
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional(transactionManager = "perunTestTransactionManager")
public class CacheManagerTransactionsTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "CacheManagerTransactions.";

	private static CacheManager cacheManager;

	private static int id = 0;

	private Group group;
	private Vo vo;

	private Holder groupHolder;
	private Holder groupHolder1;
	private final String subject = "Test subject";
	private final String timeCreated = "2016-04-24";
	private final String creator = "Admin";

	@Before
	public void setUp() throws Exception {
		cacheManager = perun.getCacheManager();
		CacheManager.setCacheDisabled(false);

		//CacheManagerTransactionsTest counts with empty cache
		cacheManager.clearCache();

		this.setUpWorld();
	}

	@AfterClass
	public static void initializeCache() throws Exception {
		//Initialize cache again after this test class ended
		cacheManager.initialize(sess, ((PerunBlImpl)sess.getPerun()).getAttributesManagerImpl());
	}

	private void setUpWorld() throws Exception {
		this.groupHolder = new Holder(0, Holder.HolderType.GROUP);
		this.groupHolder1 = new Holder(1, Holder.HolderType.GROUP);

		vo = this.setUpVo();
		group = this.setUpGroup();
	}

	private Group setUpGroup() throws Exception {
		Group group = perun.getGroupsManager().createGroup(sess, vo, new Group("AttrTestGroup","AttrTestGroupDescription"));
		assertNotNull("unable to create a group", group);
		return group;
	}

	private Vo setUpVo() throws Exception {
		Vo vo = new Vo();
		vo.setName("CacheManagerTestVo");
		vo.setShortName("CMTVO");
		assertNotNull("unable to create VO", perun.getVosManager().createVo(sess, vo));
		return vo;
	}

	@Test
	public void wasCacheUpdatedInTransaction() throws Exception {
		System.out.println(CLASS_NAME + "wasCacheUpdatedInTransaction");

		assertTrue("cache should not be updated in transaction", !cacheManager.wasCacheUpdatedInTransaction());

		Attribute attr = setUpGroupAttribute();
		cacheManager.setAttribute(attr, groupHolder, null);

		assertTrue("cache should have been updated in transaction", cacheManager.wasCacheUpdatedInTransaction());
	}

	@Test
	public void deleteAttributeWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteAttributeWhenOnlyDefinitionExists");

		AttributeDefinition attributeDefinition = setUpGroupAttributeDefinition();

		// commit is needed, else deleteAttribute will try to reinitialize cache (because index is updated only after commit happens)
		cacheManager.commit();
		// new transaction needed, else the rollback called after this method by spring would throw exception
		cacheManager.newTopLevelTransaction();

		assertEquals("returned attribute definition is not same as stored", attributeDefinition, cacheManager.getAttributeDefinition(attributeDefinition.getId()));

		cacheManager.deleteAttribute(attributeDefinition.getId(), sess, ((PerunBlImpl)perun).getAttributesManagerImpl());

		try {
			cacheManager.getAttributeDefinition(attributeDefinition.getId());
			throw new InternalErrorException("attribute definition should not exist after being deleted");
		} catch (AttributeNotExistsException e) {
			try {
				cacheManager.getAttributeDefinition(attributeDefinition.getName());
				throw new InternalErrorException("attribute definition should not exist after being deleted");
			} catch (AttributeNotExistsException ex) {
				//this is ok, attribute definition should not exist
			}
		}
	}

	@Test
	public void deleteAttribute() throws Exception {
		System.out.println(CLASS_NAME + "deleteAttribute");

		AttributeDefinition entitylessAttrDef = setUpEntitylessAttributeDefinition();
		AttributeDefinition attributeDefinition = setUpGroupAttributeDefinition();
		Attribute groupAttr = new Attribute(attributeDefinition);
		groupAttr.setValue("value");
		Attribute groupAttr1 = setUpGroupAttribute1();

		cacheManager.setAttribute(groupAttr, groupHolder, null);
		cacheManager.setAttribute(groupAttr, groupHolder1, null);
		cacheManager.setAttribute(groupAttr1, groupHolder, null);

		// commit is needed, else deleteAttribute will try to reinitialize cache (because index is updated only after commit happens)
		cacheManager.commit();
		// new transaction needed, else the rollback called after this method by spring would throw exception
		cacheManager.newTopLevelTransaction();

		assertEquals("returned attribute definition is not same as stored", attributeDefinition, cacheManager.getAttributeDefinition(attributeDefinition.getId()));
		assertEquals("returned entityless attribute definition is not same as stored", entitylessAttrDef, cacheManager.getAttributeDefinition(entitylessAttrDef.getId()));
		assertEquals("returned attribute is not same as stored", groupAttr1, cacheManager.getAttributeByName(groupAttr1.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder1, null));

		cacheManager.deleteAttribute(groupAttr.getId(), sess, ((PerunBlImpl)perun).getAttributesManagerImpl());

		assertEquals("entityless attribute definition should exist", entitylessAttrDef, cacheManager.getAttributeDefinition(entitylessAttrDef.getId()));
		assertEquals("group attribute should exist", groupAttr1, cacheManager.getAttributeByName(groupAttr1.getName(), groupHolder, null));

		try {
			cacheManager.getAttributeDefinition(attributeDefinition.getId());
			throw new InternalErrorException("attribute definition should not exist after being deleted");
		} catch (AttributeNotExistsException e) {
			try {
				cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null);
				throw new InternalErrorException("attribute should not exist after being deleted");
			} catch (AttributeNotExistsException ex) {
				try {
					cacheManager.getAttributeByName(groupAttr.getName(), groupHolder1, null);
					throw new InternalErrorException("attribute should not exist after being deleted");
				} catch (AttributeNotExistsException exc) {
					//this is ok, attribute should not exist
				}
			}
		}
	}

	@Test
	public void deleteAttributeWithCacheReinitialized() throws Exception {
		System.out.println(CLASS_NAME + "initializeCache");

		Attribute groupAttr = setUpGroupAttributeForAttributesManager();
		Attribute groupAttr1 = setUpGroupAttribute1ForAttributesManager();
		Attribute entitylessAttr = setUpEntitylessAttributeForAttributesManager();
		((PerunBlImpl)perun).getAttributesManagerImpl().setAttribute(sess, group, groupAttr);
		((PerunBlImpl)perun).getAttributesManagerImpl().setAttribute(sess, group, groupAttr1);
		((PerunBlImpl)perun).getAttributesManagerImpl().setAttribute(sess, subject, entitylessAttr);

		Holder groupHolder = new Holder(group.getId(), Holder.HolderType.GROUP);

		assertEquals("returned attribute is not same as stored", groupAttr, cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr1, cacheManager.getAttributeByName(groupAttr1.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", entitylessAttr, cacheManager.getEntitylessAttribute(entitylessAttr.getName(), subject));

		int numOfAttrDefs = ((PerunBlImpl)perun).getAttributesManagerImpl().getAttributesDefinition(sess).size();

		((PerunBlImpl)perun).getAttributesManagerImpl().deleteAttribute(sess, groupAttr);

		// commit is needed, else getAttributesDefinitions will not work (because index is updated only after commit happens)
		cacheManager.commit();
		// new transaction needed, else the rollback called after this method by spring would throw exception
		cacheManager.newTopLevelTransaction();

		assertTrue("there should be one less definition after deleting attribute", numOfAttrDefs - 1 == cacheManager.getAttributesDefinitions().size());

		assertEquals("returned attribute is not same as stored", groupAttr1, cacheManager.getAttributeByName(groupAttr1.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", groupAttr1, cacheManager.getAttributeById(groupAttr1.getId(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", entitylessAttr, cacheManager.getEntitylessAttribute(entitylessAttr.getName(), subject));

		try {
			cacheManager.getAttributeByName(groupAttr.getName(), groupHolder, null);
			throw new InternalErrorException("attribute should not exist after being deleted");
		} catch (AttributeNotExistsException e) {
			try {
				cacheManager.getAttributeById(groupAttr.getId(), groupHolder, null);
				throw new InternalErrorException("attribute should not exist after being deleted");
			} catch (AttributeNotExistsException ex) {
				// this is ok, attribute should not exist
			}
		}
	}

	@Test
	public void getAttributeInNestedTransactionWithClean() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeInNestedTransactionWithClean");

		cacheManager.newNestedTransaction();

		Attribute attr = setUpGroupAttribute();
		cacheManager.setAttribute(attr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.cleanNestedTransaction();

		try {
			cacheManager.getAttributeByName(attr.getName(), groupHolder, null);
			throw new InternalErrorException("attribute should not exist after transaction clean");
		} catch (AttributeNotExistsException e) {
			try{
				cacheManager.getAttributeById(attr.getId(), groupHolder, null);
				throw new InternalErrorException("attribute should not exist after transaction clean");
			} catch (AttributeNotExistsException ex) {
				//this is ok, attribute should not exist
			}
		}
	}

	@Test
	public void getAttributeInNestedTransactionWithCleanWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeInNestedTransactionWithCleanWhenOnlyDefinitionExists");

		AttributeDefinition attributeDefinition = setUpGroupAttributeDefinition();

		cacheManager.newNestedTransaction();

		Attribute attr = new Attribute(attributeDefinition);
		attr.setValue("Test");
		cacheManager.setAttribute(attr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.cleanNestedTransaction();

		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeById(attr.getId(), groupHolder, null));
	}

	@Test
	public void getAttributeInNestedTransactionWithFlush() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeInNestedTransactionWithFlush");

		cacheManager.newNestedTransaction();

		Attribute attr = setUpGroupAttribute();
		cacheManager.setAttribute(attr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.flushNestedTransaction();

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));
	}

	@Test
	public void getAttributeWithManyNestedTransactions() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeWithManyNestedTransactions");

		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();

		Attribute attr = setUpGroupAttribute();
		cacheManager.setAttribute(attr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.newNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.cleanNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.cleanNestedTransaction();

		try {
			cacheManager.getAttributeByName(attr.getName(), groupHolder, null);
			throw new InternalErrorException("attribute should not exist after transaction clean");
		} catch (AttributeNotExistsException e) {
			try{
				cacheManager.getAttributeById(attr.getId(), groupHolder, null);
				throw new InternalErrorException("attribute should not exist after transaction clean");
			} catch (AttributeNotExistsException ex) {
				//this is ok, attribute should not exist
			}
		}
	}

	@Test
	public void getAttributeInNestedTransactionWithRemove() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeByNameInNestedTransactionWithRemove");

		AttributeDefinition attributeDefinition = setUpGroupAttributeDefinition();

		cacheManager.newNestedTransaction();

		Attribute attr = new Attribute(attributeDefinition);
		attr.setValue("Test");
		cacheManager.setAttribute(attr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.removeAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.setAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.removeAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeById(attr.getId(), groupHolder, null));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeById(attr.getId(), groupHolder, null));
	}

	@Test
	public void getEntitylessAttributeInNestedTransactionWithClean() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributeInNestedTransactionWithClean");

		cacheManager.newNestedTransaction();

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.cleanNestedTransaction();

		try {
			cacheManager.getEntitylessAttribute(attr.getName(), subject);
			throw new InternalErrorException("attribute should not exist after transaction clean");
		} catch (AttributeNotExistsException e) {
			//this is ok, attribute should not exist
		}
	}

	@Test
	public void getEntitylessAttributeInNestedTransactionWithCleanWhenOnlyDefinitionExists() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributeInNestedTransactionWithCleanWhenOnlyDefinitionExists");

		AttributeDefinition attributeDefinition = setUpEntitylessAttributeDefinition();

		cacheManager.newNestedTransaction();

		Attribute attr = new Attribute(attributeDefinition);
		attr.setValue("Test");
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.cleanNestedTransaction();

		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getEntitylessAttribute(attr.getName(), subject));
	}

	@Test
	public void getEntitylessAttributeInNestedTransactionWithFlush() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributeInNestedTransactionWithFlush");

		cacheManager.newNestedTransaction();

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.flushNestedTransaction();

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));
	}

	@Test
	public void getEntitylessAttributeWithManyNestedTransactions() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributeWithManyNestedTransactions");

		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.newNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.cleanNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.cleanNestedTransaction();

		try {
			cacheManager.getEntitylessAttribute(attr.getName(), subject);
			throw new InternalErrorException("attribute should not exist after transaction clean");
		} catch (AttributeNotExistsException e) {
			//this is ok, attribute should not exist
		}
	}

	@Test
	public void getEntitylessAttributeInNestedTransactionWithRemove() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttributeInNestedTransactionWithRemove");

		AttributeDefinition attributeDefinition = setUpEntitylessAttributeDefinition();

		cacheManager.newNestedTransaction();

		Attribute attr = new Attribute(attributeDefinition);
		attr.setValue("Test");
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.removeEntitylessAttribute(attr, subject);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.setEntitylessAttribute(attr, subject);
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.removeEntitylessAttribute(attr, subject);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getEntitylessAttribute(attr.getName(), subject));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getEntitylessAttribute(attr.getName(), subject));
	}

	@Test
	public void getAttributeDefinitionInNestedTransactionWithClean() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinitionInNestedTransactionWithClean");

		cacheManager.newNestedTransaction();

		AttributeDefinition attr = setUpGroupAttributeDefinition();

		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));

		cacheManager.cleanNestedTransaction();

		try {
			cacheManager.getAttributeDefinition(attr.getName());
			throw new InternalErrorException("attribute definition should not exist after transaction clean");
		} catch (AttributeNotExistsException e) {
			try{
				cacheManager.getAttributeDefinition(attr.getId());
				throw new InternalErrorException("attribute definition should not exist after transaction clean");
			} catch (AttributeNotExistsException ex) {
				//this is ok, attribute definition should not exist
			}
		}
	}

	@Test
	public void getAttributeDefinitionInNestedTransactionWithFlush() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinitionInNestedTransactionWithFlush");

		cacheManager.newNestedTransaction();

		AttributeDefinition attr = setUpGroupAttributeDefinition();

		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));

		cacheManager.flushNestedTransaction();

		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));
	}

	@Test
	public void getAttributeDefinitionWithManyNestedTransactions() throws Exception {
		System.out.println(CLASS_NAME + "getAttributeDefinitionWithManyNestedTransactions");

		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();

		AttributeDefinition attr = setUpGroupAttributeDefinition();

		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));

		cacheManager.newNestedTransaction();
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));

		cacheManager.cleanNestedTransaction();
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getName()));
		assertEquals("returned attribute definition is not same as stored", attr, cacheManager.getAttributeDefinition(attr.getId()));

		cacheManager.cleanNestedTransaction();

		try {
			cacheManager.getAttributeDefinition(attr.getName());
			throw new InternalErrorException("attribute definition should not exist after transaction clean");
		} catch (AttributeNotExistsException e) {
			try{
				cacheManager.getAttributeDefinition(attr.getId());
				throw new InternalErrorException("attribute definition should not exist after transaction clean");
			} catch (AttributeNotExistsException ex) {
				//this is ok, attribute definition should not exist
			}
		}
	}

	@Test
	public void getEntitylessAttrValueInNestedTransactionWithClean() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttrValueInNestedTransactionWithClean");

		cacheManager.newNestedTransaction();

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.cleanNestedTransaction();

		assertEquals("returned attribute value should be null", null, cacheManager.getEntitylessAttrValue(attr.getId(), subject));
	}

	@Test
	public void getEntitylessAttrValueInNestedTransactionWithFlush() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttrValueInNestedTransactionWithFlush");

		cacheManager.newNestedTransaction();

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.flushNestedTransaction();

		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));
	}

	@Test
	public void getEntitylessAttrValueWithManyNestedTransactions() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttrValueWithManyNestedTransactions");

		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();

		Attribute attr = setUpEntitylessAttribute();
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.newNestedTransaction();
		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.cleanNestedTransaction();
		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.cleanNestedTransaction();

		assertEquals("returned attribute value should be null", null, cacheManager.getEntitylessAttrValue(attr.getId(), subject));
	}

	@Test
	public void getEntitylessAttrValueInNestedTransactionWithRemove() throws Exception {
		System.out.println(CLASS_NAME + "getEntitylessAttrValueInNestedTransactionWithRemove");

		AttributeDefinition attributeDefinition = setUpEntitylessAttributeDefinition();

		cacheManager.newNestedTransaction();

		Attribute attr = new Attribute(attributeDefinition);
		attr.setValue("Test");
		cacheManager.setEntitylessAttribute(attr, subject);

		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.removeEntitylessAttribute(attr, subject);
		assertEquals("returned attribute value should be null", null, cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.setEntitylessAttribute(attr, subject);
		assertEquals("returned attribute value is not same as stored", attr.getValue(), cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.removeEntitylessAttribute(attr, subject);
		assertEquals("returned attribute value should be null", null, cacheManager.getEntitylessAttrValue(attr.getId(), subject));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute value should be null", null, cacheManager.getEntitylessAttrValue(attr.getId(), subject));
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Test
	public void getAttributesByNames() throws Exception {
		System.out.println(CLASS_NAME + "getAttributesByNames");

		cacheManager.newNestedTransaction();

		Attribute groupAttr = setUpGroupAttribute();
		AttributeDefinition groupAttrDef = setUpGroupAttributeDefinition();
		cacheManager.setAttribute(groupAttr, groupHolder, null);

		List<String> attributeNames = new ArrayList<>();
		attributeNames.add(groupAttr.getName());
		attributeNames.add(groupAttrDef.getName());

		List<Attribute> attrs = cacheManager.getAttributesByNames(attributeNames, groupHolder, null);
		assertTrue("result should contain group attribute", attrs.contains(groupAttr));
		assertTrue("result should contain group attribute definition", attrs.contains(groupAttrDef));
		assertTrue("it should return 2 attributes", attrs.size() == 2);

		cacheManager.removeAttribute(groupAttr, groupHolder, null);

		attrs = cacheManager.getAttributesByNames(attributeNames, groupHolder, null);
		assertTrue("result should not contain group attribute", !attrs.contains(groupAttr));
		assertTrue("result should contain group attribute definition", attrs.contains(groupAttrDef));
		assertTrue("it should return 2 attribute definitions", attrs.size() == 2);

		cacheManager.flushNestedTransaction();

		attrs = cacheManager.getAttributesByNames(attributeNames, groupHolder, null);
		assertTrue("result should not contain group attribute", !attrs.contains(groupAttr));
		assertTrue("result should contain group attribute definition", attrs.contains(groupAttrDef));
		assertTrue("it should return 2 attribute definitions", attrs.size() == 2);
	}

	@Test
	public void getAttributesDefinitionsByIds() {
		System.out.println(CLASS_NAME + "getAttributesDefinitionsByIds");

		cacheManager.newNestedTransaction();

		AttributeDefinition groupAttrDef = setUpGroupAttributeDefinition();
		List<Integer> attributeDefinitionIds = new ArrayList<>();
		attributeDefinitionIds.add(groupAttrDef.getId());

		List<AttributeDefinition> returnedAttrDefinitions = cacheManager.getAttributesDefinitions(attributeDefinitionIds);

		assertEquals("number of attribute definitions set and returned should be the same", attributeDefinitionIds.size(), returnedAttrDefinitions.size());
		assertTrue("returned list should contain saved attribute definition", returnedAttrDefinitions.contains(groupAttrDef));

		cacheManager.flushNestedTransaction();

		returnedAttrDefinitions = cacheManager.getAttributesDefinitions(attributeDefinitionIds);

		assertEquals("number of attribute definitions set and returned should be the same", attributeDefinitionIds.size(), returnedAttrDefinitions.size());
		assertTrue("returned list should contain saved attribute definition", returnedAttrDefinitions.contains(groupAttrDef));
	}

	@Test
	public void flushAttributeWithRemove() throws Exception {
		System.out.println(CLASS_NAME + "flushAttributeWithRemove");

		AttributeDefinition attributeDefinition = setUpGroupAttributeDefinition();
		Attribute attr = new Attribute(attributeDefinition);
		attr.setValue("Test");
		cacheManager.setAttribute(attr, groupHolder, null);

		cacheManager.newNestedTransaction();
		cacheManager.newNestedTransaction();

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		cacheManager.removeAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
	}

	@Test
	public void manyNestedTransactionsWithRemove() throws Exception {
		System.out.println(CLASS_NAME + "manyNestedTransactionsWithRemove");

		AttributeDefinition attributeDefinition = setUpGroupAttributeDefinition();
		// 1. nested transaction start
		cacheManager.newNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attributeDefinition.getName(), groupHolder, null));

		// 2. nested transaction start
		cacheManager.newNestedTransaction();

		Attribute attr = new Attribute(attributeDefinition);
		attr.setValue("Test");
		cacheManager.setAttribute(attr, groupHolder, null);

		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 3. nested transaction start
		cacheManager.newNestedTransaction();

		cacheManager.removeAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		cacheManager.setAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 3. nested transaction end
		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 3. nested transaction start
		cacheManager.newNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		cacheManager.removeAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 3. nested transaction end
		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 2. nested transaction end
		cacheManager.cleanNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 1. nested transaction end
		cacheManager.cleanNestedTransaction();
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 1. nested transaction start
		cacheManager.newNestedTransaction();

		cacheManager.removeAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attributeDefinition, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		cacheManager.setAttribute(attr, groupHolder, null);
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));

		// 1. nested transaction end
		cacheManager.flushNestedTransaction();
		assertEquals("returned attribute is not same as stored", attr, cacheManager.getAttributeByName(attr.getName(), groupHolder, null));
	}






	// PRIVATE METHODS ----------------------------------------------

	private AttributeDefinition setUpGroupAttributeDefinition() {
		return setUpAttributeDefinitionForCacheManager(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute-definition");
	}

	private AttributeDefinition setUpEntitylessAttributeDefinition() {
		return setUpAttributeDefinitionForCacheManager(AttributesManager.NS_ENTITYLESS_ATTR_DEF, "entityless-test-attribute-definition");
	}

	private Attribute setUpGroupAttribute() {
		return setUpAttributeForCacheManager(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute", "GroupAttribute");
	}

	private Attribute setUpGroupAttributeForAttributesManager() throws InternalErrorException, AttributeDefinitionExistsException {
		return setUpAttributeForAttributesManager(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute", "GroupAttribute");
	}

	private Attribute setUpGroupAttribute1() {
		return setUpAttributeForCacheManager(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute1", "GroupAttribute1");
	}

	private Attribute setUpGroupAttribute1ForAttributesManager() throws InternalErrorException, AttributeDefinitionExistsException {
		return setUpAttributeForAttributesManager(AttributesManager.NS_GROUP_ATTR_OPT, "group-test-attribute1", "GroupAttribute1");
	}

	private Attribute setUpEntitylessAttribute() {
		return setUpAttributeForCacheManager(AttributesManager.NS_ENTITYLESS_ATTR_DEF, "entityless-test-attribute", "EntitylessAttribute");
	}

	private Attribute setUpEntitylessAttributeForAttributesManager() throws Exception {
		return setUpAttributeForAttributesManager(AttributesManager.NS_ENTITYLESS_ATTR_DEF, "entityless-test-attribute", "EntitylessAttribute");
	}

	private AttributeDefinition setUpAttributeDefinitionForAttributesManager(String namespace, String friendlyName) throws AttributeDefinitionExistsException, InternalErrorException {
		AttributeDefinition attributeDefinition = setUpAttributeDefinition(namespace, friendlyName);
		return ((PerunBlImpl)perun).getAttributesManagerImpl().createAttribute(sess, attributeDefinition);
	}

	private AttributeDefinition setUpAttributeDefinitionForCacheManager(String namespace, String friendlyName) {
		AttributeDefinition attributeDefinition = setUpAttributeDefinition(namespace, friendlyName);
		cacheManager.setAttributeDefinition(attributeDefinition);
		return  attributeDefinition;
	}

	private AttributeDefinition setUpAttributeDefinition(String namespace, String friendlyName) {

		AttributeDefinition attr = new Attribute();
		attr.setNamespace(namespace);
		attr.setFriendlyName(friendlyName);
		attr.setType(String.class.getName());
		attr.setId(id);
		id++;

		attr.setCreatedAt(timeCreated);
		attr.setCreatedBy(creator);
		attr.setModifiedAt(timeCreated);
		attr.setModifiedBy(creator);

		return attr;
	}

	private Attribute setUpAttributeForCacheManager(String namespace, String friendlyName, String value) {
		AttributeDefinition attributeDefinition = setUpAttributeDefinitionForCacheManager(namespace, friendlyName);
		return setUpAttribute(attributeDefinition, value);
	}

	private Attribute setUpAttributeForAttributesManager(String namespace, String friendlyName, String value) throws InternalErrorException, AttributeDefinitionExistsException {
		AttributeDefinition attributeDefinition = setUpAttributeDefinitionForAttributesManager(namespace, friendlyName);
		return setUpAttribute(attributeDefinition, value);
	}

	private Attribute setUpAttribute(AttributeDefinition attributeDefinition, String value) {
		Attribute attr = new Attribute(attributeDefinition);

		attr.setValue(value);
		attr.setValueCreatedAt(timeCreated);
		attr.setValueCreatedBy(creator);
		attr.setValueModifiedAt(timeCreated);
		attr.setValueModifiedBy(creator);

		return attr;
	}
}
