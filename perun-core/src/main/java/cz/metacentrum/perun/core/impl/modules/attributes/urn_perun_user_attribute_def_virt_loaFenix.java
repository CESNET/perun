package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

/**
 * All loaFenix values collected from UserExtSources attributes are collected
 * and the lowest value is then returned as the result.
 *
 * @author Petr Vsetecka <vsetecka@cesnet.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_loaFenix extends UserVirtualAttributeCollectedFromUserExtSource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String getSourceAttributeFriendlyName() {
		return "loaFenix";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "loaFenix";
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) {
		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);

		// get already filled value obtained from UserExtSources
		// IMPORTANT: value get from super method is type of ArrayList (destination attribute has String type), this situation is known and resolved below in code
		Attribute attribute = super.getAttributeValue(sess, user, destinationAttributeDefinition);

		if (attribute.valueAsList().isEmpty()) {
			// there are no loaFenix values, return default null
			destinationAttribute.setValue(null);
			return destinationAttribute;
		}

		// get values previously obtained and add them to ArrayList for easy ordering
		ArrayList<String> valueList = attribute.valueAsList();

		// order the ArrayList in ascending order
		Collections.sort(valueList);

		// use the first element as it will be lowest number (highest Fenix value)
		destinationAttribute.setValue(valueList.get(0));
		return destinationAttribute;
	}

}
