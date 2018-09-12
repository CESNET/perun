package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * All affiliations collected from UserExtSources attributes and eduPersonScopedAffiliationsManuallyAssigned.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("unused")
public class urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations extends UserVirtualAttributeCollectedFromUserExtSource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());


	private final Pattern userAllAttrsRemovedPattern = Pattern.compile("All attributes removed for User:\\[(.|\\s)*]", Pattern.MULTILINE);
	private final Pattern userEPSAMASetPattern = Pattern.compile("Attribute:\\[(.|\\s)*friendlyName=<" + getSecondarySourceAttributeFriendlyName() +">(.|\\s)*] set for User:\\[(.|\\s)*]", Pattern.MULTILINE);
	private final Pattern userEPSAMARemovePattern = Pattern.compile("AttributeDefinition:\\[(.|\\s)*friendlyName=<" + getSecondarySourceAttributeFriendlyName() + ">(.|\\s)*] removed for User:\\[(.|\\s)*]", Pattern.MULTILINE);

	@Override
	public String getSourceAttributeFriendlyName() {
		return "affiliation";
	}

	/**
	 * Get friendly name of secondary source attribute
	 * @return friendly name of secondary source attribute
	 */
	public String getSecondarySourceAttributeFriendlyName() {
		return "eduPersonScopedAffiliationsManuallyAssigned";
	}

	/**
	 * Get name of secondary source attribute
	 * @return name of secondary source attribute
	 */
	public String getSecondarySourceAttributeName() {
		return AttributesManager.NS_USER_ATTR_DEF + ":" + getSecondarySourceAttributeFriendlyName();
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "eduPersonScopedAffiliations";
	}

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
		//get already filled value obtained from UserExtSources
		Attribute attribute = super.getAttributeValue(sess, user, destinationAttributeDefinition);

		Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
		//get values previously obtained and add them to Set representing final value
		//for values use set because of avoiding duplicities
		Set<String> valuesWithoutDuplicities = new HashSet<>(attribute.valueAsList());

		Attribute manualEPSAAttr = null;
		try {
			//get value from urn:perun:user:attribute-def:def:eduPersonScopedAffiliationsManuallyAssigned
			manualEPSAAttr = sess.getPerunBl().getAttributesManagerBl()
					.getAttribute(sess, user, getSecondarySourceAttributeName());
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Wrong assignment of " + getSecondarySourceAttributeFriendlyName() + " for user " + user.getId(), e);
		} catch (AttributeNotExistsException e) {
			log.debug("Attribute " + getSecondarySourceAttributeFriendlyName() + " of user " + user.getId() + "does not exist, values will be skipped", e);
		}

		if (manualEPSAAttr != null) {
			Map<String, String> value = manualEPSAAttr.valueAsMap();
			if (value != null) {

				LocalDate now = LocalDate.now();
				// format has to match the format in Perun-wui setAffiliation miniapp
				// (method createAssignedAffiliationsAttribute)
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				for (Map.Entry<String, String> entry: value.entrySet()) {
					LocalDate expiration = LocalDate.parse(entry.getValue(), dateFormat);

					if (! now.isAfter(expiration)) {
						valuesWithoutDuplicities.add(entry.getKey());
					}
				}
			}
		}

		//convert set to list (values in list will be without duplicities)
		destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicities));
		return destinationAttribute;
	}

	@Override
	public List<Pattern> getPatternsForMatch() {
		List<Pattern> patterns = super.getPatternsForMatch();
		patterns.add(userAllAttrsRemovedPattern);
		patterns.add(userEPSAMARemovePattern);
		patterns.add(userEPSAMASetPattern);

		return patterns;
	}
}
