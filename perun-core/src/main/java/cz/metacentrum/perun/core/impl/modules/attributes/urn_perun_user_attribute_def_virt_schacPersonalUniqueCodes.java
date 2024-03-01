package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

/**
 * All user's unique codes collected from UserExtSources attributes.
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_schacPersonalUniqueCodes
    extends UserVirtualAttributeCollectedFromUserExtSource {

  @Override
  public String getSourceAttributeFriendlyName() {
    return "schacPersonalUniqueCode";
  }

  @Override
  public String getDestinationAttributeFriendlyName() {
    return "schacPersonalUniqueCodes";
  }

}
