package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

/**
 * All affiliations collected from UserExtSources attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
public class urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations extends UserVirtualAttributeCollectedFromUserExtSource {

	@Override
	public String getSourceAttributeFriendlyName() {
		return "affiliation";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "eduPersonScopedAffiliations";
	}

}
