package cz.metacentrum.perun.ldapc.beans;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * LdapProperties data from properties file.
 * This class is singleton.
 * 
 * @author norexan
 */
public class LdapProperties {
  
    private static LdapProperties instance = null;
    
    @Autowired
    private Properties ldapcProperties;
    
    private String ldapConsumerName;
    private String ldapBase;
    private String ldapLoginNamespace;
    
    protected LdapProperties() {
       this.ldapConsumerName=ldapcProperties.getProperty("ldap.consumerName");
       this.ldapBase=ldapcProperties.getProperty("ldap.base");
       this.ldapLoginNamespace=ldapcProperties.getProperty("ldap.loginNamespace");
    }

    public Properties getLdapcProperties() {
        return ldapcProperties;
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
    
    public synchronized static LdapProperties getInstance() {
        if(instance == null) {
            instance = new LdapProperties();
        }
        return instance;
    }
}
