package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * UsedGids
 * 
 * Contains all used and already depleted Gids.
 * 
 * Available Formats: 
 * "Gx" -> "y"  means Group with ID x using GID y
 * "Rx" -> "y"  means Resource with ID x using GID y
 * "Dy" -> "y"  means depleted GID y (no group or resource using it now)
 * No other formats are available.
 * 
 * Null in value of this attribute means there is no GID used or depleted.
 *
 * IMPORTANT: be very careful about removing values from this attribute, information can be lost forever!
 *
 * @author Michal Stava &lt;stavamichal@gmaillcom&gt;
 */
public class urn_perun_entityless_attribute_def_def_usedGids extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {
	
	private static Pattern keyPattern = Pattern.compile("^[RGD][1-9][0-9]*$");
	private static Pattern valuePattern = Pattern.compile("^[1-9][0-9]*$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//If this attribute value is null, it means there is no GIDS depleted or used.
		if(attribute.getValue() == null) return;
		
		Map<String, String> map = (Map<String, String>) attribute.getValue();
		//If there are no keys in map, it means the same like null value
		if(map.isEmpty()) return;
		
		Set<String> mapKeys = map.keySet();
		for(String mapKey: mapKeys) {
			//Test key
			if(mapKey == null) throw new WrongAttributeValueException(attribute, key, "Key in usedGids can't be null.");
			Matcher keyMatcher = keyPattern.matcher(mapKey);
			if(!keyMatcher.matches()) throw new WrongAttributeValueException(attribute, key, "Key in usedGids can be only in format 'Rx', 'Gx', 'Dx' where 'x' is positive integer.");
			
			//Test value
			String value = map.get(mapKey);
			if(value == null) throw new WrongAttributeValueException(attribute, key, "Value in usedGids can't be null.");
			Matcher valueMatcher = valuePattern.matcher(value);
			if(!valueMatcher.matches()) throw new WrongAttributeValueException(attribute, key, "Key in usedGids can be only positive integer.");
		}
		
		//If group or resource has some gid, this gid can't be depleted at the same time!
		for(String mapKey: mapKeys) {
			//We have to skip keys in usedGids which start with "D",
			//usedGids always contains key "D" + value when value has key starting with "D"
			if(mapKey.startsWith("D")) continue;
			String value = map.get(mapKey);
			if(map.containsKey("D" + value)) throw new WrongAttributeValueException(attribute, key, "This gid can't be depleted and used at the same time!");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("usedGids");
		attr.setDisplayName("Used and depleted gids.");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Contains all used and depleted Gids. Depleted means - used, but not using now.");
		return attr;
	}
}
