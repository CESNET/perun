package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DnsMapCtx extends UserVirtualAttributeCollectedFromUserExtSource.ModifyValueContext {
	private Map<String, String> dnsMap;

	DnsMapCtx(PerunSessionImpl sess, User user, AttributeDefinition ad, Map<String, String> dnsMap) {
		super(sess, user, ad);
		this.dnsMap = dnsMap;
	}

	Map<String, String> getDnsMap() {
		return dnsMap;
	}
}

/**
 * Virtual user's attribute for converting schacHomeOrganization names to country names.
 * Using help of entityless attribute dnsStateMapping for conversion.
 * Using the greedy approach -> always taking the longest match from the available ones.
 * Returns only non-empty values.
 * <p>
 * <BR>Examples:
 * <BR>muni.cz -> Czech Republic
 * <BR>oxford.ac.uk -> United Kingdom
 * <BR>.ebi.ac.uk -> null
 *
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class urn_perun_user_attribute_def_virt_institutionsCountries extends UserVirtualAttributeCollectedFromUserExtSource<DnsMapCtx> {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_institutionsCountries.class);
	private final static AttributeDefinition DNS_STATE_MAPPING_ATTR = new urn_perun_entityless_attribute_def_def_dnsStateMapping().getAttributeDefinition();
	private final static Pattern AUDIT_MESSAGE_SET_DNS_DOMAIN_COUNTRY = Pattern.compile("Attribute:\\[(.|\\s)*" + DNS_STATE_MAPPING_ATTR.getFriendlyName() + "(.|\\s)*] set for (.|\\s)*\\.", Pattern.MULTILINE);
	private final static Pattern AUDIT_MESSAGE_REMOVED_DNS_DOMAIN_COUNTRY = Pattern.compile("AttributeDefinition:\\[(.|\\s)*" + DNS_STATE_MAPPING_ATTR.getFriendlyName() + "(.|\\s)*] removed for (.|\\s)*\\.", Pattern.MULTILINE);

	@Override
	public String getSourceAttributeFriendlyName() {
		return "schacHomeOrganization";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "institutionsCountries";
	}

	@Override
	public String getDestinationAttributeDescription() {
		return "Country names associated with user.";
	}


	/**
	 * Loads DNS-to-country translation map from attribute urn:perun:entityless:attribute-def:def:dnsStateMaping.
	 * DNS domains are in keys, country names are in values of the attribute.
	 */
	@Override
	protected DnsMapCtx initModifyValueContext(PerunSessionImpl sess, User user, AttributeDefinition attr) throws InternalErrorException {
		try {
			return new DnsMapCtx(sess, user, attr, sess.getPerunBl().getAttributesManagerBl().getEntitylessStringAttributeMapping(sess, DNS_STATE_MAPPING_ATTR.getName()));
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException("cannot read dnsStateMappings", e);
		}
	}

	/**
	 * Replaces DNS domain with country name, or null.
	 */
	@Override
	public String modifyValue(DnsMapCtx ctx, String value) {
		Map<String, String> dnsMap = ctx.getDnsMap();
		//find the longest matching key
		int matchLength = 0;
		String match = null;
		for (String mapkey : dnsMap.keySet()) {
			if (value.endsWith(mapkey) && mapkey.length() > matchLength) {
				matchLength = mapkey.length();
				match = mapkey;
			}
		}
		//not found, return null to skip the value
		if (match == null) return null;
		//for empty country names, return null to skip the value
		String country = dnsMap.get(match);
		return "".equals(country) ? null : country;
	}


	/**
	 * For a change in dnsStateMapping attribute, finds all affected users and generates audit message about changing this attribute for each of them.
	 */
	@Override
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl sess, String message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<String> messages = super.resolveVirtualAttributeValueChange(sess, message);
		// react to auditlog message about changing dnsStateMapping
		String key = null;
		Matcher setMatcher = AUDIT_MESSAGE_SET_DNS_DOMAIN_COUNTRY.matcher(message);
		if(setMatcher.matches()) {
			key = setMatcher.group(3);
		} else {
			Matcher delMatcher = AUDIT_MESSAGE_REMOVED_DNS_DOMAIN_COUNTRY.matcher(message);
			if(delMatcher.matches()) {
				key = delMatcher.group(3);
			}
		}
		if (key!=null) {
			//find longer matching domains, e.g. ics.muni.cz and muni.cz for .cz
			List<String> longerDomains =  new ArrayList<>();
			AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
			for(String dnsDomain : am.getEntitylessStringAttributeMapping(sess, DNS_STATE_MAPPING_ATTR.getName()).keySet()) {
				if(dnsDomain.endsWith(key) && dnsDomain.length()>key.length()) {
					longerDomains.add(dnsDomain);
				}
			}
			log.debug("DNS domain '{}' changed, found longer domains: {}",key, longerDomains);
			//find users that are affected by the change - have schacHomeOrganization value ending in key, but not ending with longerDomains
			List<User> affectedUsers = sess.getPerunBl().getUsersManagerBl().findUsersWithExtSourceAttributeValueEnding(sess,getSourceAttributeName(),key,longerDomains);
			for (User user : affectedUsers) {
				Attribute thisAtribute = am.getAttribute(sess, user, getDestinationAttributeName());
				messages.add(thisAtribute.serializeToString() + " set for " + user.serializeToString() + ".");
			}
		}
		return messages;
	}


}

