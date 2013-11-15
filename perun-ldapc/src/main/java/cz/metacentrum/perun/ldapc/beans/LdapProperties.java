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

    public void setLdapcProperties(Properties ldapcProperties) {
        this.ldapcProperties = ldapcProperties;
    }
}
