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
 * Virtual user's attribute for 'converting' TLD/DNS name to specific country name.
 * Using help of entityless attribute dnsStateMapping for conversion.
 * Using the greedy approach -> always taking the longest match from the available ones.
 *
 * <BR>Examples:
 * <BR>muni.cz -> Czech Republic
 * <BR>oxford.ac.uk -> United Kingdom
 * <BR>.ebi.ac.uk -> ""
 *
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 */
public class urn_perun_user_attribute_def_virt_institutionsCountry extends UserVirtualAttributeCollectedFromUserExtSource {

	@Override
	public String getSourceAttributeFriendlyName() {
		return "schacHomeOrganisation";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "institutionsCountry";
	}

	@Override
	public String getDestinationAttributeDescription() {
		return "Country names associated with user.";
	}

	private static class MyCtx extends ModifyValueContext {
		Map<String, String> dnsMap;

		public Map<String, String> getDnsMap() {
			return dnsMap;
		}

		MyCtx(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
			super(sess, user, destinationAttributeDefinition);
			AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();
			Attribute dnsStateMapping = null;
			try {
				dnsStateMapping = am.getAttribute(sess, KEY, new urn_perun_entityless_attribute_def_def_dnsStateMapping().getAttributeDefinition().getName());
			} catch (InternalErrorException | AttributeNotExistsException | WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			}
			//noinspection unchecked
			dnsMap = (Map<String, String>) dnsStateMapping.getValue();
		}

	}

	@Override
	protected ModifyValueContext initModifyValueContext(PerunSessionImpl sess, User user, AttributeDefinition destinationAttributeDefinition) throws InternalErrorException {
		return new MyCtx(sess, user, destinationAttributeDefinition);
	}

	@Override
	public String modifyValue(ModifyValueContext ctx, String value) {
		Map<String, String> dnsMap = ((MyCtx) ctx).getDnsMap();
		int matchLength = 0;
		String match = null;
		for (String mapkey : dnsMap.keySet()) {
			if (value.endsWith(mapkey) && mapkey.length() > matchLength) {
				matchLength = mapkey.length();
				match = mapkey;
			}
		}
		return match == null ? "" : dnsMap.get(match);
	}
}
