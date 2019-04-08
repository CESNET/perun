package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.FacilityAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Permission for directory scratch set in service fs_scratch_local
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_scratchLocalDirPermissions extends FacilityAttributesModuleAbstract implements FacilityAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[01234567]?[01234567]{3}$");

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws WrongAttributeValueException {
		//Null is ok, it means use default permissions in script (probably 0700)
		if(attribute.getValue() == null) return;
		String attrValue = (String) attribute.getValue();
		
		Matcher match = pattern.matcher(attrValue);

		if(!match.matches()) throw new WrongAttributeValueException(attribute, facility, "Bad format of attribute, (expected something like '750' or '0700').");
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setFriendlyName("scratchLocalDirPermissions");
		attr.setDisplayName("Unix permissions for scratch local");
		attr.setType(String.class.getName());
		attr.setDescription("Unix permissions, which will be applied when new scratch folder is created.");
		return attr;
	}
}
