package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This attribute module is used to filter user certificates only to ten newest according to certificates expiration
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_userCertificatesLimited extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {


	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) {
		Attribute resultAttribute = new Attribute(attributeDefinition);
		List<String> result = new ArrayList<>();

		Attribute userCertsAttribute = getUserCertsAttribute(sess, user);
		Map<String,String> certs = userCertsAttribute.valueAsMap();
		if (certs != null) {
			Map<String, String> certsExpiration = ModulesUtilsBlImpl.retrieveCertificatesExpiration(certs);

			DateFormat dateFormatInstance = DateFormat.getDateInstance();

			Map<String, Long> certsExpirationInMilliSeconds = new HashMap<>();
			certsExpiration.forEach((key, value) -> {
				try {
					certsExpirationInMilliSeconds.put(key, dateFormatInstance.parse(value).getTime());
				} catch (ParseException ex) {
					throw new ConsistencyErrorException(ex);
				}
			});

			certsExpirationInMilliSeconds.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.limit(10)
				.forEachOrdered(entry -> result.add(certs.get(entry.getKey())));

			Utils.copyAttributeToViAttributeWithoutValue(userCertsAttribute, resultAttribute);
		}

		resultAttribute.setValue(result);

		return resultAttribute;
	}

	private Attribute getUserCertsAttribute(PerunSessionImpl sess, User user) {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertificates");
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public List<String> getStrongDependencies() {
		return Collections.singletonList(AttributesManager.NS_USER_ATTR_DEF + ":userCertificates");
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("userCertificatesLimited");
		attr.setDisplayName("User certificates limited");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("User certificates limited to ten newest certificates.");
		return attr;
	}
}
