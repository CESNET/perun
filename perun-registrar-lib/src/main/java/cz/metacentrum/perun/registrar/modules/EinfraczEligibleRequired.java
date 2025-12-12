package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.CantBeSubmittedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EinfraczEligibleRequired extends DefaultRegistrarModule {
  private static final Logger LOG = LoggerFactory.getLogger(EinfraczEligibleRequired.class);
  private static final String ELIGIBILITY_FED_ATTR = "https://www.e-infra.cz/ns/user-eligible-v1-1y";
  private static final String ELIGIBILITES_ATTR = AttributesManager.NS_USER_ATTR_VIRT + ":userEligibilities";
  private static final String ELIGIBILITY_ATTR_KEY = "einfracz";

  @Override
  public Application beforeApprove(PerunSession session, Application app)
      throws CantBeApprovedException, RegistrarException, PrivilegeException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String trimmedCreatedAt = app.getCreatedAt().split("\\.")[0]; // trim fractions of a second
    LocalDateTime createdAt = LocalDateTime.parse(trimmedCreatedAt, formatter);
    LocalDateTime yearAgo = LocalDateTime.now().minusYears(1);

    if (createdAt.isAfter(yearAgo)) {
      LinkedHashMap<String, String> additionalAttributes = BeansUtils.stringToMapOfAttributes(app.getFedInfo());
      List<String> eligibility = List.of(additionalAttributes.get("internalEligibilities").split(";"));
      if (eligibility.contains(ELIGIBILITY_FED_ATTR)) {
        return app;
      }
    }

    try {
      if (isUserAttributeValid(session, app.getUser())) {
        return app;
      }
    } catch (PerunException e) {
      throw new RegistrarException(e.getMessage(), e);
    }

    throw new CantBeApprovedException(
        "User is not eligible for e-INFRA CZ services. You must log-in using verified academic identity " +
        "(at least once a year) in order to access e-INFRA CZ services.",
        "NOT_ELIGIBLE_EINFRACZ", null, null, app.getId());
  }

  @Override
  public void canBeApproved(PerunSession session, Application app) throws PerunException {
    beforeApprove(session, app);
  }

  @Override
  public void canBeSubmitted(PerunSession session, Application.AppType appType, Map<String, String> federValues,
                             Map<String, List<String>> externalParams) throws PerunException {
    List<String> eligibility = List.of(federValues.get("internalEligibilities").split(";"));
    if (eligibility.contains(ELIGIBILITY_FED_ATTR)) {
      return;
    }

    if (isUserAttributeValid(session, session.getPerunPrincipal().getUser())) {
      return;
    }

    throw new CantBeSubmittedException(
        "User is not eligible for e-INFRA CZ services. You must log-in using verified academic identity " +
        "(at least once a year) in order to access e-INFRA CZ services.",
        "NOT_ELIGIBLE_EINFRACZ", null, null);
  }

  private boolean isUserAttributeValid(PerunSession sess, User user) throws PerunException {
    if (user == null) {
      return false;
    }
    PerunBl perun = (PerunBl) sess.getPerun();
    Attribute eligibilityAttr = perun.getAttributesManagerBl().getAttribute(sess,
        user, ELIGIBILITES_ATTR);
    if (eligibilityAttr != null && eligibilityAttr.getValue() != null) {
      Map<String, String> eligibilities = eligibilityAttr.valueAsMap();
      if (eligibilities.containsKey(ELIGIBILITY_ATTR_KEY)) {
        Instant timestamp = Instant.ofEpochSecond(Long.parseLong(eligibilities.get(ELIGIBILITY_ATTR_KEY)));
        Instant yearAgo = ZonedDateTime.now().minusYears(1).toInstant();
        return timestamp.isAfter(yearAgo);
      }
    }
    return false;
  }
}
