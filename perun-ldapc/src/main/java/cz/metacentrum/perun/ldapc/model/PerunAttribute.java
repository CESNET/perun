package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunBean;

public interface PerunAttribute<T extends PerunBean> {

  public static final boolean REQUIRED = true;
  public static final boolean OPTIONAL = false;
  public static final boolean MULTIVALUED = true;
  public static final boolean SINGLE = false;

  public String getBaseName();

  MultipleValuesExtractor<T> getMultipleValuesExtractor();

  public String getName();

  public String getName(AttributeDefinition attr);

  SingleValueExtractor<T> getSingleValueExtractor();

  public String getValue(T bean, Attribute... attributes);

  public String[] getValues(T bean, Attribute... attributes);

  public boolean hasValue(T bean, Attribute... attributes);

  public boolean isDeleted();

  public boolean isMultiValued();

  public boolean isRequired();

  public boolean requiresAttributeBean();

  void setMultipleValuesExtractor(MultipleValuesExtractor<T> valueExtractor);

  void setSingleValueExtractor(SingleValueExtractor<T> valueExtractor);

  public interface PerunAttributeNames {

    //PERUN ATTRIBUTES NAMES
    public static final String PERUN_ATTR_PREFERRED_MAIL = "preferredMail";
    public static final String PERUN_ATTR_MAIL = "mail";
    public static final String PERUN_ATTR_ORGANIZATION = "organization";
    public static final String PERUN_ATTR_PHONE = "phone";
    public static final String PERUN_ATTR_USER_CERT_D_NS = "userCertDNs";
    public static final String PERUN_ATTR_BONA_FIDE_STATUS = "elixirBonaFideStatus";
    public static final String PERUN_ATTR_SCHAC_HOME_ORGANIZATIONS = "schacHomeOrganizations";
    public static final String PERUN_ATTR_EDU_PERSON_SCOPED_AFFILIATIONS = "eduPersonScopedAffiliations";
    public static final String PERUN_ATTR_VO_PERSON_EXTERNAL_AFFILIATION = "voPersonExternalAffiliation";
    public static final String PERUN_ATTR_FORWARDED_VO_PERSON_EXTERNAL_AFFILIATION =
        "forwardedVoPersonExternalAffiliation";
    public static final String PERUN_ATTR_ENTITY_ID = "entityID";
    public static final String PERUN_ATTR_CLIENT_ID = "OIDCClientID";
    public static final String PERUN_ATTR_GROUP_NAMES = "groupNames";
    public static final String PERUN_ATTR_INSTITUTIONS_COUNTRIES = "institutionsCountries";
    public static final String PERUN_ATTR_USER_ELIGIBILITIES = "userEligibilities";

    //LDAP ATTRIBUTES NAMES
    public static final String LDAP_ATTR_ASSIGNED_TO_RESOURCE_ID = "assignedToResourceId";
    public static final String LDAP_ATTR_ASSIGNED_GROUP_ID = "assignedGroupId";
    public static final String LDAP_ATTR_DESCRIPTION = "description";
    public static final String LDAP_ATTR_COMMON_NAME = "cn";
    public static final String LDAP_ATTR_PERUN_UNIQUE_GROUP_NAME = "perunUniqueGroupName";
    public static final String LDAP_ATTR_EDU_PERSON_PRINCIPAL_NAMES = "eduPersonPrincipalNames";
    public static final String LDAP_ATTR_USER_IDENTITIES = "userIdentities";
    public static final String LDAP_ATTR_PREFERRED_MAIL = PERUN_ATTR_PREFERRED_MAIL;
    public static final String LDAP_ATTR_MAIL = PERUN_ATTR_MAIL;
    public static final String LDAP_ATTR_ORGANIZATION = "o";
    public static final String LDAP_ATTR_TELEPHONE_NUMBER = "telephoneNumber";
    public static final String LDAP_ATTR_USER_CERT_DNS = "userCertificateSubject";
    public static final String LDAP_ATTR_BONA_FIDE_STATUS = "bonaFideStatus";
    public static final String LDAP_ATTR_SCHAC_HOME_ORGANIZATIONS = PERUN_ATTR_SCHAC_HOME_ORGANIZATIONS;
    public static final String LDAP_ATTR_EDU_PERSON_SCOPED_AFFILIATIONS = PERUN_ATTR_EDU_PERSON_SCOPED_AFFILIATIONS;
    public static final String LDAP_ATTR_VO_PERSON_EXTERNAL_AFFILIATION = PERUN_ATTR_VO_PERSON_EXTERNAL_AFFILIATION;
    public static final String LDAP_ATTR_FORWARDED_VO_PERSON_EXTERNAL_AFFILIATION =
        PERUN_ATTR_FORWARDED_VO_PERSON_EXTERNAL_AFFILIATION;
    public static final String LDAP_ATTR_UID_NUMBER = "uidNumber;x-ns-";
    public static final String LDAP_ATTR_LOGIN = "login;x-ns-";
    public static final String LDAP_ATTR_USER_PASSWORD = "userPassword";
    public static final String LDAP_ATTR_SURNAME = "sn";
    public static final String LDAP_ATTR_GIVEN_NAME = "givenName";
    public static final String LDAP_ATTR_DISPLAY_NAME = "displayName";
    public static final String LDAP_ATTR_ENTITY_ID = PERUN_ATTR_ENTITY_ID;
    public static final String LDAP_ATTR_CLIENT_ID = PERUN_ATTR_CLIENT_ID;
    public static final String LDAP_ATTR_OBJECT_CLASS = "objectClass";
    public static final String LDAP_ATTR_PERUN_VO_ID = "perunVoId";
    public static final String LDAP_ATTR_PERUN_FACILITY_ID = "perunFacilityId";
    public static final String LDAP_ATTR_PERUN_USER_ID = "perunUserId";
    public static final String LDAP_ATTR_PERUN_GROUP_ID = "perunGroupId";
    public static final String LDAP_ATTR_PERUN_RESOURCE_ID = "perunResourceId";
    public static final String LDAP_ATTR_PERUN_PARENT_GROUP = "perunParentGroup";
    public static final String LDAP_ATTR_PERUN_PARENT_GROUP_ID = "perunParentGroupId";
    public static final String LDAP_ATTR_MEMBER_OF = "memberOf";
    public static final String LDAP_ATTR_UNIQUE_MEMBER = "uniqueMember";
    public static final String LDAP_ATTR_MEMBER_OF_PERUN_VO = "memberOfPerunVo";
    public static final String LDAP_ATTR_ENTRY_STATUS = "entryStatus";
    public static final String LDAP_ATTR_IS_SERVICE_USER = "isServiceUser";
    public static final String LDAP_ATTR_IS_SPONSORED_USER = "isSponsoredUser";
    public static final String LDAP_ATTR_GROUP_NAMES = PERUN_ATTR_GROUP_NAMES;
    public static final String LDAP_ATTR_INSTITUTIONS_COUNTRIES = PERUN_ATTR_INSTITUTIONS_COUNTRIES;
    public static final String LDAP_ATTR_PERUN_FACILITY_DN = "perunFacilityDn";
    public static final String LDAP_ATTR_ADMIN_OF_VO = "adminOfVo";
    public static final String LDAP_ATTR_ADMIN_OF_GROUP = "adminOfGroup";
    public static final String LDAP_ATTR_ADMIN_OF_FACILITY = "adminOfFacility";
    public static final String LDAP_ATTR_UUID = "uuid";
    public static final String LDAP_ATTR_USER_ELIGIBILITIES = PERUN_ATTR_USER_ELIGIBILITIES;

    //LDAP OBJECT CLASSES
    public static final String OBJECT_CLASS_TOP = "top";
    public static final String OBJECT_CLASS_PERUN_RESOURCE = "perunResource";
    public static final String OBJECT_CLASS_PERUN_GROUP = "perunGroup";
    public static final String OBJECT_CLASS_PERUN_FACILITY = "perunFacility";
    public static final String OBJECT_CLASS_ORGANIZATION = "organization";
    public static final String OBJECT_CLASS_PERUN_VO = "perunVO";
    public static final String OBJECT_CLASS_PERSON = "person";
    public static final String OBJECT_CLASS_ORGANIZATIONAL_PERSON = "organizationalPerson";
    public static final String OBJECT_CLASS_INET_ORG_PERSON = "inetOrgPerson";
    public static final String OBJECT_CLASS_PERUN_USER = "perunUser";
    public static final String OBJECT_CLASS_TEN_OPER_ENTRY = "tenOperEntry";
    public static final String OBJECT_CLASS_INET_USER = "inetUser";

    //LDAP ORGANIZATION UNITS
    public static final String ORGANIZATIONAL_UNIT_PEOPLE = "ou=People";

  }

  interface SingleValueExtractor<T> {
    public String getValue(T bean, Attribute... attributes);
  }

  interface MultipleValuesExtractor<T> {
    public String[] getValues(T bean, Attribute... attributes);
  }
}
