package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_group_attribute_def_def_flatGroupStructureEnabledTest {

	private urn_perun_group_attribute_def_def_flatGroupStructureEnabled classInstance;
	private Attribute attributeToCheck;
	private Group group = new Group(1,"group1","Group 1",null,null,null,null,0,0);
	private AttributesManagerBl attributesManagerBl;
	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		classInstance = new urn_perun_group_attribute_def_def_flatGroupStructureEnabled();
		attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);

		attributesManagerBl = mock(AttributesManagerBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);
		when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
	}

	@Test(expected = WrongReferenceAttributeValueException.class)
	public void testCheckWithoutMandatoryAttributeSemantics() throws Exception {
		System.out.println("testCheckWithoutMandatoryAttributeSemantics()");
		when(attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME)).thenReturn(null);
		attributeToCheck.setValue(true);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}

	@Test
	public void testCorrectSemantics() throws Exception {
		System.out.println("testCorrectSemantics()");
		when(attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME)).thenReturn(attributeToCheck);
		attributeToCheck.setValue(true);

		classInstance.checkAttributeSemantics(sess, group, attributeToCheck);
	}
}
