package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * IMPORTANT: will be removed in next release!!!
 * Test methods for urn_perun_user_attribute_def_def_elixirBonaFideStatus
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Deprecated
public class urn_perun_user_attribute_def_def_elixirBonaFideStatusTest {

	private static final String VALUE = "http://www.ga4gh.org/beacon/bonafide/ver1.0";

	private static final String USER_BONA_FIDE_STATUS_REMS_ATTR_NAME = "elixirBonaFideStatusREMS";
	private static final String USER_AFFILIATIONS_ATTR_NAME = "eduPersonScopedAffiliations";
	private static final String USER_PUBLICATIONS_ATTR_NAME = "publications";

	private static final String A_U_D_userBonaFideStatusRems = AttributesManager.NS_USER_ATTR_DEF + ":" + USER_BONA_FIDE_STATUS_REMS_ATTR_NAME;
	private static final String A_U_D_userPublications = AttributesManager.NS_USER_ATTR_VIRT + ":" + USER_PUBLICATIONS_ATTR_NAME;
	private static final String A_U_V_userAffiliations = AttributesManager.NS_USER_ATTR_VIRT + ":" + USER_AFFILIATIONS_ATTR_NAME;

	private urn_perun_user_attribute_def_def_elixirBonaFideStatus classInstance;

	private PerunSessionImpl session;
	private User user;
	private Attribute eduPersonScopedAffiliations;
	private Attribute publicationAttribute;
	private Attribute elixirBonaFideStatusREMS;

	private String ELIXIR_KEY = "ELIXIR";

	@Before
	public void setUp() {
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

		user = new User();
		user.setId(1);

		List<String> EPSA_VAL = new ArrayList<>();
		EPSA_VAL.add("faculty@somewhere.edu");
		EPSA_VAL.add("member@somewhere.edu");
		eduPersonScopedAffiliations = new Attribute();
		eduPersonScopedAffiliations.setValue(EPSA_VAL);

		LinkedHashMap<String, String> PUBLICATIONS_VAL = new LinkedHashMap<>();
		PUBLICATIONS_VAL.put(ELIXIR_KEY, "3");
		PUBLICATIONS_VAL.put("KEY", "9");
		publicationAttribute = new Attribute();
		publicationAttribute.setValue(PUBLICATIONS_VAL);

		elixirBonaFideStatusREMS = new Attribute();
		elixirBonaFideStatusREMS.setValue("IS_RESEARCHER");
	}

	@Test
	public void fillAttributeWithNoDependenciesFilled() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(
				null
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
				null
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
				null
		);

		Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
		assertNull("returned value is incorrect", receivedAttr.getValue());
	}

	@Test
	public void fillAttributeWithDependenciesHavingUnsatisfyingValues() throws Exception {
		List<String> EPSA_FAILING_VAL = new ArrayList<>();
		EPSA_FAILING_VAL.add("member@here.edu");
		Attribute eduPersonScopedAffiliationsNotSatisfying = new Attribute();
		eduPersonScopedAffiliations.setValue(EPSA_FAILING_VAL);

		LinkedHashMap<String, String> PUBLICATIONS_FAILING_VAL = new LinkedHashMap<>();
		PUBLICATIONS_FAILING_VAL.put(ELIXIR_KEY, "0");
		PUBLICATIONS_FAILING_VAL.put("KEY", "5");
		Attribute publicationAttributeNotSatisfying = new Attribute();
		publicationAttributeNotSatisfying.setValue(PUBLICATIONS_FAILING_VAL);

		Attribute elixirBonaFideStatusREMSEmpty = new Attribute();
		elixirBonaFideStatusREMSEmpty.setValue(null);

		classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(
				elixirBonaFideStatusREMSEmpty
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
				eduPersonScopedAffiliationsNotSatisfying
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
				publicationAttributeNotSatisfying
		);

		Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
		assertNull("returned value is incorrect", receivedAttr.getValue());
	}

	@Test
	public void fillAttributeWithOnlyREMSFilled() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(
				elixirBonaFideStatusREMS
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
				null
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
				null
		);

		Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
		assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);
	}

	@Test
	public void fillAttributeWithOnlyEPSAFilled() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(
				null
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
				eduPersonScopedAffiliations
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
				null
		);

		Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
		assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);
	}

	@Test
	public void fillAttributeWithOnlyPublicationsFilled() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(
				null
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
				null
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
				publicationAttribute
		);

		Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
		assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);

	}

	@Test
	public void fillAttributeWithAllDependencyAttrsFiled() throws Exception {
		classInstance = new urn_perun_user_attribute_def_def_elixirBonaFideStatus();

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userBonaFideStatusRems)).thenReturn(
				elixirBonaFideStatusREMS
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_V_userAffiliations)).thenReturn(
				eduPersonScopedAffiliations
		);

		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_D_userPublications)).thenReturn(
				publicationAttribute
		);

		Attribute receivedAttr = classInstance.fillAttribute(session, user, classInstance.getAttributeDefinition());
		assertEquals("returned value is incorrect", receivedAttr.getValue(), VALUE);
	}

}
