package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

/**
 * All eIDASPersonIdentifiers collected from UserExtSources attributes eIDASPersonIdentifier.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_eIDASPersonIdentifiers
    extends UserVirtualAttributeCollectedFromUserExtSource {

  @Override
  public String getDestinationAttributeDescription() {
    return "All eIDAS Person Identifiers of a user.";
  }

  @Override
  public String getDestinationAttributeDisplayName() {
    return "eIDASPersonIdentifiers";
  }

  @Override
  public String getDestinationAttributeFriendlyName() {
    return "eIDASPersonIdentifiers";
  }

  @Override
  public String getSourceAttributeFriendlyName() {
    return "eIDASPersonIdentifier";
  }

}
