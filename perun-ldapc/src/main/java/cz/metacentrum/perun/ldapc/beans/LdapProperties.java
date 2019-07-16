package cz.metacentrum.perun.ldapc.beans;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * LdapProperties data from properties file.
 */
public class LdapProperties {

	private String ldapConsumerName;
	private String ldapBase;
	private String ldapLoginNamespace;

	public LdapProperties(String ldapConsumerName, String ldapBase, String ldapLoginNamespace) {
		this.ldapConsumerName = ldapConsumerName;
		this.ldapBase = ldapBase;
		this.ldapLoginNamespace = ldapLoginNamespace;
	}

	public boolean propsLoaded() {
		return ldapConsumerName!=null&&ldapBase!=null&&ldapLoginNamespace!=null;
	}

	public String getLdapConsumerName() {
		return ldapConsumerName;
	}

	public String getLdapBase() {
		return ldapBase;
	}

	public String getLdapLoginNamespace() {
		return ldapLoginNamespace;
	}
}
