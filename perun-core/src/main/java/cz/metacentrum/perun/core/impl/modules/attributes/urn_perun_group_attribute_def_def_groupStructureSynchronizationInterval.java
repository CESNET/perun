package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

public class urn_perun_group_attribute_def_def_groupStructureSynchronizationInterval extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		try {
			Attribute foundAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME);
			if(foundAttribute.getValue() != null) {
				throw new WrongReferenceAttributeValueException(attribute, foundAttribute, group, null, group, null, "Attribute " + attribute.getName() + " cannot be set because attribute " + GroupsManager.GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME + " is already set.");
			}
		} catch (AttributeNotExistsException exc) {
			throw new ConsistencyErrorException("Attribute " + GroupsManager.GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME + " is supposed to exist", exc);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(GroupsManager.GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("groupStructureSynchronizationInterval");
		attr.setDisplayName("Group structure synchronization interval");
		attr.setType(String.class.getName());
		attr.setDescription("Time between two successful group structure synchronizations.");
		return attr;
	}
}
