package cz.metacentrum.perun.core.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Core configuration values. Bean initialized by Spring.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CoreConfig {

	private final static Logger log = LoggerFactory.getLogger(CoreConfig.class);

	private Properties properties;

	/**
	 * Stores this bean into static BeansUtils for backward compatibility. Called by init-method in perun-base.xml.
	 */
	public void initBeansUtils() {
		BeansUtils.setConfig(this);
	}

	private boolean dbInitializatorEnabled;
	private boolean readOnlyPerun;
	private int groupSynchronizationInterval;
	private int groupSynchronizationTimeout;
	private int groupMaxConcurentGroupsToSynchronize;
	private int mailchangeValidationWindow;
	private int pwdresetValidationWindow;
	private List<String> admins;
	private List<String> enginePrincipals;
	private List<String> generatedLoginNamespaces;
	private List<String> notificationPrincipals;
	private List<String> proxyIdPs;
	private List<String> registrarPrincipals;
	private List<String> rpcPowerusers;
	private Set<String> dontLookupUsers;
	private String alternativePasswordManagerProgram;
	private String dbType;
	private String instanceId;
	private String instanceName;
	private String mailchangeBackupFrom;
	private String mailchangeSecretKey;
	private String nativeLanguage;
	private String passwordManagerProgram;
	private String pwdresetInitVector;
	private String pwdresetSecretKey;
	private String recaptchaPrivateKey;
	private String rpcPrincipal;
	private String rtDefaultQueue;
	private String rtServiceuserPassword;
	private String rtServiceuserUsername;
	private String rtUrl;
	private String smsProgram;
	private String userExtSourcesPersistent;
	private List<String> allowedCorsDomains;
	private boolean isCsrfProtectionEnabled;

	public int getGroupMaxConcurentGroupsToSynchronize() {
		return groupMaxConcurentGroupsToSynchronize;
	}

	public void setGroupMaxConcurentGroupsToSynchronize(int groupMaxConcurentGroupsToSynchronize) {
		this.groupMaxConcurentGroupsToSynchronize = groupMaxConcurentGroupsToSynchronize;
	}

	private Map<String, List<AttributeDefinition>> attributesForUpdate = new HashMap<>();
	private Map<String, String> oidcIssuersExtsourceNames = new HashMap<>();
	private Map<String, String> oidcIssuersExtsourceTypes = new HashMap<>();

	boolean isDbInitializatorEnabled() {
		return dbInitializatorEnabled;
	}

	public void setDbInitializatorEnabled(boolean dbInitializatorEnabled) {
		this.dbInitializatorEnabled = dbInitializatorEnabled;
	}

	boolean isReadOnlyPerun() {
		return readOnlyPerun;
	}

	public void setReadOnlyPerun(boolean readOnlyPerun) {
		this.readOnlyPerun = readOnlyPerun;
	}

	public int getGroupSynchronizationInterval() {
		return groupSynchronizationInterval;
	}

	public void setGroupSynchronizationInterval(int groupSynchronizationInterval) {
		this.groupSynchronizationInterval = groupSynchronizationInterval;
	}

	public int getGroupSynchronizationTimeout() {
		return groupSynchronizationTimeout;
	}

	public void setGroupSynchronizationTimeout(int groupSynchronizationTimeout) {
		this.groupSynchronizationTimeout = groupSynchronizationTimeout;
	}

	public int getMailchangeValidationWindow() {
		return mailchangeValidationWindow;
	}

	public void setMailchangeValidationWindow(int mailchangeValidationWindow) {
		this.mailchangeValidationWindow = mailchangeValidationWindow;
	}

	public int getPwdresetValidationWindow() {
		return pwdresetValidationWindow;
	}

	public void setPwdresetValidationWindow(int pwdresetValidationWindow) {
		this.pwdresetValidationWindow = pwdresetValidationWindow;
	}

	public List<String> getAdmins() {
		return admins;
	}

	public void setAdmins(List<String> admins) {
		this.admins = admins;
	}

	public List<String> getEnginePrincipals() {
		return enginePrincipals;
	}

	public void setEnginePrincipals(List<String> enginePrincipals) {
		this.enginePrincipals = enginePrincipals;
	}

	public List<String> getGeneratedLoginNamespaces() {
		return generatedLoginNamespaces;
	}

	public void setGeneratedLoginNamespaces(List<String> generatedLoginNamespaces) {
		this.generatedLoginNamespaces = generatedLoginNamespaces;
	}

	public List<String> getNotificationPrincipals() {
		return notificationPrincipals;
	}

	public void setNotificationPrincipals(List<String> notificationPrincipals) {
		this.notificationPrincipals = notificationPrincipals;
	}

	public List<String> getProxyIdPs() {
		return proxyIdPs;
	}

	public void setProxyIdPs(List<String> proxyIdPs) {
		this.proxyIdPs = proxyIdPs;
	}

	public List<String> getRegistrarPrincipals() {
		return registrarPrincipals;
	}

	public void setRegistrarPrincipals(List<String> registrarPrincipals) {
		this.registrarPrincipals = registrarPrincipals;
	}

	public List<String> getRpcPowerusers() {
		return rpcPowerusers;
	}

	public void setRpcPowerusers(List<String> rpcPowerusers) {
		this.rpcPowerusers = rpcPowerusers;
	}

	public Set<String> getDontLookupUsers() {
		return dontLookupUsers;
	}

	public void setDontLookupUsers(Set<String> dontLookupUsers) {
		this.dontLookupUsers = dontLookupUsers;
	}

	public String getAlternativePasswordManagerProgram() {
		return alternativePasswordManagerProgram;
	}

	public void setAlternativePasswordManagerProgram(String alternativePasswordManagerProgram) {
		this.alternativePasswordManagerProgram = alternativePasswordManagerProgram;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getMailchangeBackupFrom() {
		return mailchangeBackupFrom;
	}

	public void setMailchangeBackupFrom(String mailchangeBackupFrom) {
		this.mailchangeBackupFrom = mailchangeBackupFrom;
	}

	public String getMailchangeSecretKey() {
		return mailchangeSecretKey;
	}

	public void setMailchangeSecretKey(String mailchangeSecretKey) {
		this.mailchangeSecretKey = mailchangeSecretKey;
	}

	public String getNativeLanguage() {
		return nativeLanguage;
	}

	public void setNativeLanguage(String nativeLanguage) {
		this.nativeLanguage = nativeLanguage;
	}

	public String getPasswordManagerProgram() {
		return passwordManagerProgram;
	}

	public void setPasswordManagerProgram(String passwordManagerProgram) {
		this.passwordManagerProgram = passwordManagerProgram;
	}

	public String getPwdresetInitVector() {
		return pwdresetInitVector;
	}

	public void setPwdresetInitVector(String pwdresetInitVector) {
		this.pwdresetInitVector = pwdresetInitVector;
	}

	public String getPwdresetSecretKey() {
		return pwdresetSecretKey;
	}

	public void setPwdresetSecretKey(String pwdresetSecretKey) {
		this.pwdresetSecretKey = pwdresetSecretKey;
	}

	public String getRecaptchaPrivateKey() {
		return recaptchaPrivateKey;
	}

	public void setRecaptchaPrivateKey(String recaptchaPrivateKey) {
		this.recaptchaPrivateKey = recaptchaPrivateKey;
	}

	public String getRpcPrincipal() {
		return rpcPrincipal;
	}

	public void setRpcPrincipal(String rpcPrincipal) {
		this.rpcPrincipal = rpcPrincipal;
	}

	public String getRtDefaultQueue() {
		return rtDefaultQueue;
	}

	public void setRtDefaultQueue(String rtDefaultQueue) {
		this.rtDefaultQueue = rtDefaultQueue;
	}

	public String getRtServiceuserPassword() {
		return rtServiceuserPassword;
	}

	public void setRtServiceuserPassword(String rtServiceuserPassword) {
		this.rtServiceuserPassword = rtServiceuserPassword;
	}

	public String getRtServiceuserUsername() {
		return rtServiceuserUsername;
	}

	public void setRtServiceuserUsername(String rtServiceuserUsername) {
		this.rtServiceuserUsername = rtServiceuserUsername;
	}

	public String getRtUrl() {
		return rtUrl;
	}

	public void setRtUrl(String rtUrl) {
		this.rtUrl = rtUrl;
	}

	public String getSmsProgram() {
		return smsProgram;
	}

	public void setSmsProgram(String smsProgram) {
		this.smsProgram = smsProgram;
	}

	public String getUserExtSourcesPersistent() {
		return userExtSourcesPersistent;
	}

	public void setUserExtSourcesPersistent(String userExtSourcesPersistent) {
		this.userExtSourcesPersistent = userExtSourcesPersistent;
	}

	public void setOidcIssuers(List<String> oidcIssuers) {
		for (String issuer : oidcIssuers) {
			String iss = getOidcIssuerProperty(issuer, "iss");
			if (iss == null) continue;
			String extSourceName = getOidcIssuerProperty(issuer, "extsource.name");
			if (extSourceName == null) continue;
			String extSourceType = getOidcIssuerProperty(issuer, "extsource.type");
			if (extSourceType == null) continue;
			log.debug("registering OIDC issuer {} with extSourceName={} and extSourceType={}", iss, extSourceName, extSourceType);
			oidcIssuersExtsourceNames.put(iss, extSourceName);
			oidcIssuersExtsourceTypes.put(iss, extSourceType);
		}
	}

	private String getOidcIssuerProperty(String issuer, String suffix) {
		String p = "perun.oidc." + issuer + "." + suffix;
		String value = properties.getProperty(p);
		if (value == null) {
			log.error("property {} not found, skipping OIDC issuer {}", p, issuer);
		}
		return value;
	}

	public Map<String, String> getOidcIssuersExtsourceNames() {
		return oidcIssuersExtsourceNames;
	}

	public Map<String, String> getOidcIssuersExtsourceTypes() {
		return oidcIssuersExtsourceTypes;
	}

	/**
	 * Attributes to be saved when new PerunSession is created.
	 *
	 * @return a map from ExtSource types like ExtSourcesManager.EXTSOURCE_IDP to lists of attribute definitions
	 */
	public Map<String, List<AttributeDefinition>> getAttributesForUpdate() {
		return attributesForUpdate;
	}

	private void createAttributeDefinitions(String extSourceType, List<String> attrNames) {
		List<AttributeDefinition> attrs = new ArrayList<>();
		for (String attrName : attrNames) {
			AttributeDefinition attr = new Attribute();
			attr.setType(String.class.getName());
			attr.setNamespace("urn:perun:ues:attribute-def:def");
			attr.setFriendlyName(attrName);
			switch (attrName) {
				case "mail":
					attr.setDisplayName("mail");
					attr.setDescription("email address");
					break;
				case "cn":
					attr.setDisplayName("common name");
					attr.setDescription("full name of person");
					break;
				case "sn":
					attr.setDisplayName("surname");
					attr.setDescription("family name, usually last name (first in Hungary)");
					break;
				case "givenName":
					attr.setDisplayName("given name");
					attr.setDescription("usually first name (last in Hungary)");
					break;
				case "eppn":
					attr.setDisplayName("eduPersonPrincipalName");
					attr.setDescription("person identifier in academic federations");
					break;
				case "epuid":
					attr.setDisplayName("eduPersonUniqueId");
					attr.setDescription("non re-assignable person identifier in academic federations");
					break;
				case "displayName":
					attr.setDisplayName("displayName");
					attr.setDescription("full name of person");
					break;
				case "uid":
					attr.setDisplayName("uid");
					attr.setDescription("user identifier");
					break;
				case "o":
					attr.setDisplayName("organization");
					attr.setDescription("user's home organization");
					break;
				case "ou":
					attr.setDisplayName("organization unit");
					attr.setDescription("department, faculty, institute, etc.");
					break;
				case "loa":
					attr.setDisplayName("level of assurance");
					attr.setDescription("confidence in person's identity");
					break;
				case "affiliation":
					attr.setDisplayName("affiliation");
					attr.setDescription("person's relation to organization");
					break;
				case "schacHomeOrganization":
					attr.setDisplayName("schacHomeOrganization");
					attr.setDescription("domain name of person's organization (SChema for Academia)");
					break;
				case "dn":
					attr.setDisplayName("certificate DN");
					attr.setDescription("Distinguished Name from X509 digital certificate");
					break;
				case "cadn":
					attr.setDisplayName("CA DN");
					attr.setDescription("Distinguished Name of Certificate Authority");
					break;
				case "certificate":
					attr.setDisplayName("X509 certificate");
					attr.setDescription("PEM encoded X509 certificate");
					attr.setType(BeansUtils.largeStringClassName);
					break;
				default:
					attr.setDisplayName(attrName);
					attr.setDescription(attrName);
			}
			attrs.add(attr);
		}
		attributesForUpdate.put(extSourceType, attrs);
	}

	public void setAttributesForUpdateIdP(List<String> attrNames) {
		createAttributeDefinitions("cz.metacentrum.perun.core.impl.ExtSourceIdp", attrNames);
	}

	public void setAttributesForUpdateX509(List<String> attrNames) {
		createAttributeDefinitions("cz.metacentrum.perun.core.impl.ExtSourceX509", attrNames);
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public List<String> getAllowedCorsDomains() {
		return allowedCorsDomains;
	}

	public void setAllowedCorsDomains(List<String> allowedCorsDomains) {
		this.allowedCorsDomains = allowedCorsDomains;
	}

	public boolean isCsrfProtectionEnabled() {
		return isCsrfProtectionEnabled;
	}

	public void setCsrfProtectionEnabled(boolean csrfProtectionEnabled) {
		isCsrfProtectionEnabled = csrfProtectionEnabled;
	}

}
