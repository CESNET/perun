package cz.metacentrum.perun.ldapc.initializer.utils;

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
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
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
public class Utils {

	private final static Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * Method to set last processed id for concrete consumer
	 *
	 * @param consumerName name of consumer to set
	 * @param lastProcessedId id to set
	 * @param perunPrincipal perunPrincipal for initializing RpcCaller
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	public static void setLastProcessedId(PerunPrincipal perunPrincipal, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException {
		RpcCaller rpcCaller = new RpcCallerImpl(perunPrincipal);
		Rpc.AuditMessagesManager.setLastProcessedId(rpcCaller, consumerName, lastProcessedId);
	}

	/**
	 * Return Writer for output for specific file with fileName.
	 * If fileName is null, return standard output
	 *
	 * @param fileName fileName or null (if stdout)
	 * @return writer with specific output
	 *
	 * @exception FileNotFoundException if defined file can't be created
	 */
	public static Writer getWriterForOutput(String fileName) throws FileNotFoundException {
		if (fileName != null) return new PrintWriter(fileName);
		else return new OutputStreamWriter(System.out);
	}

	/**
	 * Method generate all Vos to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 */
	public static void generateAllVosToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException {
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
			String dn = "dn: ";
			String desc = "description: ";
			String oc1 = "objectclass: top";
			String oc2 = "objectclass: organization";
			String oc3 = "objectclass: perunVO";
			String o = "o: ";
			String perunVoId = "perunVoId: ";
			perunVoId+= String.valueOf(vo.getId());
			o+= vo.getShortName();
			desc+= vo.getName();
			dn+= "perunVoId=" + vo.getId() + "," + ldapBase;
			writer.write(dn + '\n');
			writer.write(oc1 + '\n');
			writer.write(oc2 + '\n');
			writer.write(oc3 + '\n');
			writer.write(o + '\n');
			writer.write(perunVoId + '\n');
			writer.write(desc + '\n');
			//Generate all members in member groups of this vo and add them here (only members with status Valid)
			List<Member> validMembers = perun.getMembersManagerBl().getMembers(perunSession, vo, Status.VALID);
			for(Member m: validMembers) {
				writer.write("uniqueMember: perunUserId=" + m.getUserId() + "," + branchOuPeople + '\n');
			}
			writer.write('\n');
		}
	}

	/**
	 * Method generate all Resources to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 */
	public static void generateAllResourcesToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException {
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
				//Read facility attribute entityID and write it for the resource if exists
				Facility facility = null;
				try {
					facility = perun.getFacilitiesManagerBl().getFacilityById(perunSession, resource.getFacilityId());
				} catch (FacilityNotExistsException ex) {
					throw new InternalErrorException("Can't found facility of this resource " + resource, ex);
				}
				Attribute entityIDAttr = null;
				try {
					entityIDAttr = perun.getAttributesManagerBl().getAttribute(perunSession, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":entityID");
				} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
					//entityId attribute not exists or its assignment is wrong, use empty value
					entityIDAttr = new Attribute();
					log.error("EntityId attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
				}
				Attribute clientIDAttr = null;
				try {
					clientIDAttr = perun.getAttributesManagerBl().getAttribute(perunSession, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":OIDCClientID");
				} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
					//clientIDAttr attribute not exists or its assignment is wrong, use empty value
					clientIDAttr = new Attribute();
					log.error("clientIDAttr attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
				}

				String dn = "dn: ";
				String oc1 = "objectclass: top";
				String oc3 = "objectclass: perunResource";
				String cn = "cn: ";
				String perunVoId = "perunVoId: ";
				String perunFacilityId = "perunFacilityId: ";
				String perunResourceId = "perunResourceId: ";
				String description = "description:: ";
				String entityID = "entityID: ";
				String OIDCClientID = "OIDCClientID: ";

				perunVoId+= String.valueOf(resource.getVoId());
				perunFacilityId+= String.valueOf(resource.getFacilityId());
				perunResourceId+= String.valueOf(resource.getId());
				dn+= "perunResourceId=" + resource.getId() + ",perunVoId=" + resource.getVoId() + "," + ldapBase;
				cn+= resource.getName();
				String descriptionValue = resource.getDescription();
				if(descriptionValue != null) {
					if(descriptionValue.matches("^\\s*$")) descriptionValue = null;
				}
				writer.write(dn + '\n');
				writer.write(oc1 + '\n');
				writer.write(oc3 + '\n');
				writer.write(cn + '\n');
				writer.write(perunResourceId + '\n');
				if(descriptionValue != null) writer.write(description + Base64Coder.encodeString(descriptionValue) + '\n');
				writer.write(perunVoId + '\n');
				writer.write(perunFacilityId + '\n');
				if(entityIDAttr.getValue() != null) writer.write(entityID + (String) entityIDAttr.getValue() + '\n');
				if(clientIDAttr.getValue() != null) writer.write(OIDCClientID + (String) clientIDAttr.getValue() + '\n');
				//ADD resources which group is assigned to
				List<Group> associatedGroups = perun.getResourcesManagerBl().getAssignedGroups(perunSession, resource);
				for(Group g: associatedGroups) {
					writer.write("assignedGroupId: " + g.getId());
					writer.write('\n');
				}
				writer.write('\n');
			}
		}
	}

	/**
	 * Method generate all Groups to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 */
	public static void generateAllGroupsToWriter(PerunInitializer perunInitializer) throws InternalErrorException, IOException {
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
				String dn = "dn: ";
				String oc1 = "objectclass: top";
				String oc3 = "objectclass: perunGroup";
				String cn = "cn: ";
				String perunVoId = "perunVoId: ";
				String parentGroup = "perunParentGroup: ";
				String parentGroupId = "perunParentGroupId: ";
				String perunGroupId = "perunGroupId: ";
				String description = "description:: ";
				String perunUniqueGroupName = "perunUniqueGroupName: ";
				List<Member> members;
				members = perun.getGroupsManagerBl().getGroupMembers(perunSession, group, Status.VALID);
				perunGroupId+= String.valueOf(group.getId());
				perunVoId+= String.valueOf(group.getVoId());
				dn+= "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + "," + ldapBase;
				cn+= group.getName();
				perunUniqueGroupName+= vo.getShortName() + ":" + group.getName();
				String descriptionValue = group.getDescription();
				if(descriptionValue != null) {
					if(descriptionValue.matches("^\\s*$")) descriptionValue = null;
				}
				if(group.getParentGroupId() != null) {
					parentGroupId+= group.getParentGroupId();
					parentGroup+= "perunGroupId=" + group.getParentGroupId()+ ",perunVoId=" + group.getVoId() + "," + ldapBase;
				}
				List<Member> admins = new ArrayList<>();
				writer.write(dn + '\n');
				writer.write(oc1 + '\n');
				writer.write(oc3 + '\n');
				writer.write(cn + '\n');
				writer.write(perunUniqueGroupName + '\n');
				writer.write(perunGroupId + '\n');
				writer.write(perunVoId + '\n');
				if(descriptionValue != null) writer.write(description + Base64Coder.encodeString(descriptionValue) + '\n');
				if(group.getParentGroupId() != null) {
					writer.write(parentGroupId + '\n');
					writer.write(parentGroup + '\n');
				}
				//ADD Group Members
				for(Member m: members) {
					writer.write("uniqueMember: " + "perunUserId=" + m.getUserId() + "," + branchOuPeople);
					writer.write('\n');
				}
				//ADD resources which group is assigned to
				List<Resource> associatedResources;
				associatedResources = perun.getResourcesManagerBl().getAssignedResources(perunSession, group);
				for(Resource r: associatedResources) {
					writer.write("assignedToResourceId: " + r.getId());
					writer.write('\n');
				}
				writer.write('\n');
			}
		}
	}

	/**
	 * Method generate all Users to the text for using in LDIF.
	 * Write all these information to writer in perunInitializer object.
	 *
	 * @param perunInitializer need to be loaded to get all needed dependencies
	 *
	 * @throws InternalErrorException if some problem with initializer or objects in perun-core
	 * @throws IOException if some problem with writer
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	public static void generateAllUsersToWriter(PerunInitializer perunInitializer) throws IOException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
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
			String dn = "dn: ";
			String entryStatus = "entryStatus: active";
			String oc1 = "objectclass: top";
			String oc2 = "objectclass: person";
			String oc3 = "objectclass: organizationalPerson";
			String oc4 = "objectclass: inetOrgPerson";
			String oc5 = "objectclass: perunUser";
			String oc6 = "objectclass: tenOperEntry";
			String oc7 = "objectclass: inetUser";
			String sn = "sn: ";
			String cn = "cn: ";
			String givenName = "givenName: ";
			String perunUserId = "perunUserId: ";
			String mail = "mail: ";
			String preferredMail = "preferredMail: ";
			String o = "o: ";
			String isServiceUser = "isServiceUser: ";
			String isSponsoredUser = "isSponsoredUser: ";
			String userPassword = "userPassword: ";
			String phone = "telephoneNumber: ";
			String bonaFideStatus = "bonaFideStatus: ";
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

			Attribute attrPreferredMail = null;
			Attribute attrOrganization = null;
			Attribute attrVirtCertDNs = null;
			Attribute attrSchacHomeOrganizations = null;
			Attribute attrBonaFideStatus = null;
			Attribute attrEduPersonScopedAffiliations = null;
			Attribute attrLibraryIDs = null;
			Attribute attrPhone = null;
			Attribute attrGroupNames = null;
			//Check if there is any attribute missing and if so, log it and skip it
			try {
				attrPreferredMail = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Preferred mail attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrOrganization = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":organization");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Organization attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrVirtCertDNs = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Certificate DNs attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrSchacHomeOrganizations = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":schacHomeOrganizations");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Schac home organizations attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrBonaFideStatus = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":elixirBonaFideStatus");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Bona fide status attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrEduPersonScopedAffiliations = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":eduPersonScopedAffiliations");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Edu person scoped affilations attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrLibraryIDs = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":libraryIDs");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Library IDs attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrPhone = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_DEF + ":phone");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Phone attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}
			try {
				attrGroupNames = perun.getAttributesManagerBl().getAttribute(perunSession, user, AttributesManager.NS_USER_ATTR_VIRT + ":groupNames");
			} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
				log.error("Group names attribute is missing or it's assignment is wrong is missing. Attribute was skipped.", ex);
			}

			perunUserId+= String.valueOf(user.getId());
			dn+= "perunUserId=" + user.getId() + "," + branchOuPeople;
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			if(firstName == null || firstName.isEmpty()) firstName = "";
			else cn+= firstName + " ";
			if(lastName == null || lastName.isEmpty()) lastName = "N/A";
			sn+= lastName;
			cn+= lastName;
			if(user.isServiceUser()) isServiceUser+= "1";
			else isServiceUser+= "0";
			if(user.isSponsoredUser()) isSponsoredUser+= "1";
			else isSponsoredUser+= "0";
			if(firstName.isEmpty()) givenName = null;
			else givenName+= firstName;
			if(attrPreferredMail == null || attrPreferredMail.getValue() == null) mail = null;
			else mail+= (String) attrPreferredMail.getValue();
			if(attrPreferredMail == null || attrPreferredMail.getValue() == null) preferredMail =null;
			else preferredMail+= (String) attrPreferredMail.getValue();
			if(attrOrganization == null || attrOrganization.getValue() == null) o= null;
			else o+= (String) attrOrganization.getValue();
			if(attrPhone == null || attrPhone.getValue() == null || ((String) attrPhone.getValue()).isEmpty()) phone= null;
			else phone+= (String) attrPhone.getValue();
			if(attrBonaFideStatus == null || attrBonaFideStatus.getValue() == null || ((String) attrBonaFideStatus.getValue()).isEmpty()) bonaFideStatus = null;
			else bonaFideStatus+= (String) attrBonaFideStatus.getValue();
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
			writer.write(dn + '\n');
			writer.write(oc1 + '\n');
			writer.write(oc2 + '\n');
			writer.write(oc3 + '\n');
			writer.write(oc4 + '\n');
			writer.write(oc5 + '\n');
			writer.write(oc6 + '\n');
			writer.write(oc7 + '\n');
			writer.write(entryStatus + '\n');
			writer.write(sn + '\n');
			writer.write(cn + '\n');
			if(givenName != null) writer.write(givenName + '\n');
			writer.write(perunUserId + '\n');
			writer.write(isServiceUser + '\n');
			writer.write(isSponsoredUser + '\n');
			if(mail != null) writer.write(mail + '\n');
			if(preferredMail != null) writer.write(preferredMail + '\n');
			if(o != null) writer.write(o + '\n');
			if(phone != null) writer.write(phone + '\n');
			if(bonaFideStatus != null) writer.write(bonaFideStatus + '\n');
			if(certSubjectsWithoutPrefix != null && !certSubjectsWithoutPrefix.isEmpty()) {
				for(String s: certSubjectsWithoutPrefix) {
					writer.write("userCertificateSubject: " + s + '\n');
				}
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
			List<String> eduPersonScopedAffiliations = new ArrayList<>();
			if(attrEduPersonScopedAffiliations != null && attrEduPersonScopedAffiliations.getValue() != null) {
				eduPersonScopedAffiliations = (ArrayList) attrEduPersonScopedAffiliations.getValue();
			}
			if(eduPersonScopedAffiliations != null && !eduPersonScopedAffiliations.isEmpty()) {
				for(String affiliation : eduPersonScopedAffiliations) {
					writer.write("eduPersonScopedAffiliations: " + affiliation + '\n');
				}
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
			List<String> groupNames = new ArrayList<>();
			if(attrGroupNames != null && attrGroupNames.getValue() != null) {
				groupNames = (ArrayList) attrGroupNames.getValue();
			}
			if(groupNames != null && !groupNames.isEmpty()) {
				for(String groupName : groupNames) {
					writer.write("groupNames: " + groupName + '\n');
				}
			}
			//GET ALL USERS UIDs
			List<String> similarUids = perun.getAttributesManagerBl().getAllSimilarAttributeNames(perunSession, AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:");
			if(similarUids != null && !similarUids.isEmpty()) {
				for(String s: similarUids) {
					Attribute uidNamespace = perun.getAttributesManagerBl().getAttribute(perunSession, user, s);
					if(uidNamespace != null && uidNamespace.getValue() != null) {
						writer.write("uidNumber;x-ns-" + uidNamespace.getFriendlyNameParameter() + ": " + uidNamespace.getValue().toString() + '\n');
					}
				}
			}
			//GET ALL USERS LOGINs
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
			//GET ALL USERS EXTlogins FOR EVERY EXTSOURCE WITH TYPE EQUALS IDP
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
			//ADD MEMBEROF ATTRIBUTE TO WRITER
			for(String s: membersOf) {
				writer.write(s + '\n');
			}
			//ADD MEMBEROFPERUNVO ATTRIBUTE TO WRITER
			for(String s: membersOfPerunVo) {
				writer.write(s + '\n');
			}
			writer.write('\n');
		}

	}
}
