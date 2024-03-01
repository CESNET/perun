package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserExtSourceAttributesModuleAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Attribute module for isCesnetEligibleLastSeen, value is String representing timestamp.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class urn_perun_ues_attribute_def_def_isCesnetEligibleLastSeen extends UserExtSourceAttributesModuleAbstract {

  private static final String A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN =
      AttributesManager.NS_USER_ATTR_DEF + ":isCesnetEligibleLastSeen";
  private static final Logger log =
      LoggerFactory.getLogger(urn_perun_ues_attribute_def_def_isCesnetEligibleLastSeen.class);

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, UserExtSource ues, Attribute attribute)
      throws WrongAttributeValueException {
    if (attribute.getValue() == null) {
      return;
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setLenient(false);
    try {
      dateFormat.parse(attribute.valueAsString());
    } catch (ParseException ex) {
      throw new WrongAttributeValueException(attribute,
          "Format of timestamp is not correct, it should be 'yyyy-MM-dd HH:mm:ss'", ex);
    }
  }

  /**
   * When isCesnetEligibleLastSeen of a user's ext source is set, check if it is
   * more recent than the user's current isCesnetEligibleLastSeen. If so, update it.
   *
   * @param session
   * @param ues
   * @param attribute
   */
  @Override
  public void changedAttributeHook(PerunSessionImpl session, UserExtSource ues, Attribute attribute) {
    if (attribute.getValue() == null) {
      return;
    }

    User user;
    try {
      user = session.getPerunBl().getUsersManagerBl().getUserById(session, ues.getUserId());
    } catch (UserNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }

    Attribute userIsCesnetEligible;
    try {
      userIsCesnetEligible = session.getPerunBl().getAttributesManagerBl()
          .getAttribute(session, user, A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN);
    } catch (AttributeNotExistsException ex) {
      log.warn("Attribute {} doesn't exist.", A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN);
      return;
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setLenient(false);
    try {
      Date uesTimestamp = dateFormat.parse(attribute.valueAsString());
      // if user's isCesnetEligibleLastSeen is null or earlier than new value of ues isCesnetEligibleLastSeen, update it
      if (userIsCesnetEligible.getValue() == null ||
          uesTimestamp.after(dateFormat.parse(userIsCesnetEligible.valueAsString()))) {
        userIsCesnetEligible.setValue(attribute.getValue());
        session.getPerunBl().getAttributesManagerBl().setAttribute(session, user, userIsCesnetEligible);
      }
    } catch (ParseException | WrongAttributeAssignmentException | WrongAttributeValueException |
             WrongReferenceAttributeValueException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_UES_ATTR_DEF);
    attr.setFriendlyName("isCesnetEligibleLastSeen");
    attr.setDisplayName("isCesnetEligibleLastSeen");
    attr.setType(String.class.getName());
    attr.setDescription("isCesnetEligibleLastSeen");
    return attr;
  }
}
