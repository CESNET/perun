package cz.metacentrum.perun.ldapc.beans;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * LdapProperties data from properties file.
 *
 * @author norexan
 */
public class LdapProperties {

	private Properties ldapcProperties;
	private String ldapConsumerName;
	private String ldapBase;
	private String ldapLoginNamespace;

	@Autowired
	public LdapProperties(Properties ldapcProperties) {
		this.ldapcProperties=ldapcProperties;
	}

	public Properties getLdapcProperties() {
		return ldapcProperties;
	}

	public String getLdapConsumerName() {
		return this.getLdapcProperties().getProperty("ldap.consumerName");
	}

	public String getLdapBase() {
		return this.getLdapcProperties().getProperty("ldap.base");
	}

	public String getLdapLoginNamespace() {
		return this.getLdapcProperties().getProperty("ldap.loginNamespace");
	}

	/**
	 * This means that object class groupOfUniqueNames is not supported yet!
	 *
	 * @return true if not supported, false if still needed
	 */
	public boolean isThisNewVersionOfLdap() {
		String newVersionOfLdap = this.getLdapcProperties().getProperty("ldap.newVersionOfLdap");
		if("true".equals(newVersionOfLdap)) {
			return true;
		}
		return false;
	}

	public void setLdapcProperties(Properties ldapcProperties) {
		this.ldapcProperties = ldapcProperties;
	}
}
