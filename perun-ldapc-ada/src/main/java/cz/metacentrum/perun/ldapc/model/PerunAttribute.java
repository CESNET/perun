package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface PerunAttribute<T extends PerunBean> {

	public interface PerunAttributeNames {
	
		//PERUN ATTRIBUTES NAMES
		public static final String perunAttrPreferredMail = "preferredMail";
		public static final String perunAttrMail = "mail";
		public static final String perunAttrOrganization = "organization";
		public static final String perunAttrPhone = "phone";
		public static final String perunAttrUserCertDNs = "userCertDNs";
		public static final String perunAttrBonaFideStatus = "elixirBonaFideStatus";
		public static final String perunAttrSchacHomeOrganizations = "schacHomeOrganizations";
		public static final String perunAttrEduPersonScopedAffiliations = "eduPersonScopedAffiliations";
		public static final String perunAttrLibraryIDs = "libraryIDs";
		public static final String perunAttrEntityID = "entityID";
		public static final String perunAttrClientID = "OIDCClientID";
		public static final String perunAttrGroupNames = "groupNames";
		public static final String perunAttrInstitutionsCountries = "institutionsCountries";
	
		//LDAP ATTRIBUTES NAMES
		public static final String ldapAttrAssignedToResourceId = "assignedToResourceId";
		public static final String ldapAttrAssignedGroupId = "assignedGroupId";
		public static final String ldapAttrDescription = "description";
		public static final String ldapAttrCommonName = "cn";
		public static final String ldapAttrPerunUniqueGroupName= "perunUniqueGroupName";
		public static final String ldapAttrEduPersonPrincipalNames = "eduPersonPrincipalNames";
		public static final String ldapAttrPreferredMail = perunAttrPreferredMail;
		public static final String ldapAttrMail = perunAttrMail;
		public static final String ldapAttrOrganization = "o";
		public static final String ldapAttrTelephoneNumber = "telephoneNumber";
		public static final String ldapAttrUserCertDNs = "userCertificateSubject";
		public static final String ldapAttrBonaFideStatus = "bonaFideStatus";
		public static final String ldapAttrSchacHomeOrganizations = perunAttrSchacHomeOrganizations;
		public static final String ldapAttrEduPersonScopedAffiliations = perunAttrEduPersonScopedAffiliations;
		public static final String ldapAttrLibraryIDs = perunAttrLibraryIDs;
		public static final String ldapAttrUidNumber = "uidNumber;x-ns-";
		public static final String ldapAttrLogin = "login;x-ns-";
		public static final String ldapAttrUserPassword = "userPassword";
		public static final String ldapAttrSurname = "sn";
		public static final String ldapAttrGivenName = "givenName";
		public static final String ldapAttrEntityID = perunAttrEntityID;
		public static final String ldapAttrClientID = perunAttrClientID;
		public static final String ldapAttrObjectClass = "objectClass";
		public static final String ldapAttrPerunVoId = "perunVoId";
		public static final String ldapAttrPerunFacilityId = "perunFacilityId";
		public static final String ldapAttrPerunUserId = "perunUserId";
		public static final String ldapAttrPerunGroupId = "perunGroupId";
		public static final String ldapAttrPerunResourceId = "perunResourceId";
		public static final String ldapAttrPerunParentGroup = "perunParentGroup";
		public static final String ldapAttrPerunParentGroupId = "perunParentGroupId";
		public static final String ldapAttrMemberOf = "memberOf";
		public static final String ldapAttrUniqueMember = "uniqueMember";
		public static final String ldapAttrMemberOfPerunVo = "memberOfPerunVo";
		public static final String ldapAttrEntryStatus = "entryStatus";
		public static final String ldapAttrIsServiceUser = "isServiceUser";
		public static final String ldapAttrIsSponsoredUser = "isSponsoredUser";
		public static final String ldapAttrGroupNames = perunAttrGroupNames;
		public static final String ldapAttrInstitutionsCountries = perunAttrInstitutionsCountries;
	
		//LDAP OBJECT CLASSES
		public static final String objectClassTop = "top";
		public static final String objectClassPerunResource = "perunResource";
		public static final String objectClassPerunGroup = "perunGroup";
		public static final String objectClassOrganization = "organization";
		public static final String objectClassPerunVO = "perunVO";
		public static final String objectClassPerson = "person";
		public static final String objectClassOrganizationalPerson = "organizationalPerson";
		public static final String objectClassInetOrgPerson = "inetOrgPerson";
		public static final String objectClassPerunUser = "perunUser";
		public static final String objectClassTenOperEntry = "tenOperEntry";
		public static final String objectClassInetUser = "inetUser";
	
		//LDAP ORGANIZATION UNITS
		public static final String organizationalUnitPeople = "ou=People";
	
	}

	interface SingleValueExtractor<T> {
		public String getValue(T bean, Attribute... attributes) throws InternalErrorException;
	}

	interface MultipleValuesExtractor<T> {
		public String[] getValues(T bean, Attribute... attributes) throws InternalErrorException;
	}

	public static final boolean REQUIRED = true;
	public static final boolean OPTIONAL = false;

	public static final boolean MULTIVALUED = true;
	public static final boolean SINGLE = false;
	
	public boolean isRequired();

	public boolean isMultiValued();

	public String getName();
	
	public String getName(AttributeDefinition attr);
	
	public String getBaseName();

	public boolean hasValue(T bean, Attribute... attributes) throws InternalErrorException;

	public String getValue(T bean, Attribute... attributes) throws InternalErrorException;

	public String[] getValues(T bean, Attribute... attributes) throws InternalErrorException;

	SingleValueExtractor<T> getSingleValueExtractor();

	void setSingleValueExtractor(SingleValueExtractor<T> valueExtractor);

	MultipleValuesExtractor<T> getMultipleValuesExtractor();

	void setMultipleValuesExtractor(MultipleValuesExtractor<T> valueExtractor);

	public boolean requiresAttributeBean();
}
