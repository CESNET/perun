package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUserExtSource;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeChangedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUes;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUes;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common ancestor class for user virtual attributes that just collect values from userExtSource attributes.
 * <p>
 * For a given user, collects string values of userExtSource attributes with friendly name specified by
 * getSourceAttributeFriendlyName(), and splits them at character ';' which is used by mod_shib to join multiple values,
 * and stores all values into virtual user attribute with friendly name specified by
 * getDestinationAttributeFriendlyName().
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class UserVirtualAttributeCollectedFromUserExtSource
    <T extends UserVirtualAttributeCollectedFromUserExtSource.ModifyValueContext>
    extends UserVirtualAttributesModuleAbstract {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Specifies friendly (short) name of attribute from namespace urn:perun:user:attribute-def:virt where values will be
   * stored
   *
   * @return short name of user attribute which is destination for collected values
   */
  public abstract String getDestinationAttributeFriendlyName();


  /**
   * Specifies friendly (short) name of attribute from namespace urn:perun:ues:attribute-def:def whose values are to be
   * collected.
   *
   * @return short name of userExtSource attribute which is source of values
   */
  public abstract String getSourceAttributeFriendlyName();

  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
    String friendlyName = getDestinationAttributeFriendlyName();
    attr.setFriendlyName(friendlyName);
    attr.setDisplayName(getDestinationAttributeDisplayName());
    attr.setType(ArrayList.class.getName());
    attr.setDescription(getDestinationAttributeDescription());
    return attr;
  }

  @Override
  public Attribute getAttributeValue(PerunSessionImpl sess, User user,
                                     AttributeDefinition destinationAttributeDefinition) {
    T ctx = initModifyValueContext(sess, user, destinationAttributeDefinition);
    Attribute destinationAttribute = new Attribute(destinationAttributeDefinition);
    //for values use set because of avoiding duplicities
    Set<String> valuesWithoutDuplicities = new HashSet<>();

    List<String> attributeExceptions = BeansUtils.getCoreConfig().getIdpLoginValidityExceptions();
    boolean skipLastAccessCheck =
        attributeExceptions != null && attributeExceptions.contains(this.getDestinationAttributeName());

    String sourceAttributeFriendlyName = getSourceAttributeFriendlyName();
    List<UserExtSource> userExtSources = sess.getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
    userExtSources = userExtSources.stream().filter(this.getExtSourceFilter(sess)).collect(Collectors.toList());
    AttributesManagerBl am = sess.getPerunBl().getAttributesManagerBl();

    for (UserExtSource userExtSource : userExtSources) {
      if (!skipLastAccessCheck && !isLastAccessValid(userExtSource)) {
        continue;
      }
      try {
        String sourceAttributeName = getSourceAttributeName();
        Attribute a = am.getAttribute(sess, userExtSource, sourceAttributeName);
        String extLogin = null;
        if (getAlsoExtLogin(sess)) {
          extLogin = userExtSource.getLogin();
        }
        Object value = a.getValue() != null ? a.getValue() : new ArrayList<>();
        a.setValue(value);
        if (value instanceof String) {
          value = extLogin != null && !extLogin.isEmpty() ? value + ";" + extLogin : value;
          //Apache mod_shib joins multiple values with ';', split them again
          String[] rawValues = ((String) value).split(";");
          //add non-null values returned by modifyValue()
          Arrays.stream(rawValues).map(v -> modifyValue(sess, ctx, userExtSource, v)).filter(getValueFilter(sess))
              .forEachOrdered(valuesWithoutDuplicities::add);
        } else if (value instanceof ArrayList) {
          if (extLogin != null && !extLogin.isEmpty()) {
            List<String> listValue = a.valueAsList();
            listValue.add(extLogin);
            a.setValue(listValue);
          }
          //If values are already separated to list of strings
          a.valueAsList().stream().map(v -> modifyValue(sess, ctx, userExtSource, v)).filter(getValueFilter(sess))
              .forEachOrdered(valuesWithoutDuplicities::add);
        }
      } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
        log.error("cannot read " + sourceAttributeFriendlyName + " from userExtSource " + userExtSource.getId() +
                  " of user " + user.getId(), e);
      }
    }

    //convert set to list (values in list will be without duplicities)
    destinationAttribute.setValue(new ArrayList<>(valuesWithoutDuplicities));
    return destinationAttribute;
  }

  public String getDestinationAttributeDescription() {
    return "Collected values of userExtSource attribute " + getDestinationAttributeFriendlyName();
  }

  public String getDestinationAttributeDisplayName() {
    return getDestinationAttributeFriendlyName();
  }

  /**
   * Gets full URN of this virtual user attribute.
   *
   * @return full destination attribute URN
   */
  public final String getDestinationAttributeName() {
    return AttributesManager.NS_USER_ATTR_VIRT + ":" + getDestinationAttributeFriendlyName();
  }

  public List<AttributeHandleIdentifier> getHandleIdentifiers() {
    List<AttributeHandleIdentifier> handleIdenfiers = new ArrayList<>();
    handleIdenfiers.add(auditEvent -> {
      if (auditEvent instanceof AllAttributesRemovedForUserExtSource) {
        return ((AllAttributesRemovedForUserExtSource) auditEvent).getUserExtSource().getUserId();
      } else {
        return null;
      }
    });
    handleIdenfiers.add(auditEvent -> {
      if (auditEvent instanceof AttributeRemovedForUes &&
          ((AttributeRemovedForUes) auditEvent).getAttribute().getFriendlyName()
              .equals(getSourceAttributeFriendlyName())) {
        return ((AttributeRemovedForUes) auditEvent).getUes().getUserId();
      } else {
        return null;
      }
    });
    handleIdenfiers.add(auditEvent -> {
      if (auditEvent instanceof AttributeSetForUes &&
          ((AttributeSetForUes) auditEvent).getAttribute().getFriendlyName().equals(getSourceAttributeFriendlyName())) {
        return ((AttributeSetForUes) auditEvent).getUes().getUserId();
      } else {
        return null;
      }
    });
    return handleIdenfiers;
  }

  /**
   * Gets full URN of the UserExtSource attribute used for computing rhis attribute value.
   *
   * @return full source attribute URN
   */
  public final String getSourceAttributeName() {
    return AttributesManager.NS_UES_ATTR_DEF + ":" + getSourceAttributeFriendlyName();
  }

  /**
   * Override this method to return true to also collect logins from the user ext sources.
   *
   * @return boolean value indicating whether to also collect logins
   */
  protected boolean getAlsoExtLogin(PerunSessionImpl sess) {
    return false;
  }

  /**
   * Override this method to filter the collected values.
   *
   * @return regex by which the collected values will be filtered.
   */
  protected Predicate<String> getValueFilter(PerunSessionImpl sess) {
    return Objects::nonNull;
  }

  /**
   * Override this method to filter the user ext sources (collect the value from the UES only if it passes the filter).
   *
   * @return UES predicate serving as a filter
   */
  protected Predicate<UserExtSource> getExtSourceFilter(PerunSessionImpl sess) {
    return ues -> true;
  }

  protected T initModifyValueContext(PerunSessionImpl sess, User user,
                                     AttributeDefinition destinationAttributeDefinition) {
    //noinspection unchecked
    return (T) new ModifyValueContext(sess, user, destinationAttributeDefinition);
  }

  /**
   * Checks configuration properties idpLoginValidity if last access is not outdated. Skips non-idp ext sources.
   *
   * @param ues user extsource to be checked
   * @return true if ues is of type IdP and its last access is not outdated, false otherwise
   */
  protected boolean isLastAccessValid(UserExtSource ues) {
    if (!ExtSourcesManager.EXTSOURCE_IDP.equals(ues.getExtSource().getType())) {
      return true;
    }

    LocalDateTime lastAccess = LocalDateTime.parse(ues.getLastAccess(), Utils.LAST_ACCESS_FORMATTER);
    return lastAccess.plusMonths(BeansUtils.getCoreConfig().getIdpLoginValidity()).isAfter(LocalDateTime.now());
  }

  /**
   * Override this method if you need to modify the original values. The default implementation makes no modification.
   * Return null if the value should be skipped.
   *
   * @param session PerunSession
   * @param ctx     context initialized in initModifyValueContext method
   * @param ues     UserExtSource
   * @param value   of userExtSource attribute
   * @return modified value or null to skip the value
   */
  public String modifyValue(PerunSession session, T ctx, UserExtSource ues, String value) {
    return value;
  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message)
      throws WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
    List<AuditEvent> resolvingMessages = new ArrayList<>();
    if (message == null) {
      return resolvingMessages;
    }

    List<AttributeHandleIdentifier> list = getHandleIdentifiers();
    for (AttributeHandleIdentifier attributeHandleIdenfier : list) {
      Integer userId = attributeHandleIdenfier.shouldBeEventHandled(message);
      if (userId != null) {
        try {
          User user = perunSession.getPerunBl().getUsersManagerBl().getUserById(perunSession, userId);
          AttributeDefinition attributeDefinition = perunSession.getPerunBl().getAttributesManagerBl()
              .getAttributeDefinition(perunSession, getDestinationAttributeName());
          resolvingMessages.add(new AttributeChangedForUser(new Attribute(attributeDefinition), user));
        } catch (UserNotExistsException e) {
          log.warn("User from UserExtSource doesn't exist in Perun. This occurred while parsing message: {}.", message);
        }
      }
    }

    return resolvingMessages;
  }

  /**
   * Functional interface for controlling AuditEvents.
   * <p>
   * Modules can overwrite method shouldBeEventHandled to change or add events that should handled. Events that should
   * be handled are events which make modules to produce another AuditEvent.
   */
  @FunctionalInterface
  public interface AttributeHandleIdentifier {

    /**
     * Determines whether given auditEvent should be handled. If it should be the method returns userId of user from the
     * auditEvent, otherwise returns null.
     *
     * @param auditEvent given auditEvent
     * @return userId of user from auditEvent, otherwise null
     */
    Integer shouldBeEventHandled(AuditEvent auditEvent);
  }

  public static class ModifyValueContext {
    private final PerunSessionImpl session;
    private final User user;
    private final AttributeDefinition destinationAttributeDefinition;

    public ModifyValueContext(PerunSessionImpl session, User user, AttributeDefinition destinationAttributeDefinition) {
      this.session = session;
      this.user = user;
      this.destinationAttributeDefinition = destinationAttributeDefinition;
    }

    @SuppressWarnings("unused")
    public AttributeDefinition getDestinationAttributeDefinition() {
      return destinationAttributeDefinition;
    }

    public PerunSessionImpl getSession() {
      return session;
    }

    public User getUser() {
      return user;
    }
  }
}
