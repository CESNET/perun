package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributeCollectedFromUserExtSource;

/**
 * All europeanStudentIDs collected from UserExtSources attributes europeanStudentID.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
@SuppressWarnings("unused")
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_europeanStudentIDs
    extends UserVirtualAttributeCollectedFromUserExtSource {

  @Override
  public String getSourceAttributeFriendlyName() {
    return "europeanStudentID";
  }

  @Override
  public String getDestinationAttributeFriendlyName() {
    return "europeanStudentIDs";
  }

  @Override
  public String getDestinationAttributeDisplayName() {
    return "europeanStudentIDs";
  }

  @Override
  public String getDestinationAttributeDescription() {
    return "All european student IDs of a user.";
  }

}
