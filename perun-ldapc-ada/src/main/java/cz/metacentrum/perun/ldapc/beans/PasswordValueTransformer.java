package cz.metacentrum.perun.ldapc.beans;

import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;

public class PasswordValueTransformer implements AttributeValueTransformer {

	@Autowired 
	protected LdapProperties ldapProperties;
	
	@Override
	public String getValue(String value, Attribute attr) {
		return "{SASL}" + attr.getValue() + "@" + ldapProperties.getLdapLoginNamespace().toUpperCase();
	}

}
