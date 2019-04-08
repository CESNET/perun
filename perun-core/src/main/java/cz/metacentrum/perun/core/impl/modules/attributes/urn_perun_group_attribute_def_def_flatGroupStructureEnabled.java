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
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Flat group structure synchronization
 *
 * true if flat structure synchronization is enabled
 * false if not
 * empty if there is no setting
 *
 * @author Erik Horváth <horvatherik3@gmail.com>
 */
public class urn_perun_group_attribute_def_def_flatGroupStructureEnabled extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {
	private static final String MANDATORY_ATTRIBUTE_NAME = GroupsManager.GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME;

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		AttributesManagerBl attributeManager = perunSession.getPerunBl().getAttributesManagerBl();
		try {
			Attribute foundAttribute = attributeManager.getAttribute(perunSession, group, MANDATORY_ATTRIBUTE_NAME);
			if(foundAttribute == null || foundAttribute.getValue() == null) {
				throw new WrongAttributeAssignmentException("Attribute " + MANDATORY_ATTRIBUTE_NAME + " must be defined first.");
			}
		} catch (AttributeNotExistsException exc) {
			throw new ConsistencyErrorException("Attribute " + MANDATORY_ATTRIBUTE_NAME + " is supposed to exist", exc);
		}
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(MANDATORY_ATTRIBUTE_NAME);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("flatGroupStructureEnabled");
		attr.setDisplayName("Flat Group Structure Synchronization Enabled");
		attr.setType(Boolean.class.getName());
		attr.setDescription("Enables flat group structure synchronization from external source.");
		return attr;
	}
}