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
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Group structure synchronization
 *
 * true if structure synchronization is enabled
 * false if not
 * empty if there is no setting
 *
 * @author Erik Horváth <horvatherik3@gmail.com>
 */
public class urn_perun_group_attribute_def_def_groupStructureSynchronizationEnabled extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {
	private static final Logger log = LoggerFactory.getLogger(urn_perun_group_attribute_def_def_groupStructureSynchronizationEnabled.class);

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//Null value is ok, means no settings for group
		if(attribute.getValue() == null) return;

		if (attribute.valueAsBoolean()) {

			if (perunSession.getPerunBl().getGroupsManagerBl().isGroupSynchronizedFromExternallSource(perunSession, group)) {
				throw new InternalErrorException("Synchronization is already enabled for one of the parent groups.");
			}

			try {
				Attribute attrSynchronizeEnabled = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, group, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME);
				if (Objects.equals("true", attrSynchronizeEnabled.getValue())) {
					throw new WrongReferenceAttributeValueException(attribute, attrSynchronizeEnabled);
				}

				Attribute requiredAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, group, GroupsManager.GROUP_STRUCTURE_SYNCHRO_INTERVAL_ATTRNAME);
				if (requiredAttribute.getValue() == null) {
					throw new WrongReferenceAttributeValueException(attribute, requiredAttribute, requiredAttribute.toString() + " must be set in order to enable synchronization.");
				}

				requiredAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, group, GroupsManager.GROUPSQUERY_ATTRNAME);
				if (requiredAttribute.getValue() == null) {
					throw new WrongReferenceAttributeValueException(attribute, requiredAttribute, requiredAttribute.toString() + " must be set in order to enable synchronization.");
				}

				requiredAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, group, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
				if (requiredAttribute.getValue() == null) {
					throw new WrongReferenceAttributeValueException(attribute, requiredAttribute, requiredAttribute.toString() + " must be set in order to enable synchronization.");
				}

				requiredAttribute = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
				if (requiredAttribute.getValue() == null) {
					throw new WrongReferenceAttributeValueException(attribute, requiredAttribute, requiredAttribute.toString() + " must be set in order to enable synchronization.");
				}
			} catch (AttributeNotExistsException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		if (attribute.getValue() == null) {
			try {
				AttributesManagerBl attributesManager = session.getPerunBl().getAttributesManagerBl();
				AttributeDefinition removeAttrDef = attributesManager.getAttributeDefinition(session, new urn_perun_group_attribute_def_def_flatGroupStructureEnabled().getAttributeDefinition().getName());
				attributesManager.removeAttributeWithoutCheck(session, group, removeAttrDef);
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				log.debug("Attribute for flat group structure synchronization does not exist", ex);
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("groupStructureSynchronizationEnabled");
		attr.setDisplayName("Group Structure Synchronization Enabled");
		attr.setType(Boolean.class.getName());
		attr.setDescription("Enables group structure synchronization from external source.");
		return attr;
	}
}
