package cz.metacentrum.perun.core.impl.modules.attributes;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * All affiliations collected from:
 * 		ues:def:voPersonExternalAffiliation
 *
 * @author Michal Berky
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_forwardedVoPersonExternalAffiliation extends UserVirtualAttributeCollectedFromUserExtSource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Pattern userAllAttrsRemovedPattern = Pattern.compile("All attributes removed for User:\\[(.*)]", Pattern.DOTALL);

	// format has to match the format in Perun-wui setAffiliation miniapp (method createAssignedAffiliationsAttribute)
	private final String VALIDITY_DATE_FORMAT = "yyyy-MM-dd";

	@Override
	public String getSourceAttributeFriendlyName() {
		return "voPersonExternalAffiliation";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "forwardedVoPersonExternalAffiliation";
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
