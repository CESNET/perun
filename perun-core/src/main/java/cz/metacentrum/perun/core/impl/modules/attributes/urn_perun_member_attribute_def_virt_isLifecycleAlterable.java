package cz.metacentrum.perun.core.impl.modules.attributes;

import static cz.metacentrum.perun.core.blImpl.VosManagerBlImpl.A_MEMBER_DEF_MEMBER_ORGANIZATIONS;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns true, if member's lifecycle can be altered (status change, expiration, deletion...), else returns false.
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_member_attribute_def_virt_isLifecycleAlterable extends MemberVirtualAttributesModuleAbstract
    implements MemberVirtualAttributesModuleImplApi {

  static final Logger LOG = LoggerFactory.getLogger(urn_perun_member_attribute_def_virt_isLifecycleAlterable.class);

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_VIRT);
    attr.setFriendlyName("isLifecycleAlterable");
    attr.setDisplayName("Is lifecycle alterable");
    attr.setType(Boolean.class.getName());
    attr.setDescription(
        "Whether member's lifecycle alteration (extension, status change, deletion) can be performed. " +
        "Only lifecycles of members who came from member vo to hierarchical vo cannot be altered.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, Member member, AttributeDefinition attribute) {
    Attribute newAttribute = new Attribute(attribute);

    try {
      Attribute memberOrganizationsAttr =
          sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, member, A_MEMBER_DEF_MEMBER_ORGANIZATIONS);
      Vo vo = sess.getPerunBl().getVosManagerBl().getVoById(sess, member.getVoId());
      ArrayList<String> memberOrganizations = memberOrganizationsAttr.valueAsList();
      newAttribute.setValue(memberOrganizations == null || memberOrganizations.isEmpty() ||
                            memberOrganizations.equals(new ArrayList<>(List.of(vo.getShortName()))));
      return newAttribute;

    } catch (WrongAttributeAssignmentException | AttributeNotExistsException | VoNotExistsException e) {
      LOG.warn("Cannot decide if lifecycle alteration is possible for member {}.", member);
    }
    return newAttribute;

  }

}
