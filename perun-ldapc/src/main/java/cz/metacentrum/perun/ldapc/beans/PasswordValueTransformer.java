package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Value transformer, which converts users login into value acceptable by LDAP userPassword attribute. Nor Perun or LDAP
 * actually contains userPassword. Password check in LDAP is redirected to KDC.
 * <p>
 * Transformation example: "login" -> "{SASL}login@namespace" where "namespace" is defined in LDAPc configuration and
 * should match "login-namespace:[namespace]" name of the attribute from Perun.
 */
public class PasswordValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

  @Autowired
  protected LdapProperties ldapProperties;

  @Override
  public String getValue(String value, Attribute attr) {
    return StringUtils.isBlank(ldapProperties.getLdapLoginNamespace()) ? null :
        "{SASL}" + attr.getValue() + "@" + ldapProperties.getLdapLoginNamespace().toUpperCase();
  }

}
