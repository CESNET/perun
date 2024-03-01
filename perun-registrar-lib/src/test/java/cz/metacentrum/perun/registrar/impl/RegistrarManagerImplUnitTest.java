package cz.metacentrum.perun.registrar.impl;

import static cz.metacentrum.perun.registrar.impl.RegistrarManagerImpl.URN_USER_DISPLAY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.CoreConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class RegistrarManagerImplUnitTest {
  RegistrarManagerImpl registrarManager;

  CoreConfig coreConfig;
  CoreConfig mockConfig = mock(CoreConfig.class, RETURNS_DEEP_STUBS);

  @Before
  public void setUp() {
    coreConfig = BeansUtils.getCoreConfig();
    BeansUtils.setConfig(mockConfig);

    registrarManager = new RegistrarManagerImpl();
  }

  @After
  public void tearDown() {
    BeansUtils.setConfig(coreConfig);
  }

  @Test
  public void testDisplayNameWithoutOtherAttributes() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Vojtech Sassmann");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, null);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc.");
    assertThat(candidate.getTitleAfter()).isEqualTo(null);
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
  }

  @Test
  public void testDisplayNameWithMiddleNameAndFedData() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Vojtech Jan Sassmann Dis.");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("givenName", "Vojtech");
    fedData.put("sn", "Sassmann");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc.");
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis.");
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
    assertThat(candidate.getMiddleName()).isEqualTo("Jan");
  }

  @Test
  public void testDisplayNameReverseWithMiddleNameAndFedData() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Sassmann Jan Vojtech Dis.");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("givenName", "Vojtech");
    fedData.put("sn", "Sassmann");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc.");
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis.");
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
    assertThat(candidate.getMiddleName()).isEqualTo("Jan");
  }

  @Test
  public void testDisplayNameReverseAndFedData() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Sassmann Vojtech Dis.");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("givenName", "Vojtech");
    fedData.put("sn", "Sassmann");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc.");
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis.");
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
    assertThat(candidate.getMiddleName()).isEqualTo(null);
  }

  @Test
  public void testDisplayNameWithOnlyLastNameAndFedInfo() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Sassmann");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("sn", "Sassmann");
    fedData.put("givenName", "Vojtech");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo(null);
    assertThat(candidate.getTitleAfter()).isEqualTo(null);
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
  }

  @Test
  public void testParseMultipleTitlesBefore() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Mgr. Vojtech Sassmann");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("sn", "Sassmann");
    fedData.put("givenName", "Vojtech");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc. Mgr.");
    assertThat(candidate.getTitleAfter()).isEqualTo(null);
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
  }

  @Test
  public void testParseMultipleTitlesAfter() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Vojtech Sassmann Dis. Csc.");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("sn", "Sassmann");
    fedData.put("givenName", "Vojtech");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo(null);
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis. Csc.");
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
  }

  @Test
  public void testParseWithoutFedInfo() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Mgr. Bc. Vojtech Sassmann Dis. Csc.");
    Map<String, String> fedData = new HashMap<>();

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo("Mgr. Bc.");
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis. Csc.");
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
  }

  @Test
  public void testDisplayNameReverseWithCommaAndFedData() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Sassmann, Vojtech Dis.");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("givenName", "Vojtech");
    fedData.put("sn", "Sassmann");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc.");
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis.");
    assertThat(candidate.getMiddleName()).isEqualTo(null);
    assertThat(candidate.getLastName()).isEqualTo("Sassmann");
    assertThat(candidate.getFirstName()).isEqualTo("Vojtech");
  }

  @Test
  public void testParseNamesIfTheMiddleNameEqualsFirstName() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Jakub Jakub Hejda Dis.");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("givenName", "Jakub");
    fedData.put("sn", "Hejda");

    Candidate candidate2 = new Candidate();
    Map<String, String> attributes2 = new HashMap<>();
    attributes2.put(URN_USER_DISPLAY_NAME, "Sir doc. John Paolo John Paolo Van Horn Phd.");
    Map<String, String> fedData2 = new HashMap<>();
    fedData2.put("givenName", "John Paolo");
    fedData2.put("sn", "Van Horn");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);
    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate2, attributes2, fedData2);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc.");
    assertThat(candidate.getFirstName()).isEqualTo("Jakub");
    assertThat(candidate.getMiddleName()).isEqualTo("Jakub");
    assertThat(candidate.getLastName()).isEqualTo("Hejda");
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis.");

    assertThat(candidate2.getTitleBefore()).isEqualTo("Sir doc.");
    assertThat(candidate2.getFirstName()).isEqualTo("John Paolo");
    assertThat(candidate2.getMiddleName()).isEqualTo("John Paolo");
    assertThat(candidate2.getLastName()).isEqualTo("Van Horn");
    assertThat(candidate2.getTitleAfter()).isEqualTo("Phd.");
  }

  @Test
  public void testParseNamesIfTheMiddleNameEqualsFirstNameReversePattern() {
    Candidate candidate = new Candidate();
    Map<String, String> attributes = new HashMap<>();
    attributes.put(URN_USER_DISPLAY_NAME, "Bc. Hejda Hejda Jakub Dis.");
    Map<String, String> fedData = new HashMap<>();
    fedData.put("givenName", "Jakub");
    fedData.put("sn", "Hejda");

    registrarManager.parseNamesFromDisplayNameAndFedInfo(candidate, attributes, fedData);

    assertThat(candidate.getTitleBefore()).isEqualTo("Bc.");
    assertThat(candidate.getFirstName()).isEqualTo("Jakub");
    assertThat(candidate.getMiddleName()).isEqualTo("Hejda");
    assertThat(candidate.getLastName()).isEqualTo("Hejda");
    assertThat(candidate.getTitleAfter()).isEqualTo("Dis.");
  }
}
