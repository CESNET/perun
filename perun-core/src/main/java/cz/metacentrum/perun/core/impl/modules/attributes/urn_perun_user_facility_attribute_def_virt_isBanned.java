package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForFacilityAndUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanSetForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanUpdatedForFacility;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserFacilityVirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module for getting information if user is banned on facility.
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_facility_attribute_def_virt_isBanned extends UserFacilityVirtualAttributesModuleAbstract
    implements UserFacilityVirtualAttributesModuleImplApi {
  private static final String attrName = AttributesManager.NS_USER_FACILITY_ATTR_VIRT + ":isBanned";
  private static final Logger LOG = LoggerFactory.getLogger(urn_perun_user_facility_attribute_def_virt_isBanned.class);

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
    attr.setFriendlyName("isBanned");
    attr.setDisplayName("Is banned on Facility");
    attr.setType(Boolean.class.getName());
    attr.setDescription("True if user is banned on facility.");
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user, Facility facility,
                                     AttributeDefinition attributeDefinition) {
    Attribute attribute = new Attribute(attributeDefinition);

    attribute.setValue(sess.getPerunBl().getFacilitiesManagerBl().banExists(sess, user.getId(), facility.getId()));

    return attribute;

  }

  private List<AuditEvent> resolveBanChanged(PerunSessionImpl perunSession, int userId, int facilityId) {
    List<AuditEvent> resolvingMessages = new ArrayList<>();

    try {
      User user = perunSession.getPerunBl().getUsersManagerBl().getUserById(perunSession, userId);
      Facility facility = perunSession.getPerunBl().getFacilitiesManagerBl().getFacilityById(perunSession, facilityId);
      AttributeDefinition attributeDefinition =
          perunSession.getPerunBl().getAttributesManagerBl().getAttributeDefinition(perunSession, attrName);
      resolvingMessages.add(new AttributeChangedForFacilityAndUser(new Attribute(attributeDefinition), facility, user));
    } catch (UserNotExistsException | FacilityNotExistsException | AttributeNotExistsException e) {
      LOG.error("Can't resolve virtual attribute value change for " + this.getClass().getSimpleName() +
                " module because of exception.", e);
    }

    return resolvingMessages;
  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) {
    List<AuditEvent> resolvingMessages = new ArrayList<>();
    if (message == null) {
      return resolvingMessages;
    }

    if (message instanceof BanSetForFacility) {
      return resolveBanChanged(perunSession, ((BanSetForFacility) message).getUserId(),
          ((BanSetForFacility) message).getFacilityId());

    } else if (message instanceof BanRemovedForFacility) {
      return resolveBanChanged(perunSession, ((BanRemovedForFacility) message).getUserId(),
          ((BanRemovedForFacility) message).getFacilityId());

    } else if (message instanceof BanUpdatedForFacility) {
      return resolveBanChanged(perunSession, ((BanUpdatedForFacility) message).getUserId(),
          ((BanUpdatedForFacility) message).getFacilityId());
    }

    return resolvingMessages;
  }
}
