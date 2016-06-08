package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceMemberVirtualAttributesModuleImplApi;

/**
 * Module for getting information if member is banned on resource.
 *
 * Get true if exists ban of member on resource or his user on facility.
 *
 * @author Michal Stava
 */
public class urn_perun_member_resource_attribute_def_virt_isBanned extends ResourceMemberVirtualAttributesModuleAbstract implements ResourceMemberVirtualAttributesModuleImplApi {

    public Attribute getAttributeValue(PerunSessionImpl sess, Resource resource, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException {
        Attribute attribute = new Attribute(attributeDefinition);
		//Default value is false
		attribute.setValue(false);

		Facility facility;
		try {
			facility = sess.getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());
		} catch (FacilityNotExistsException ex) {
			throw new InternalErrorException("Facility for Resource " + resource + " not exists!");
		}

		User user;
		try {
			user = sess.getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
		} catch (UserNotExistsException ex) {
			throw new InternalErrorException("User for Member " + member + " not exists!");
		}

		//Ban on resource exists?
		if(sess.getPerunBl().getResourcesManagerBl().banExists(sess, member.getId(), resource.getId())) attribute.setValue(true);
		//Ban on facility exists?
        if(sess.getPerunBl().getFacilitiesManagerBl().banExists(sess, user.getId(), facility.getId())) attribute.setValue(true);
        
        return attribute;

    }

    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
        attr.setFriendlyName("isBanned");
        attr.setDisplayName("Is banned on Resource or Facility");
        attr.setType(Boolean.class.getName());
        attr.setDescription("True if member is banned on resource or all facility.");
        return attr;
    }



}
