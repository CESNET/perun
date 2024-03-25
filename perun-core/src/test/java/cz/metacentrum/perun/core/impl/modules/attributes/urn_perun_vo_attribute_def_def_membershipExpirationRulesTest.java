package cz.metacentrum.perun.core.impl.modules.attributes;

import static cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule.AUTO_EXTENSION_EXT_SOURCES;
import static cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule.AUTO_EXTENSION_LAST_LOGIN_PERIOD;
import static cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule.EXPIRE_SPONSORED_MEMBERS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_vo_attribute_def_def_membershipExpirationRulesTest {

  private static urn_perun_vo_attribute_def_def_membershipExpirationRules classInstance;
  private static PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
  private static Attribute attributeToCheck;
  private static Vo vo;

  @Test
  public void autoExtensionExtSourcesCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(AUTO_EXTENSION_EXT_SOURCES, "2,3");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
  }

  // ------------- Syntax ------------- //

  @Test
  public void autoExtensionExtSourcesInCorrectSemantics() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    when(session.getPerunBl().getExtSourcesManagerBl().getExtSourceById(any(), anyInt())).thenThrow(
        ExtSourceNotExistsException.class);
    value.put(AUTO_EXTENSION_EXT_SOURCES, "3");
    attributeToCheck.setValue(value);
    assertThatExceptionOfType(WrongReferenceAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSemantics(session, vo, attributeToCheck));
  }

  @Test
  public void autoExtensionExtSourcesInCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(AUTO_EXTENSION_EXT_SOURCES, "3,");
    attributeToCheck.setValue(value);
    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, vo, attributeToCheck));
  }

  @Test
  public void autoExtensionExtSourcesInCorrectSyntaxCommaPrefix() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(AUTO_EXTENSION_EXT_SOURCES, ",3");
    attributeToCheck.setValue(value);
    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, vo, attributeToCheck));
  }

  @Test
  public void autoExtensionLastLoginPeriodCorrectSemantics() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    when(session.getPerunBl().getExtSourcesManagerBl().getExtSourceById(any(), anyInt())).thenReturn(new ExtSource());
    value.put(AUTO_EXTENSION_EXT_SOURCES, "3");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSemantics(session, vo, attributeToCheck);
  }

  @Test
  public void autoExtensionLastLoginPeriodCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(AUTO_EXTENSION_LAST_LOGIN_PERIOD, "3m");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
  }

  @Test
  public void autoExtensionLastLoginPeriodInCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(AUTO_EXTENSION_LAST_LOGIN_PERIOD, "-3m");
    attributeToCheck.setValue(value);
    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, vo, attributeToCheck));
  }

  @Test
  public void autoExtensionMultipleExtSourcesCorrectSemantics() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    when(session.getPerunBl().getExtSourcesManagerBl().getExtSourceById(any(), anyInt())).thenReturn(new ExtSource());
    value.put(AUTO_EXTENSION_EXT_SOURCES, "3,2");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSemantics(session, vo, attributeToCheck);
  }

  // ------------- Semantics ------------- //

  @Test
  public void expireSponsoredMembersCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(EXPIRE_SPONSORED_MEMBERS, "false");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
  }

  @Test
  public void expireSponsoredMembersInCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(EXPIRE_SPONSORED_MEMBERS, "wrong value");
    attributeToCheck.setValue(value);
    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, vo, attributeToCheck));
  }

  @Before
  public void setUp() {
    classInstance = new urn_perun_vo_attribute_def_def_membershipExpirationRules();
    attributeToCheck = new Attribute();
    vo = new Vo();
  }
}
