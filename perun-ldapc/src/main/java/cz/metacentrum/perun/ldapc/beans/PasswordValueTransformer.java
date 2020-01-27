package cz.metacentrum.perun.ldapc.beans;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

public class PasswordValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

	@Autowired 
	protected LdapProperties ldapProperties;
	
	@Override
	public String getValue(String value, Attribute attr) {
		return StringUtils.isBlank(ldapProperties.getLdapLoginNamespace()) ? 
				null : "{SASL}" + attr.getValue() + "@" + ldapProperties.getLdapLoginNamespace().toUpperCase();
	}

}
