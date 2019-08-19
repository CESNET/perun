package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

/**
 * All eduPersonUniqueIds collected from UserExtSources attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_epuids extends UserVirtualAttributeCollectedFromUserExtSource {

	@Override
	public String getSourceAttributeFriendlyName() {
		return "epuid";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "epuids";
	}

	@Override
	public String getDestinationAttributeDisplayName() {
		return "eduPersonUniqueIds";
	}

	@Override
	public String getDestinationAttributeDescription() {
		return "All eduPersonUniqueIds of a user";
	}

}
