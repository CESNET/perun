package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Role;
import java.util.List;

/**
 * This interface serves as a template for defined common properties
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * $Id$
 */
public interface AttributesModuleImplApi {
    
    /**
     * Return all modules which are needed to correct check.
     * 
     * @return list of modules we need to correct check
     */
    List<String> getDependencies();
    
    /**
     * Return all modules which are needed to correct check (for strong dependencies do another step at initializing)
     * 
     * @return list of modules we need to correct check
     */
    List<String> getStrongDependencies();
    
    /**
     *  Return list of roles, which are authorized to work with this module
     * 
     * @return list of roles
     */
    List<Role> getAuthorizedRoles();
    
    /**
     * Return attributes definition which is represented by the module
     * 
     * @return attribute definition
     * 
     */
    AttributeDefinition getAttributeDefinition();
}
