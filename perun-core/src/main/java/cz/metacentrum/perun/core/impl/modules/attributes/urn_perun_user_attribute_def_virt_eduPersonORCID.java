package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;
import org.apache.commons.lang3.StringUtils;

/**
 * ORCIDs collected from UserExtSources attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_eduPersonORCID extends UserVirtualAttributeCollectedFromUserExtSource {

	@Override
	public String getSourceAttributeFriendlyName() {
		return "eppn";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "eduPersonORCID";
	}

	@Override
	public String getDestinationAttributeDescription() {
		return "All ORCIDs of a user";
	}

	@Override
	public String modifyValue(PerunSession session, ModifyValueContext ctx, UserExtSource ues, String value) {
		if(value.endsWith("@orcid")) {
			return "http://orcid.org/"+StringUtils.substringBefore(value,"@");
		} else {
			return null;
		}

	}
}
