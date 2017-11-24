package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

import java.util.Map;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_entityless_attribute_def_def_dnsStateMapping.KEY;

/**
 * Virtual user's attribute for converting schacHomeOrganization names to country names.
 * Using help of entityless attribute dnsStateMapping for conversion.
 * Using the greedy approach -> always taking the longest match from the available ones.
 * Returns only non-empty values.
 *
 * <BR>Examples:
 * <BR>muni.cz -> Czech Republic
 * <BR>oxford.ac.uk -> United Kingdom
 * <BR>.ebi.ac.uk -> null
 *
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 */
@SuppressWarnings("unused")
public class urn_perun_user_attribute_def_virt_institutionsCountries extends UserVirtualAttributeCollectedFromUserExtSource {

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

	private static class MyCtx extends ModifyValueContext {
		Map<String, String> dnsMap;

		Map<String, String> getDnsMap() {
			return dnsMap;
		}

		MyCtx(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
			super(sess, user, destinationAttributeDefinition);
			AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
			try {
				String dnsStateMappingName = new urn_perun_entityless_attribute_def_def_dnsStateMapping().getAttributeDefinition().getName();
				Attribute dnsStateMapping = am.getAttribute(sess, KEY, dnsStateMappingName);
				//noinspection unchecked
				dnsMap = (Map<String, String>) dnsStateMapping.getValue();
			} catch (InternalErrorException | AttributeNotExistsException | WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	@Override
	protected ModifyValueContext initModifyValueContext(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
		return new MyCtx(sess, user, destinationAttributeDefinition);
	}

	@Override
	public String modifyValue(ModifyValueContext ctx, String value) {
		Map<String, String> dnsMap = ((MyCtx) ctx).getDnsMap();
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
}
