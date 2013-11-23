
package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityUserVirtualAttributesModuleImplApi;
import java.util.ArrayList;
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
            Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
            if (attribute.getValue() != null) {
                Utils.copyAttributeToVirtualAttributeWithValue(attribute, attr);
                return attr;
            }
            
            Attribute facilityUGIDs = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
            Attribute userPrefferedUGIDs = (sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":preferredUnixGIDs"));
            List<Resource> resources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
            String namespace = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace").getValue();
            Set<Integer> resourcesUGIDs = new HashSet<>();
            
            for (Resource resource : resources) {
                List<Integer> resourcesShellsForTest = (List<Integer>) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace).getValue();
                if (resourcesShellsForTest != null) resourcesUGIDs.addAll(resourcesShellsForTest);
            }
            
            if (userPrefferedUGIDs.getValue() != null){
                for (Integer pUGID : (List<Integer>)userPrefferedUGIDs.getValue()) {
                    if (resourcesUGIDs.contains(pUGID)) {
                        Utils.copyAttributeToVirtualAttributeWithValue(userPrefferedUGIDs, attr);
                        return attr;
                    }
                }
            }
            if (facilityUGIDs.getValue() != null){
                for (Integer fUGID : (List<Integer>)facilityUGIDs.getValue()) {
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
    
    public boolean setAttributeValue(PerunSessionImpl sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
        try {
            return  sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, facility, user, attribute);
        } catch (WrongAttributeAssignmentException ex) {
            throw new ConsistencyErrorException(ex);
        } catch (WrongAttributeValueException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<String> getStrongDependencies() {
        List<String> strongDependencies = new ArrayList<String>();
        strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
        strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
        strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":preferredUnixGIDs");
        strongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:*");
        strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
        return strongDependencies;
    }

    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
        attr.setFriendlyName("defaultUnixGID");
        attr.setType(String.class.getName());
        attr.setDescription("Computed UGID from user preferrences");
        return attr;
    }
}

