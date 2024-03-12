package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_userCertificates extends UserAttributesModuleAbstract
    implements UserAttributesModuleImplApi {

  @Override
  public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute)
      throws WrongAttributeValueException {
    try {
      Map<String, String> certs = attribute.valueAsMap();

      if (certs != null) {
        for (String certDN : certs.keySet()) {
          String cert = certs.get(certDN);

          // Remove --- BEGIN --- and --- END ----
          String certWithoutBegin = cert.replaceFirst("-----BEGIN CERTIFICATE-----", "");
          String rawCert = certWithoutBegin.replaceFirst("-----END CERTIFICATE-----", "");

          CertificateFactory certFact = CertificateFactory.getInstance("X.509");
          try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(rawCert.getBytes()))) {
            certFact.generateCertificate(bis);
          } catch (IllegalArgumentException e) {
            throw new WrongAttributeValueException(attribute, user, "Certificate is not in base64 format!");
          } catch (IOException e) {
            throw new InternalError(e);
          }


        }
      }
    } catch (CertificateException e) {
      throw new WrongAttributeValueException(attribute, user,
          "Wrong format, certificate must be in PEM format prepended by -----BEGIN CERTIFICATE----- and appended by -----END CERTIFICATE-----.",
          e);
    }

  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) {
    return new Attribute(attribute);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attr.setFriendlyName("userCertificates");
    attr.setDisplayName("Certificates");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Hash map for all users full certificates.");
    return attr;
  }
}
