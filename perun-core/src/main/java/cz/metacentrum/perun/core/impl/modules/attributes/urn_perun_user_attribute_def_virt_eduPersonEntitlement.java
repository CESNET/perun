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
import java.util.HashSet;
import java.util.Set;

/**
 * All entitlements collected from:
 *  - UserExtSources attributes
 *  - urn:perun:user:attribute-def:virt:groupNames
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_eduPersonEntitlement extends UserVirtualAttributeCollectedFromUserExtSource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public String getSourceAttributeFriendlyName() {
		return "entitlement";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "eduPersonEntitlement";
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) {
		//get already filled value obtained from UserExtSources
		Attribute attribute = super.getAttributeValue(sess, user, destinationAttributeDefinition);

		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
		//get values previously obtained and add them to Set representing final value
		//for values use set because of avoiding duplicities
		Set<String> valuesWithoutDuplicities = new HashSet<>(attribute.valueAsList());

		//convert set to list (values in list will be without duplicities)
		destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicities));
		return destinationAttribute;
	}

}
