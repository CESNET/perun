package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attribute represents timezone of the user's location.
 * 
 * @author Jiří Mauritz
 * @version $Id$
 */
public class urn_perun_user_attribute_def_def_timezone extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {
    
    private static final String resourcesPath = "src/main/resources/";
    
    @Override
    public void checkAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
        if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "Timezone must be set");
        if (!(attribute.getValue() instanceof String)) throw new WrongAttributeValueException(attribute, user, "Attribute value (timezone) is not String type.");
        
        String attributeValue = (String) attribute.getValue();
        boolean match = false;
        try {
            match = Utils.patternIsInFile(attributeValue, resourcesPath + "timezones.txt");
        } catch (FileNotFoundException ex) {
            throw new InternalErrorException("File with timezones not found. Should be in " + resourcesPath, ex);
        } catch (IOException ex) {
            throw new InternalErrorException("Unexpected IO exception when parsing timzone.", ex);
        }
        
        if (!(match)) throw new WrongAttributeValueException(attribute, user, "Timezone is not in correct form.");
    }

    @Override
    public AttributeDefinition getAttributeDefinition() {
        AttributeDefinition attr = new AttributeDefinition();
        attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
        attr.setFriendlyName("timezone");
        attr.setType(String.class.getName());
        attr.setDescription("User's timezone described by ±[hh] (ISO 8601).");
        return attr;
    }
    
}
