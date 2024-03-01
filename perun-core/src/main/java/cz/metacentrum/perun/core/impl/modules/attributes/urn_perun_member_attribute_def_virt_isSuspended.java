package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Check if member is suspended in the Vo at this very moment.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_member_attribute_def_virt_isSuspended extends MemberVirtualAttributesModuleAbstract
    implements MemberVirtualAttributesModuleImplApi {

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Member member, AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);

    Optional<BanOnVo> ban = sess.getPerunBl().getVosManagerBl().getBanForMember(sess, member.getId());

    if (!ban.isPresent()) {
      attribute.setValue(false);
    } else {
      Date startOfToday = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
      attribute.setValue(ban.get()
          .getValidityTo()
          .after(startOfToday));
    }

    return attribute;
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_VIRT);
    attr.setFriendlyName("isSuspended");
    attr.setDisplayName("Suspended in VO");
    attr.setType(Boolean.class.getName());
    attr.setDescription("If member is suspended in the Vo at this moment.");
    return attr;
  }

}
