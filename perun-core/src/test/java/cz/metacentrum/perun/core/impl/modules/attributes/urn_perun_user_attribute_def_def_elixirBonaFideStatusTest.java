package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * IMPORTANT: will be removed in next release!!! Test methods for urn_perun_user_attribute_def_def_elixirBonaFideStatus
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Deprecated
public class urn_perun_user_attribute_def_def_elixirBonaFideStatusTest {

  private static final String VALUE = "http://www.ga4gh.org/beacon/bonafide/ver1.0";

  private static final String USER_BONA_FIDE_STATUS_REMS_ATTR_NAME = "elixirBonaFideStatusREMS";
  private static final String USER_AFFILIATIONS_ATTR_NAME = "voPersonExternalAffiliation";
  private static final String USER_PUBLICATIONS_ATTR_NAME = "publications";

  private static final String A_U_D_userBonaFideStatusRems =
      AttributesManager.NS_USER_ATTR_DEF + ":" + USER_BONA_FIDE_STATUS_REMS_ATTR_NAME;
  private static final String A_U_D_userPublications =
      AttributesManager.NS_USER_ATTR_VIRT + ":" + USER_PUBLICATIONS_ATTR_NAME;
  private static final String A_U_V_userAffiliations =
      AttributesManager.NS_USER_ATTR_VIRT + ":" + USER_AFFILIATIONS_ATTR_NAME;
  private static final String ELIXIR_KEY = "ELIXIR";
  private urn_perun_user_attribute_def_def_elixirBonaFideStatus classInstance;
  private PerunSessionImpl session;
  private User user;
  private Attribute voPersonExternalAffiliation;
  private Attribute publicationAttribute;
  private Attribute elixirBonaFideStatusREMS;

  @Test
  public void fillAttributeWithAllDependencyAttrsFiled() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(elixirBonaFideStatusREMS);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
        voPersonExternalAffiliation);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
        publicationAttribute);

    Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
    assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);
  }

  @Test
  public void fillAttributeWithDependenciesHavingUnsatisfyingValues() throws Exception {
    List<String> epsaFailingVal = new ArrayList<>();
    epsaFailingVal.add("member@here.edu");
    Attribute voPersonExternalAffiliationNotSatisfying = new Attribute();
    voPersonExternalAffiliation.setValue(epsaFailingVal);

    LinkedHashMap<String, String> publicationsFailingVal = new LinkedHashMap<>();
    publicationsFailingVal.put(ELIXIR_KEY, "0");
    publicationsFailingVal.put("KEY", "5");
    Attribute publicationAttributeNotSatisfying = new Attribute();
    publicationAttributeNotSatisfying.setValue(publicationsFailingVal);

    Attribute elixirBonaFideStatusREMSEmpty = new Attribute();
    elixirBonaFideStatusREMSEmpty.setValue(null);

    classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(elixirBonaFideStatusREMSEmpty);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
        voPersonExternalAffiliationNotSatisfying);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
        publicationAttributeNotSatisfying);

    Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
    assertNull("returned value is incorrect", receivedAttr.getValue());
  }

  @Test
  public void fillAttributeWithNoDependenciesFilled() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(null);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
        null);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
        null);

    Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
    assertNull("returned value is incorrect", receivedAttr.getValue());
  }

  @Test
  public void fillAttributeWithOnlyEPSAFilled() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(null);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
        voPersonExternalAffiliation);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
        null);

    Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
    assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);
  }

  @Test
  public void fillAttributeWithOnlyPublicationsFilled() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(null);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
        null);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
        publicationAttribute);

    Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
    assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);

  }

  @Test
  public void fillAttributeWithOnlyREMSFilled() throws Exception {
    classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

    when(session.getPerunBl().getAttributesManagerBl()
             .getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(elixirBonaFideStatusREMS);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
        null);

    when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
        null);

    Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
    assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);
  }

  @Before
  public void setUp() {
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    user = new User();
    user.setId(1);

    List<String> epsaVal = new ArrayList<>();
    epsaVal.add("faculty@somewhere.edu");
    epsaVal.add("member@somewhere.edu");
    voPersonExternalAffiliation = new Attribute();
    voPersonExternalAffiliation.setValue(epsaVal);

    LinkedHashMap<String, String> publicationsVal = new LinkedHashMap<>();
    publicationsVal.put(ELIXIR_KEY, "3");
    publicationsVal.put("KEY", "9");
    publicationAttribute = new Attribute();
    publicationAttribute.setValue(publicationsVal);

    elixirBonaFideStatusREMS = new Attribute();
    elixirBonaFideStatusREMS.setValue("IS_RESEARCHER");
  }

}
