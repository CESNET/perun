/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Papperwing
 */
public class urn_perun_user_facility_attribute_def_virt_shell extends FacilityUserVirtualAttributesModuleAbstract implements FacilityUserVirtualAttributesModuleImplApi {

    /**
     * Gets the value of the attribute
     */
    public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
        Attribute attr = new Attribute(attributeDefinition);
        try {
            Attribute a = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":shell");
            if (a.getValue() != null) {
                attr.setValue(a.getValue());
                return attr;
            }
        } catch (WrongAttributeAssignmentException ex) {
            throw new InternalErrorException(ex);
        } catch (AttributeNotExistsException ex) {
            throw new InternalErrorException(ex);
        }

        try {
            List<String> facilityShells = (List<String>) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":shells").getValue();
            List<String> userPrefferedShells = ((List<String>) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":prefferedShells"));
            List<Resource> resources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
            Set<String> resourcesShells = new HashSet<String>();
            
            for (Resource resource : resources) {
                resourcesShells.addAll((List<String>) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells"));
            }
            if (userPrefferedShells != null)
            for (String pShell : userPrefferedShells) {
                if (resourcesShells.contains(pShell)) {
                    attr.setValue(pShell);
                    return attr;
                }
            }
            if (facilityShells != null)
            for (String fShell : facilityShells) {
                if (resourcesShells.contains(fShell)) {
                    attr.setValue(fShell);
                    return attr;
                }
            }




        } catch (AttributeNotExistsException ex) {
            throw new InternalErrorException(ex);
        } catch (WrongAttributeAssignmentException ex) {
            throw new InternalErrorException(ex);
        }
        attr = new Attribute(attributeDefinition);
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
        List<String> StrongDependencies = new ArrayList<String>();
        StrongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":shell");
        StrongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":shells");
        StrongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":prefferedShells");
        StrongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells");
        return StrongDependencies;
    }

    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
        attr.setFriendlyName("shell");
        attr.setType(String.class.getName());
        attr.setDescription("Computed Shell from user preferences");
        return attr;
    }
}
