package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * IMPORTANT: will be removed in next release!!!
 * This module determines if user is a researcher. If so,
 * it provides URL: 'http://www.ga4gh.org/beacon/bonafide/ver1.0'.
 *
 * The decision depends on attributes 'elixirBonaFideStatusREMS', 'eduPersonScopedAffiliations'
 * and 'user:def:publications'.
 * If 'elixirBonaFideStatusREMS' is not empty, user is a researcher.
 * If 'eduPersonScopedAffiliations' contains affiliation that starts with 'faculty@', user is a researcher.
 * If 'user:def:publications' contains key 'ELIXIR' and associated value is > 0, user is a researcher
 * Otherwise, null value is set.
 *
 * @author Vojtech Sassmann &lt;vojtech.sassmann@gmail.com&gt;
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Deprecated
public class urn_perun_user_attribute_def_def_elixirBonaFideStatus extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_elixirBonaFideStatus.class);

	private static final String FRIENDLY_NAME = "elixirBonaFideStatus";
	private static final String URL = "http://www.ga4gh.org/beacon/bonafide/ver1.0";

	private static final String USER_BONA_FIDE_STATUS_REMS_ATTR_NAME = "elixirBonaFideStatusREMS";
	private static final String USER_AFFILIATIONS_ATTR_NAME = "eduPersonScopedAffiliations";
	private static final String USER_PUBLICATIONS_ATTR_NAME = "publications";

	private static final String A_U_D_userBonaFideStatusRems = AttributesManager.NS_USER_ATTR_DEF + ":" + USER_BONA_FIDE_STATUS_REMS_ATTR_NAME;
	private static final String A_U_D_userPublications = AttributesManager.NS_USER_ATTR_VIRT + ":" + USER_PUBLICATIONS_ATTR_NAME;
	private static final String A_U_V_userAffiliations = AttributesManager.NS_USER_ATTR_VIRT + ":" + USER_AFFILIATIONS_ATTR_NAME;

	private static final String ELIXIR_KEY = "ELIXIR";


	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);

		//try to get value from 'elixirBonaFideStatusREMS': if not empty, we have bona_fide
		try {
			Attribute statusAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_D_userBonaFideStatusRems);
			if (statusAttr != null && statusAttr.getValue() != null) {
				attribute.setValue(URL);
				return attribute;
			}
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			log.error("Cannot read {} from user {}", USER_BONA_FIDE_STATUS_REMS_ATTR_NAME, user, e);
		}

		//try to get value from 'eduPersonScopedAffiliations': if has faculty@..., we have bona_fide
		try {
			Attribute affiliationsAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_V_userAffiliations);
			if (affiliationsAttr != null && affiliationsAttr.getValue() != null) {
				ArrayList<String> affiliationsValue = affiliationsAttr.valueAsList();
				for (String val : affiliationsValue) {
					if (val.startsWith("faculty@")) {
						attribute.setValue(URL);
						return attribute;
					}
				}
			}
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			log.error("Cannot read {} from user {}", USER_AFFILIATIONS_ATTR_NAME, user, e);
		}

		//try to get value from publications: if has thanks to ELIXIR, we have bona_fide
		try {
			Attribute publicationsAttr = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_D_userPublications);
			if (publicationsAttr != null && publicationsAttr.getValue() != null) {
				Map<String, String> publications = publicationsAttr.valueAsMap();
				if (publications.containsKey(ELIXIR_KEY)) {
					String value = publications.get(ELIXIR_KEY);
					try {
						int count = Integer.parseInt(value);
						if (count > 0) {
							attribute.setValue(URL);
							return attribute;
						}
					} catch (NumberFormatException ex) {
						log.error("Attribute " + A_U_D_userPublications +" has wrong value for key " + ELIXIR_KEY, ex);
					}
				}
			}
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			log.error("Cannot read {} from user {}", USER_PUBLICATIONS_ATTR_NAME, user, e);
		}

		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName(FRIENDLY_NAME);
		attr.setDisplayName("Bona fide researcher status");
		attr.setType(String.class.getName());
		attr.setDescription("Flag if user is qualified researcher. URI ‘http://www.ga4gh.org/beacon/bonafide/ver1.0’ value is provided if person is bona fide researcher. Empty value otherwise.");
		return attr;
	}
}
