package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.impl.PerunAppsConfig;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;

/**
 * Virtual attribute for group detail URL in Perun Admin GUI.
 *
 * @author Matej Hakoš <492968@mail.muni.cz>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_group_attribute_def_virt_groupDetailUrl extends GroupVirtualAttributesModuleAbstract
    implements GroupVirtualAttributesModuleImplApi {

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition definition = new AttributeDefinition();
    definition.setNamespace(AttributesManager.NS_GROUP_ATTR_VIRT);
    definition.setDisplayName("Group detail URL");
    definition.setFriendlyName("groupDetailUrl");
    definition.setDescription("URL of the group detail page in Perun Admin GUI.");
    definition.setType(String.class.getName());
    return definition;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl perunSession, Group group,
                                     AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);
    int groupId = group.getId();
    int voId = group.getVoId();
    PerunAppsConfig.Brand defaultBrand = PerunAppsConfig.getDefaultBrand();
    String domain = defaultBrand.getNewApps().getAdmin();
    try {
      Vo vo = perunSession.getPerunBl().getVosManager().getVoById(perunSession, voId);
      domain = PerunAppsConfig.getBrandContainingVo(vo.getShortName()).getNewApps().getAdmin();
    } catch (PerunException e) {
      // In case VO does not exist, use default Branding
    }
    String value = domain + "/organizations/" + voId + "/groups/" + groupId;
    attribute.setValue(value);
    return attribute;
  }
}
