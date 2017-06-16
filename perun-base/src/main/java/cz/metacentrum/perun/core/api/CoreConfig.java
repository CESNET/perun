package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Set;

/**
 * Core configuration values. Bean initialized by Spring.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class CoreConfig {

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
}
