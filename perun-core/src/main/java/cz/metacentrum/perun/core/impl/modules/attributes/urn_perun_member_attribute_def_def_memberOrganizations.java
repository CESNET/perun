package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberAttributesModuleImplApi;

import java.util.ArrayList;

/**
 * Module for list of VOs from where member comes.
 *
 * @author Sarka Palkovicova <sarka.palkovicova@gmail.com>
 */
public class urn_perun_member_attribute_def_def_memberOrganizations extends MemberAttributesModuleAbstract
    implements MemberAttributesModuleImplApi {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    if (attribute.getValue() == null) {
      return;
    }
    for (String voName : attribute.valueAsList()) {
      try {
        perunSession.getPerunBl().getVosManagerBl().getVoByShortName(perunSession, voName);
      } catch (VoNotExistsException e) {
        throw new WrongReferenceAttributeValueException(String.format("VO %s does not exist.", voName));
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
    attr.setFriendlyName("memberOrganizations");
    attr.setDisplayName("Member organizations");
    attr.setType(ArrayList.class.getName());
    attr.setDescription("Short names of member organizations from where member comes.");
    return attr;
  }
}
