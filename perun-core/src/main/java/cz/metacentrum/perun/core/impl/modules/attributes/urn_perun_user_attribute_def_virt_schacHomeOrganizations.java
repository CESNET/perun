package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

/**
 * All schacHomeOrganizations collected from UserExtSources attributes.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_schacHomeOrganizations extends UserVirtualAttributeCollectedFromUserExtSource {

	@Override
	public String getSourceAttributeFriendlyName() {
		return "schacHomeOrganization";
	}

	@Override
	public String getDestinationAttributeFriendlyName() {
		return "schacHomeOrganizations";
	}

}
