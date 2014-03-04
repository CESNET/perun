/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceGroupAttributesModuleImplApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module for project directory permissions
 * 
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 25.2.2014
 */
public class urn_perun_group_resource_attribute_def_def_projectDirPermissions extends ResourceGroupAttributesModuleAbstract implements ResourceGroupAttributesModuleImplApi {
    
    public void checkAttributeValue(PerunSessionImpl sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
      Integer permissions = (Integer) attribute.getValue();
      //Permissions can be null (if null, it means DEFAULT 750)
      if (permissions == null) return;
      
      String perm = permissions.toString();
       
      //Only 3 consecutive numbers with value >=0 and <=7 are allowed
      Pattern pattern = Pattern.compile("^[01234567]{3}$");
      Matcher match = pattern.matcher(perm);

      if (!match.matches()) {
        throw new WrongAttributeValueException(attribute, "Bad format of attribute projectDirPermissions (expected something like '750').");    
      }
    }
    
    public AttributeDefinition getAttributeDefinition() {
      AttributeDefinition attr = new AttributeDefinition();
      attr.setNamespace(AttributesManager.NS_GROUP_RESOURCE_ATTR_DEF);
      attr.setFriendlyName("projectDirPermissions");
      attr.setType(Integer.class.getName());
      attr.setDescription("Permissions (ACL) to directory, where the project exists.");
      return attr;
    }
}
