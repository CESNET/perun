package cz.metacentrum.perun.core.api;


import java.util.Arrays;
import java.util.List;

public class Role {

  public static final String PERUNADMIN = "PERUNADMIN";
  public static final String PERUNADMINBA = "PERUNADMINBA";
  public static final String PERUNOBSERVER = "PERUNOBSERVER";
  public static final String VOADMIN = "VOADMIN";
  public static final String ORGANIZATIONMEMBERSHIPMANAGER = "ORGANIZATIONMEMBERSHIPMANAGER";
  public static final String VOCREATOR = "VOCREATOR";
  public static final String GROUPADMIN = "GROUPADMIN";
  public static final String GROUPOBSERVER = "GROUPOBSERVER";
  public static final String GROUPMEMBERSHIPMANAGER = "GROUPMEMBERSHIPMANAGER";
  public static final String SELF = "SELF";
  public static final String FACILITYADMIN = "FACILITYADMIN";
  public static final String FACILITYOBSERVER = "FACILITYOBSERVER";
  public static final String FACILITYCREATOR = "FACILITYCREATOR";
  public static final String TRUSTEDFACILITYADMIN = "TRUSTEDFACILITYADMIN";
  public static final String RESOURCEADMIN = "RESOURCEADMIN";
  public static final String RESOURCEOBSERVER = "RESOURCEOBSERVER";
  public static final String RESOURCESELFSERVICE = "RESOURCESELFSERVICE";
  public static final String REGISTRAR = "REGISTRAR";
  public static final String ENGINE = "ENGINE";
  public static final String RPC = "RPC";
  public static final String NOTIFICATIONS = "NOTIFICATIONS";
  public static final String SERVICEUSER = "SERVICEUSER";
  public static final String SPREGAPPLICATION = "SPREGAPPLICATION";
  public static final String SPONSOR = "SPONSOR";
  public static final String SPONSORNOCREATERIGHTS = "SPONSORNOCREATERIGHTS";
  public static final String VOOBSERVER = "VOOBSERVER";
  public static final String TOPGROUPCREATOR = "TOPGROUPCREATOR";
  public static final String GROUPCREATOR = "GROUPCREATOR";
  public static final String CABINETADMIN = "CABINETADMIN";
  public static final String AUDITCONSUMERADMIN = "AUDITCONSUMERADMIN";
  public static final String SPONSORSHIP = "SPONSORSHIP";
  public static final String UNKNOWNROLENAME = "UNKNOWN";
  public static final String PASSWORDRESETMANAGER = "PASSWORDRESETMANAGER";
  public static final String MEMBERSHIP = "MEMBERSHIP";
  public static final String MFA = "MFA";
  public static final String PROXY = "PROXY";
  public static final String VOBANMANAGER = "VOBANMANAGER";
  public static final String RESOURCEBANMANAGER = "RESOURCEBANMANAGER";
  public static final String FACILITYBANMANAGER = "FACILITYBANMANAGER";
  public static final String SERVICEACCOUNTCREATOR = "SERVICEACCOUNTCREATOR";
  public static final String EXEMPTEDFROMMFA = "EXEMPTEDFROMMFA";

  private Role() {
  }

  public static List<String> rolesAsList() {
    return Arrays.asList(AUDITCONSUMERADMIN, CABINETADMIN, ENGINE, FACILITYADMIN, FACILITYOBSERVER, FACILITYCREATOR,
        TRUSTEDFACILITYADMIN, GROUPADMIN, GROUPOBSERVER, GROUPMEMBERSHIPMANAGER, MEMBERSHIP, NOTIFICATIONS,
        PASSWORDRESETMANAGER, PERUNADMIN, PERUNADMINBA, PERUNOBSERVER, REGISTRAR, RESOURCEADMIN, RESOURCEOBSERVER,
        RESOURCESELFSERVICE, RPC, SELF, SERVICEUSER, SPREGAPPLICATION, SPONSOR, TOPGROUPCREATOR,
        UNKNOWNROLENAME, VOADMIN, VOCREATOR, VOOBSERVER, SPONSORSHIP, MFA, PROXY, VOBANMANAGER, RESOURCEBANMANAGER,
        FACILITYBANMANAGER, SPONSORNOCREATERIGHTS, SERVICEACCOUNTCREATOR, EXEMPTEDFROMMFA,
        ORGANIZATIONMEMBERSHIPMANAGER, GROUPCREATOR);
  }
}
