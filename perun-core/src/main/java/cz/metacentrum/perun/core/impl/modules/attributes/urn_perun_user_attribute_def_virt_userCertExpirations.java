package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.apache.commons.codec.binary.Base64;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Get and set specified user certificate expiration
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_user_attribute_def_virt_userCertExpirations extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		HashMap<String, String> certsExpirations = new LinkedHashMap<>();

		try {
			Attribute userCertsAttribute = getUserCertsAttribute(sess, user);
			HashMap<String,String> certs = (LinkedHashMap<String,String>) userCertsAttribute.getValue();

			if (certs != null) {
				for (String certDN : certs.keySet()) {
					String cert = certs.get(certDN);

					// Remove --- BEGIN --- and --- END ----
					String certWithoutBegin = cert.replaceFirst("-----BEGIN CERTIFICATE-----", "");
					String rawCert = certWithoutBegin.replaceFirst("-----END CERTIFICATE-----", "");

					X509Certificate x509 = X509Certificate.getInstance(Base64.decodeBase64(rawCert.getBytes()));

					// TODO use some defined date/time format
					DateFormat dateFormat = DateFormat.getDateInstance();
					certsExpirations.put(certDN, dateFormat.format(x509.getNotAfter()));
				}
				Utils.copyAttributeToViAttributeWithoutValue(userCertsAttribute, attribute);
			}
		} catch (AttributeNotExistsException ex) {
			// FIXME throw new WrongReferenceAttributeValueException("User " + user + " doesn't have assigned urn:perun:user:attribute-def:def:userCertificates attribute", ex);
		} catch (CertificateException e) {
			throw new InternalErrorException("CertificateException - user: " + user + ".", e);
		}

		attribute.setValue(certsExpirations);
		return attribute;
	}

	private Attribute getUserCertsAttribute(PerunSessionImpl sess, User user) throws InternalErrorException, AttributeNotExistsException {
		try {
			return sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF + ":userCertificates");
		} catch(WrongAttributeAssignmentException ex) { throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> strongDependencies = new ArrayList<>();
		strongDependencies.add(AttributesManager.NS_USER_ATTR_DEF + ":userCertificates");
		return strongDependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("userCertExpirations");
		attr.setDisplayName("Certificates expirations");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Expiration of user certificate.");
		return attr;
	}
}
