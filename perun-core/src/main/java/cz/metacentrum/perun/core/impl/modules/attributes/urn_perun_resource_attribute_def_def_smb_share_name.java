package cz.metacentrum.perun.core.impl.modules.attributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

/**
 *
 * @author Milan Halenar <255818@mail.muni.cz>
 */
public class urn_perun_resource_attribute_def_def_smb_share_name extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

    public Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
       /*return new Attribute(attribute);
       //TODO*/
        return new Attribute();
    }

    public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
        /*String share_name = (String) attribute.getValue();
        Pattern pattern = Pattern.compile("^([-_a-z0-9]+)$");
        Matcher match = pattern.matcher(share_name);

        if (share_name.length() > 15 || !match.matches()) {
            throw new WrongAttributeValueException(attribute, "Bad smb_share_name attribute format");
        }
        Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);
        AttributesManagerBl attrMan = perunSession.getPerunBl().getAttributesManagerBl();
        for (Resource r : perunSession.getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility)) {
            // if(r.equals(resource)) continue; //skip self
            try {
                if (attrMan.getAttribute(perunSession, r, attribute.getName()).equals(attribute)) {
                    if (r.equals(resource)) continue; //skip self
                    throw new WrongAttributeValueException(attribute, "smb_share_name attribute value is already used on resource " + r.getName());
                }

                for (Group g : perunSession.getPerunBl().getResourcesManagerBl().getAssignedGroups(perunSession, r, true)) {
                    if (attrMan.getAttribute(perunSession, r, g, attribute.getName()).equals(attribute)) {
                         throw new WrongAttributeValueException(attribute, "smb_share_name attribute value is already used on resource_group " + r.getName() + g.getName());
                    }
                }
            } catch (AttributeNotExistsException ex) {
                throw new InternalErrorException(ex);
            }
        }*/
    }
    
    public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
      attr.setFriendlyName("smb_share_name");
      attr.setType(String.class.getName());
      attr.setDescription("Name of Samba share represented by resource.");
      return attr;
    }
}
