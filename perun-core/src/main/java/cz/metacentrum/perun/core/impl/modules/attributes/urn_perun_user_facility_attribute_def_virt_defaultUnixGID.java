package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleImplApi;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_virt_defaultUnixGID extends FacilityUserVirtualAttributesModuleAbstract implements FacilityUserVirtualAttributesModuleImplApi {
    
    public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException{
        Attribute attr = new Attribute(attributeDefinition);
        try {
            Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":shell");
            if (attribute.getValue() != null) {
                Utils.copyAttributeToVirtualAttributeWithValue(attribute, attr);
                return attr;
            }
        } catch (WrongAttributeAssignmentException ex) {
            throw new InternalErrorException(ex);
        } catch (AttributeNotExistsException ex) {
            throw new InternalErrorException(ex);
        }

        try {
            Attribute facilityUGIDs = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":GID namespace");
            Attribute userPrefferedUGIDs = (sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":preferredUnixGIDs"));
            List<Resource> resources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
            Set<String> resourcesUGIDs = new HashSet<String>();
            
            for (Resource resource : resources) {
                List<String> resourcesShellsForTest = (List<String>) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells").getValue();
                if (resourcesShellsForTest != null) resourcesUGIDs.addAll(resourcesShellsForTest);
            }
            
            if (userPrefferedUGIDs.getValue() != null){
                for (String pUGID : (List<String>)userPrefferedUGIDs.getValue()) {
                    if (resourcesUGIDs.contains(pUGID)) {
                        Utils.copyAttributeToVirtualAttributeWithValue(userPrefferedUGIDs, attr);
                        return attr;
                    }
                }
            }
            if (facilityUGIDs.getValue() != null){
                for (String fUGID : (List<String>)facilityUGIDs.getValue()) {
                    if (resourcesUGIDs.contains(fUGID)) {
                        Utils.copyAttributeToVirtualAttributeWithValue(facilityUGIDs, attr);
                        return attr;
                    }
                }
            }

        } catch (AttributeNotExistsException ex) {
            throw new InternalErrorException(ex);
        } catch (WrongAttributeAssignmentException ex) {
            throw new InternalErrorException(ex);
        }
        
        
        return attr;
    }
    
}
