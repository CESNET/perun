package cz.metacentrum.perun.ldapc.initializer.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Base64Coder;
import cz.metacentrum.perun.ldapc.initializer.beans.PerunInitializer;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.rpclib.api.RpcCaller;
import cz.metacentrum.perun.rpclib.impl.RpcCallerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class with static utility methods
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class Utils implements cz.metacentrum.perun.ldapc.initializer.api.UtilsApi {

	private final static Logger log = LoggerFactory.getLogger(Utils.class);

	@Override
	public void initializeLDAPFromPerun(PerunInitializer perunInitializer, Boolean updateLastProcessedId) throws InternalErrorException {
		try {
			//get last message id before start of initializing
			int lastMessageBeforeInitializingData = perunInitializer.getPerunBl().getAuditer().getLastMessageId();
			System.err.println("Last message id before starting initializing: " + lastMessageBeforeInitializingData + '\n');

			try {
				this.generateAllVosToWriter(perunInitializer);
				this.generateAllGroupsToWriter(perunInitializer);
				this.generateAllResourcesToWriter(perunInitializer);
				this.generateAllUsersToWriter(perunInitializer);
			} catch (IOException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				System.err.println("Problem with initializing users, there is an attribute which probably not exists.");
				throw new InternalErrorException(ex);
			}

			//get last message id after initializing
			int lastMessageAfterInitializingData = perunInitializer.getPerunBl().getAuditer().getLastMessageId();
			System.err.println("Last message id after initializing: " + lastMessageAfterInitializingData + '\n');

			//This is the only operation of WRITING to the DB
			//Call RPC-LIB for this purpose
			if(updateLastProcessedId) {
				try {
					this.setLastProcessedId(perunInitializer.getPerunPrincipal(), perunInitializer.getConsumerName(), lastMessageAfterInitializingData);
				} catch (InternalErrorException | PrivilegeException ex) {
					System.err.println("Can't set last processed ID because of lack of privileges or some Internal error.");
					throw new InternalErrorException(ex);
				}
			}
		} finally {
			//Close writer if already opened
			if(perunInitializer != null) {
				try {
					perunInitializer.closeWriter();
				} catch(IOException ex) {
					System.err.println("Can't close writer by normal way.");
					throw new InternalErrorException(ex);
				}
			}
		}

		System.err.println("Generating of initializing LDIF done without error!");
	}

	@Override
	public void setLastProcessedId(PerunPrincipal perunPrincipal, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException {
		RpcCaller rpcCaller = new RpcCallerImpl(perunPrincipal);
		Rpc.AuditMessagesManager.setLastProcessedId(rpcCaller, consumerName, lastProcessedId);
	}

	@Override
	public void generateAllVosToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException {
		//Load basic variables
		if(perunInitializer == null) throw new InternalErrorException("PerunInitializer must be loaded before using in generating methods!");
		PerunSession perunSession = perunInitializer.getPerunSession();
		PerunBl perun = perunInitializer.getPerunBl();
		String ldapBase = perunInitializer.getLdapBase();
		String branchOuPeople = "ou=People," + ldapBase;
		BufferedWriter writer = perunInitializer.getOutputWriter();

		//Get list of all vos
		List<Vo> vos = perun.getVosManagerBl().getVos(perunSession);

		//For every vos get needed information and write them to the writer
		for(Vo vo: vos) {
			//object DN
			String dn = "dn: ";
			dn+= "perunVoId=" + vo.getId() + "," + ldapBase;
			writer.write(dn + '\n');

			//vo description
			String desc = "description: ";
			desc+= vo.getName();
			writer.write(desc + '\n');

			//all object classes
			String oc1 = "objectclass: top";
			writer.write(oc1 + '\n');
			String oc2 = "objectclass: organization";
			writer.write(oc2 + '\n');
			String oc3 = "objectclass: perunVO";
			writer.write(oc3 + '\n');

			//vo short name
			String o = "o: ";
			o+= vo.getShortName();
			writer.write(o + '\n');

			//vo id in perun
			String perunVoId = "perunVoId: ";
			perunVoId+= String.valueOf(vo.getId());
			writer.write(perunVoId + '\n');

			//all dns of valid members of this vo
			List<Member> validMembers = perun.getMembersManagerBl().getMembers(perunSession, vo, Status.VALID);
			for(Member m: validMembers) {
				writer.write("uniqueMember: perunUserId=" + m.getUserId() + "," + branchOuPeople + '\n');
			}

			//mandatory delimiter (empty linen between two records)
			writer.write('\n');
		}
	}

	@Override
	public void generateAllResourcesToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException {
		//Load basic variables
		if(perunInitializer == null) throw new InternalErrorException("PerunInitializer must be loaded before using in generating methods!");
		PerunSession perunSession = perunInitializer.getPerunSession();
		PerunBl perun = perunInitializer.getPerunBl();
		String ldapBase = perunInitializer.getLdapBase();
		BufferedWriter writer = perunInitializer.getOutputWriter();

		//first get all Vos
		List<Vo> vos = perun.getVosManagerBl().getVos(perunSession);

		//Then from every Vo get all assigned resources and write their data to the writer
		for(Vo vo: vos) {
			List<Resource> resources;
			resources = perun.getResourcesManagerBl().getResources(perunSession, vo);
			for(Resource resource: resources) {
				Facility facility = null;
				try {
					facility = perun.getFacilitiesManagerBl().getFacilityById(perunSession, resource.getFacilityId());
				} catch (FacilityNotExistsException ex) {
					throw new InternalErrorException("Can't found facility of this resource " + resource, ex);
				}

				//object DN
				String dn = "dn: ";
				dn+= "perunResourceId=" + resource.getId() + ",perunVoId=" + resource.getVoId() + "," + ldapBase;
				writer.write(dn + '\n');

				//all object classes
				String oc1 = "objectclass: top";
				writer.write(oc1 + '\n');
				String oc2 = "objectclass: perunResource";
				writer.write(oc2 + '\n');

				//common name
				String cn = "cn: ";
				cn+= resource.getName();
				writer.write(cn + '\n');

				//assigned vo id in Perun
				String perunVoId = "perunVoId: ";
				perunVoId+= String.valueOf(resource.getVoId());
				writer.write(perunVoId + '\n');

				//assigned facility id in Perun
				String perunFacilityId = "perunFacilityId: ";
				perunFacilityId+= String.valueOf(resource.getFacilityId());
				writer.write(perunFacilityId + '\n');

				//resource id in Perun
				String perunResourceId = "perunResourceId: ";
				perunResourceId+= String.valueOf(resource.getId());
				writer.write(perunResourceId + '\n');

				//resource description
				String description = "description:: ";
				String descriptionValue = resource.getDescription();
				if(descriptionValue != null) {
					if(descriptionValue.matches("^\\s*$")) descriptionValue = null;
				}
				if(descriptionValue != null) writer.write(description + Base64Coder.encodeString(descriptionValue) + '\n');

				//entityID
				String entityID = "entityID: ";
				Attribute entityIDAttr = null;
				try {
					entityIDAttr = perun.getAttributesManagerBl().getAttribute(perunSession, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":entityID");
				} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
					//entityId attribute not exists or its assignment is wrong, use empty value
					entityIDAttr = new Attribute();
					log.error("EntityId attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
				}
				if(entityIDAttr.getValue() != null) writer.write(entityID + (String) entityIDAttr.getValue() + '\n');

				//oidcClientID
				String OIDCClientID = "OIDCClientID: ";
				Attribute clientIDAttr = null;
				try {
					clientIDAttr = perun.getAttributesManagerBl().getAttribute(perunSession, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":OIDCClientID");
				} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
					//clientIDAttr attribute not exists or its assignment is wrong, use empty value
					clientIDAttr = new Attribute();
					log.error("clientIDAttr attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
				}
				if(clientIDAttr.getValue() != null) writer.write(OIDCClientID + (String) clientIDAttr.getValue() + '\n');


				//all perun ids of assigned groups to this resource
				List<Group> assignedGroups = perun.getResourcesManagerBl().getAssignedGroups(perunSession, resource);
				for(Group g: assignedGroups) {
					writer.write("assignedGroupId: " + g.getId());
					writer.write('\n');
				}

				//mandatory delimiter (empty linen between two records)
				writer.write('\n');
			}
		}
	}

	@Override
	public void generateAllGroupsToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException {
		//Load basic variables
		if(perunInitializer == null) throw new InternalErrorException("PerunInitializer must be loaded before using in generating methods!");
		PerunSession perunSession = perunInitializer.getPerunSession();
		PerunBl perun = perunInitializer.getPerunBl();
		String ldapBase = perunInitializer.getLdapBase();
		String branchOuPeople = "ou=People," + ldapBase;
		BufferedWriter writer = perunInitializer.getOutputWriter();

		//First get all vos
		List<Vo> vos = perun.getVosManagerBl().getVos(perunSession);

		//Then from all vos get all assigned groups and generate data about them to the writer
		for(Vo vo: vos) {
			List<Group> groups;
			groups = perun.getGroupsManagerBl().getGroups(perunSession, vo);

			for(Group group: groups) {
				//object DN
				String dn = "dn: ";
				dn+= "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + "," + ldapBase;
				writer.write(dn + '\n');

				//all object classes
				String oc1 = "objectclass: top";
				writer.write(oc1 + '\n');
				String oc2 = "objectclass: perunGroup";
				writer.write(oc2 + '\n');

				//common name
				String cn = "cn: ";
				cn+= group.getName();

				//associated vo id in Perun
				String perunVoId = "perunVoId: ";
				perunVoId+= String.valueOf(group.getVoId());

				//dn of parent group and id of parent group from Perun
				String parentGroup = "perunParentGroup: ";
				String parentGroupId = "perunParentGroupId: ";
				if(group.getParentGroupId() != null) {
					parentGroupId+= group.getParentGroupId();
					parentGroup+= "perunGroupId=" + group.getParentGroupId()+ ",perunVoId=" + group.getVoId() + "," + ldapBase;
				}

				//group id in Perun
				String perunGroupId = "perunGroupId: ";
				perunGroupId+= String.valueOf(group.getId());

				//unique name of group (vo_short_name:group_name)
				String perunUniqueGroupName = "perunUniqueGroupName: ";
				perunUniqueGroupName+= vo.getShortName() + ":" + group.getName();

				//group description
				String description = "description:: ";
				String descriptionValue = group.getDescription();
				if(descriptionValue != null) {
					if(descriptionValue.matches("^\\s*$")) descriptionValue = null;
				}


				//all DNs of valid members of the group
				List<Member> members;
				members = perun.getGroupsManagerBl().getGroupMembers(perunSession, group, Status.VALID);
				writer.write(cn + '\n');
				writer.write(perunUniqueGroupName + '\n');
				writer.write(perunGroupId + '\n');
				writer.write(perunVoId + '\n');
				if(descriptionValue != null) writer.write(description + Base64Coder.encodeString(descriptionValue) + '\n');
				if(group.getParentGroupId() != null) {
					writer.write(parentGroupId + '\n');
					writer.write(parentGroup + '\n');
				}
				for(Member m: members) {
					writer.write("uniqueMember: " + "perunUserId=" + m.getUserId() + "," + branchOuPeople);
					writer.write('\n');
				}


				//all ids of resources where group is assigned
				List<Resource> associatedResources;
				associatedResources = perun.getResourcesManagerBl().getAssignedResources(perunSession, group);
				for(Resource r: associatedResources) {
					writer.write("assignedToResourceId: " + r.getId());
					writer.write('\n');
				}

				//mandatory delimiter (empty linen between two records)
				writer.write('\n');
			}
		}
	}

	@Override
	public void generateAllUsersToWriter(PerunInitializer perunInitializer) throws IOException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		//Load basic variables
		if(perunInitializer == null) throw new InternalErrorException("PerunInitializer must be loaded before using in generating methods!");
		PerunSession perunSession = perunInitializer.getPerunSession();
		PerunBl perun = perunInitializer.getPerunBl();
		String ldapBase = perunInitializer.getLdapBase();
		String branchOuPeople = "ou=People," + ldapBase;
		String loginNamespace = perunInitializer.getLoginNamespace();
		BufferedWriter writer = perunInitializer.getOutputWriter();

		List<User> users = perun.getUsersManagerBl().getUsers(perunSession);

		for(User user: users) {
			//object DN
			String dn = "dn: ";
			dn+= "perunUserId=" + user.getId() + "," + branchOuPeople;
			writer.write(dn + '\n');

			//all object classes
			String oc1 = "objectclass: top";
			writer.write(oc1 + '\n');
			String oc2 = "objectclass: person";
			writer.write(oc2 + '\n');
			String oc3 = "objectclass: organizationalPerson";
			writer.write(oc3 + '\n');
			String oc4 = "objectclass: inetOrgPerson";
			writer.write(oc4 + '\n');
			String oc5 = "objectclass: perunUser";
			writer.write(oc5 + '\n');
			String oc6 = "objectclass: tenOperEntry";
			writer.write(oc6 + '\n');
			String oc7 = "objectclass: inetUser";
			writer.write(oc7 + '\n');

			//default entry status
			String entryStatus = "entryStatus: active";
			writer.write(entryStatus + '\n');

			//common name of user
			String cn = "cn: ";
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			if(firstName == null || firstName.isEmpty()) firstName = "";
			else cn+= firstName + " ";
			if(lastName == null || lastName.isEmpty()) lastName = "N/A";
			cn+= lastName;
			writer.write(cn + '\n');

			//surname of user
			String sn = "sn: ";
			sn+= lastName;
			writer.write(sn + '\n');

			//given name of user
			String givenName = "givenName: ";
			if(firstName.isEmpty()) givenName = null;
			else givenName+= firstName;
			if(givenName != null) writer.write(givenName + '\n');

			//user id from perun
			String perunUserId = "perunUserId: ";
			perunUserId+= String.valueOf(user.getId());
			writer.write(perunUserId + '\n');

			//is user a service user
			String isServiceUser = "isServiceUser: ";
			if(user.isServiceUser()) isServiceUser+= "1";
			else isServiceUser+= "0";
			writer.write(isServiceUser + '\n');

			//is user sponsored
			String isSponsoredUser = "isSponsoredUser: ";
			if(user.isSponsoredUser()) isSponsoredUser+= "1";
			else isSponsoredUser+= "0";
			writer.write(isSponsoredUser + '\n');

			//preferred mail
			String preferredMail = "preferredMail: ";
			Attribute attrPreferredMail = null;
			try {
				attrPreferredMail = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Preferred mail attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			if(attrPreferredMail == null || attrPreferredMail.getValue() == null) preferredMail =null;
			else preferredMail+= (String) attrPreferredMail.getValue();
			if(preferredMail != null) writer.write(preferredMail + '\n');

			//mail (same value as preferred mail)
			String mail = "mail: ";
			if(attrPreferredMail == null || attrPreferredMail.getValue() == null) mail = null;
			else mail+= (String) attrPreferredMail.getValue();
			if(mail != null) writer.write(mail + '\n');

			//organization
			String o = "o: ";
			Attribute attrOrganization = null;
			try {
				attrOrganization = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":organization");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Organization attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			if(attrOrganization == null || attrOrganization.getValue() == null) o= null;
			else o+= (String) attrOrganization.getValue();
			if(o != null) writer.write(o + '\n');

			//phone
			String phone = "telephoneNumber: ";
			Attribute attrPhone = null;
			try {
				attrPhone = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":phone");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Phone attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			if(attrPhone == null || attrPhone.getValue() == null || ((String) attrPhone.getValue()).isEmpty()) phone= null;
			else phone+= (String) attrPhone.getValue();
			if(phone != null) writer.write(phone + '\n');

			//bona fide status
			String bonaFideStatus = "bonaFideStatus: ";
			Attribute attrBonaFideStatus = null;
			try {
				attrBonaFideStatus = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":elixirBonaFideStatus");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Bona fide status attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			if(attrBonaFideStatus == null || attrBonaFideStatus.getValue() == null || ((String) attrBonaFideStatus.getValue()).isEmpty()) bonaFideStatus = null;
			else bonaFideStatus+= (String) attrBonaFideStatus.getValue();
			if(bonaFideStatus != null) writer.write(bonaFideStatus + '\n');

			//all certificates subjects
			Attribute attrVirtCertDNs = null;
			try {
				attrVirtCertDNs = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Certificate DNs attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			Map<String, String> certDNs = null;
			Set<String> certSubjectsWithPrefix = null;
			Set<String> certSubjectsWithoutPrefix = new HashSet<>();
			if(attrVirtCertDNs != null && attrVirtCertDNs.getValue() != null) {
				certDNs = (Map) attrVirtCertDNs.getValue();
				certSubjectsWithPrefix = certDNs.keySet();
				for(String certSubject: certSubjectsWithPrefix) {
					certSubjectsWithoutPrefix.add(certSubject.replaceFirst("^[0-9]+[:]", ""));
				}
			}
			if(certSubjectsWithoutPrefix != null && !certSubjectsWithoutPrefix.isEmpty()) {
				for(String s: certSubjectsWithoutPrefix) {
					writer.write("userCertificateSubject: " + s + '\n');
				}
			}

			//all schac home organizations
			Attribute attrSchacHomeOrganizations = null;
			try {
				attrSchacHomeOrganizations = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":schacHomeOrganizations");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Schac home organizations attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			List<String> schacHomeOrganizations = new ArrayList<>();
			if(attrSchacHomeOrganizations != null && attrSchacHomeOrganizations.getValue() != null) {
				schacHomeOrganizations = (ArrayList) attrSchacHomeOrganizations.getValue();
			}
			if(schacHomeOrganizations != null && !schacHomeOrganizations.isEmpty()) {
				for(String organization : schacHomeOrganizations) {
					writer.write("schacHomeOrganizations: " + organization + '\n');
				}
			}

			//all edu person scoped affilations
			Attribute attrEduPersonScopedAffiliations = null;
			try {
				attrEduPersonScopedAffiliations = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":eduPersonScopedAffiliations");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Edu person scoped affilations attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			List<String> eduPersonScopedAffiliations = new ArrayList<>();
			if(attrEduPersonScopedAffiliations != null && attrEduPersonScopedAffiliations.getValue() != null) {
				eduPersonScopedAffiliations = (ArrayList) attrEduPersonScopedAffiliations.getValue();
			}
			if(eduPersonScopedAffiliations != null && !eduPersonScopedAffiliations.isEmpty()) {
				for(String affiliation : eduPersonScopedAffiliations) {
					writer.write("eduPersonScopedAffiliations: " + affiliation + '\n');
				}
			}

			//all library ids
			Attribute attrLibraryIDs = null;
			try {
				attrLibraryIDs = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":libraryIDs");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Library IDs attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			List<String> libraryIDs = new ArrayList<>();
			if(attrLibraryIDs != null && attrLibraryIDs.getValue() != null) {
				libraryIDs = (ArrayList) attrLibraryIDs.getValue();
			}
			if(libraryIDs != null && !libraryIDs.isEmpty()) {
				for(String id : libraryIDs) {
					writer.write("libraryIDs: " + id + '\n');
				}
			}

			//all group names
			Attribute attrGroupNames = null;
			try {
				attrGroupNames = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":groupNames");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Group names attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			List<String> groupNames = new ArrayList<>();
			if(attrGroupNames != null && attrGroupNames.getValue() != null) {
				groupNames = (ArrayList) attrGroupNames.getValue();
			}
			if(groupNames != null && !groupNames.isEmpty()) {
				for(String groupName : groupNames) {
					writer.write("groupNames: " + groupName + '\n');
				}
			}

			//all institutions countries
			Attribute attrInstitutionsCountries = null;
			try {
				attrInstitutionsCountries = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":institutionsCountries");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Institutions countries attribute is missing or it's assignment is wrong. Attribute was skipped.", ex);
			}
			List<String> institutionsCountries = new ArrayList<>();
			if(attrInstitutionsCountries != null && attrInstitutionsCountries.getValue() != null) {
				institutionsCountries = (ArrayList) attrInstitutionsCountries.getValue();
			}
			if(institutionsCountries != null && !institutionsCountries.isEmpty()) {
				for(String institutionsCountry : institutionsCountries) {
					writer.write("institutionsCountries: " + institutionsCountry + '\n');
				}
			}

			//all uids
			List<String> similarUids = perun.getAttributesManagerBl().getAllSimilarAttributeNames(perunSession, AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:");
			if(similarUids != null && !similarUids.isEmpty()) {
				for(String s: similarUids) {
					Attribute uidNamespace = perun.getAttributesManagerBl().getAttribute(perunSession, user, s);
					if(uidNamespace != null && uidNamespace.getValue() != null) {
						writer.write("uidNumber;x-ns-" + uidNamespace.getFriendlyNameParameter() + ": " + uidNamespace.getValue().toString() + '\n');
					}
				}
			}

			//all logins
			String userPassword = "userPassword: ";
			List<String> similarLogins = perun.getAttributesManagerBl().getAllSimilarAttributeNames(perunSession, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:");
			if(similarLogins != null && !similarLogins.isEmpty()) {
				for(String s: similarLogins) {
					Attribute loginNamespaceAttribute = perun.getAttributesManagerBl().getAttribute(perunSession, user, s);
					if(loginNamespace != null && loginNamespaceAttribute.getValue() != null) {
						String loginNamespaceAttributeValue = (String) loginNamespaceAttribute.getValue();
						writer.write("login;x-ns-" + loginNamespaceAttribute.getFriendlyNameParameter() + ": " + loginNamespaceAttributeValue + '\n');
						if(loginNamespaceAttribute.getFriendlyNameParameter().toLowerCase().equals(loginNamespace.toLowerCase())) {
							writer.write(userPassword + "{SASL}" + loginNamespaceAttributeValue  + '@' + loginNamespace + '\n');
						}
					}
				}
			}

			//all edu person principal names
			List<UserExtSource> userExtSources = perun.getUsersManagerBl().getUserExtSources(perunSession, user);
			List<String> extLogins = new ArrayList<>();
			for(UserExtSource ues: userExtSources) {
				if(ues != null && ues.getExtSource() != null) {
					String type = ues.getExtSource().getType();
					if(type != null) {
						if(type.equals(ExtSourcesManager.EXTSOURCE_IDP)) {
							String extLogin;
							extLogin = ues.getLogin();
							if(extLogin == null) extLogin = "";
							writer.write("eduPersonPrincipalNames: " + extLogin + '\n');
						}
					}
				}
			}

			//all groups memberships
			List<String> membersOf = new ArrayList<>();
			List<Member> members;
			Set<String> membersOfPerunVo = new HashSet<>();
			members = perun.getMembersManagerBl().getMembersByUser(perunSession, user);
			for(Member member: members) {
				if(member.getStatus().equals(Status.VALID)) {
					membersOfPerunVo.add("memberOfPerunVo: " + member.getVoId());
					List<Group> groups;
					groups = perun.getGroupsManagerBl().getAllMemberGroups(perunSession, member);
					for(Group group: groups) {
						membersOf.add("memberOf: " + "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + "," + ldapBase);
					}
				}
			}
			for(String s: membersOf) {
				writer.write(s + '\n');
			}

			//all vos memberships
			for(String s: membersOfPerunVo) {
				writer.write(s + '\n');
			}

			//mandatory delimiter (empty linen between two records)
			writer.write('\n');
		}
	}
}
