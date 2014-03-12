package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
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

    public Attribute getAttributeValue(PerunSessionImpl sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
        Attribute attr = new Attribute(attributeDefinition);
        try {
            //first phaze: if attribute UF:D:defaultUnixGID is set, it has top priority
            Attribute attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
            if (attribute.getValue() != null) {
                Utils.copyAttributeToVirtualAttributeWithValue(attribute, attr);
                return attr;
            }
            //second phase: UF:D:defaultUnixGID is not set, module will select unix GID from preffered list
            String namespace = (String) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace").getValue();
            if (namespace == null) {
                return attr;
            }

            Attribute userPrefferedUGIDs = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredDefaultUnixGIDs-namespace:" + namespace);
            List<Resource> resources = sess.getPerunBl().getUsersManagerBl().getAllowedResources(sess, facility, user);
            if (userPrefferedUGIDs.getValue() != null) {
                Set<Integer> resourcesUGIDs = new HashSet<>();
                for (Resource resource : resources) {
                    Integer gidForTest = (Integer) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace).getValue();
                    if (gidForTest != null) {
                        resourcesUGIDs.add(gidForTest);
                    }
                }

                List<Member> userMembers = sess.getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
                Set<Integer> groupsUGIDs = new HashSet<Integer>();
                for (Resource resource : resources) {
                    List<Group> groupsFromResource = sess.getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource);
                    for (Group group : groupsFromResource) {
                        List<Member> groupMembers = sess.getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
                        groupMembers.retainAll(userMembers);
                        if (!groupMembers.isEmpty()) {
                            Integer gidForTest = (Integer) sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace:" + namespace).getValue();
                            if (gidForTest != null) {
                                groupsUGIDs.add(gidForTest);
                            }
                        }
                    }
                }

                for (String pUGID : (List<String>) userPrefferedUGIDs.getValue()) {
                    Integer nUGID = Integer.valueOf(pUGID);
                    if (resourcesUGIDs.contains(nUGID) || groupsUGIDs.contains(nUGID)) {
                        Utils.copyAttributeToViAttributeWithoutValue(userPrefferedUGIDs, attr);
                        attr.setValue(nUGID);
                        return attr;
                    }
                }
            }
            //third phase: Preffered unix GID is not on the facility and it's choosed from resource with minimal id and has set unix GID
            Resource resourceWithMinId = null;
            for(Resource resource: resources) {
               Attribute uGIDAttrForResource = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace);
               if(uGIDAttrForResource.getValue() != null) {
                   if(resourceWithMinId == null || (resource.getId() < resourceWithMinId.getId())) resourceWithMinId = resource;
               }
            }
           
            if(resourceWithMinId != null) {
               Attribute uGIDAttrForResource = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceWithMinId, AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace);
               Utils.copyAttributeToVirtualAttributeWithValue(uGIDAttrForResource, attr);
               return attr;
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
            Attribute attributeToSet = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
            return sess.getPerunBl().getAttributesManagerBl().setAttributeWithoutCheck(sess, facility, user, attributeToSet);


        } catch (WrongAttributeAssignmentException ex) {
            throw new ConsistencyErrorException(ex);
        } catch (WrongAttributeValueException ex) {
            throw new InternalErrorException(ex);
        } catch (AttributeNotExistsException ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<String> getStrongDependencies() {
        List<String> strongDependencies = new ArrayList<String>();
        strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID");
        strongDependencies.add(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace");
        strongDependencies.add(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":preferredDefaultUnixGIDs-namespace:*");
        strongDependencies.add(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:*");
        return strongDependencies;
    }

    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
        attr.setFriendlyName("defaultUnixGID");
        attr.setType(String.class.getName());
        attr.setDescription("Computed unix group id from user preferrences");
        return attr;
    }
}
