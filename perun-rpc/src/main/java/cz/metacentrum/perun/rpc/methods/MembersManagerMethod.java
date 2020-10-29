package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public enum MembersManagerMethod implements ManagerMethod {

	/*#
	 * Deletes only member data appropriated by member id.
	 *
	 * @param member int Member <code>id</code>
	 */
	deleteMember {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getMembersManager().deleteMember(ac.getSession(), ac.getMemberById(parms.readInt("member")));
			return null;
		}
	},

	/*#
	 * Delete members with given ids. It is possible to delete members from multiple vos.
	 *
	 * @param member int Member <code>id</code>
	 */
	deleteMembers {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			int[] ids = parms.readArrayOfInts("members");
			List<Member> members = new ArrayList<>(ids.length);
			for (int id : ids) {
				members.add(ac.getMemberById(id));
			}
			ac.getMembersManager().deleteMembers(ac.getSession(), members);
			return null;
		}
	},

	/*#
	 * Creates a new member from candidate which is prepared for creating specificUser.
	 *
	 * In list specificUserOwners can't be specificUser, only normal users are allowed.
	 * <strong>This method runs asynchronously</strong>
	 *
	 * @param vo int VO <code>id</code>
	 * @param candidate Candidate prepared future specificUser
	 * @param specificUserType String Type of user: SERVICE or SPONSORED
	 * @param specificUserOwners List<User> List of users who own specificUser (can't be empty or contain specificUser)
	 * @return Member newly created member (of specific User)
	 */
	/*#
	 * Creates a new member from candidate which is prepared for creating specificUser.
	 *
	 * This method also add user to all groups in list.
	 * In list specificUserOwners can't be specificUser, only normal users are allowed.
	 * Empty list of groups is ok, the behavior is then same like in the method without list of groups.
	 * <strong>This method runs asynchronously</strong>
	 *
	 * @param vo int VO ID
	 * @param candidate Candidate prepared future specificUser
	 * @param specificUserType String Type of user: SERVICE or SPONSORED
	 * @param specificUserOwners List<User> List of users who own specificUser (can't be empty or contain specificUser)
	 * @param groups List<Group> List of groups where member need to be add too (must be from the same vo)
	 * @return Member newly created member (of specific User)
	 */
	createSpecificMember {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("groups") ) {
				return ac.getMembersManager().createSpecificMember(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.read("candidate", Candidate.class),
						parms.readList("specificUserOwners", User.class),
						SpecificUserType.valueOf(parms.readString("specificUserType")),
						parms.readList("groups", Group.class));
			} else {
				return ac.getMembersManager().createSpecificMember(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.read("candidate", Candidate.class),
						parms.readList("specificUserOwners", User.class),
						SpecificUserType.valueOf(parms.readString("specificUserType")));
			}
		}
	},

	/*#
	 * Creates a new sponsored member in a given VO and namespace.
	 *
	 * Can be called either by a user with role SPONSOR, in that case the user becomes the sponsor,
	 * or by a user with role REGISTRAR that must specify the sponsoring user using ID.
	 *
	 * @param guestName String identification of sponsored account, e.g. "John Doe" or "conference member 1"
	 * @param password String password
	 * @param vo int VO ID
	 * @param namespace String namespace selecting remote system for storing the password
	 * @param sponsor int sponsor's ID
	 * @param email (optional) preferred email that will be set to the created user. If no email
	 *              is provided, "no-reply@muni.cz" is used.
	 * @return RichMember newly created sponsored member
	 */
	/*#
	 * Creates a new sponsored member in a given VO and namespace.
	 *
	 * Can be called either by a user with role SPONSOR, in that case the user becomes the sponsor,
	 * or by a user with role REGISTRAR that must specify the sponsoring user using ID.
	 *
	 * @param firstName first name - mandatory
	 * @param lastName last name - mandatory
	 * @param titleBefore titles before the name - optionally
	 * @param titleAfter titles after the name - optionally
	 * @param password String password
	 * @param vo int VO ID
	 * @param namespace String namespace selecting remote system for storing the password
	 * @param sponsor int sponsor's ID
	 * @param email (optional) preferred email that will be set to the created user. If no email
	 *              is provided, "no-reply@muni.cz" is used.
	 * @param validityTo (Optional) String the last day, when the sponsorship is active, yyyy-mm-dd format.
	 * @return RichMember newly created sponsored member
	 */
	createSponsoredMember {
		@Override
		public RichMember call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			String password = params.readString("password");
			Vo vo =  ac.getVoById(params.readInt("vo"));
			String namespace = params.readString("namespace");
			String email = null;
			if (params.contains("email")) {
				email = params.readString("email");
			}
			if (email != null && !Utils.emailPattern.matcher(email).matches()) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Email has an invalid format.");
			}
			User sponsor = null;
			LocalDate validityTo = null;
			if (params.contains("validityTo")) {
				validityTo = params.readLocalDate("validityTo");
			}
			if(params.contains("sponsor")) {
				sponsor = ac.getUserById(params.readInt("sponsor"));
			}
			Map<String, String> name = new HashMap<>();
			if (params.contains("guestName")) {
				name.put("guestName", params.readString("guestName"));
			} else if (params.contains("firstName") && params.contains("lastName")) {
				name.put("firstName", params.readString("firstName"));
				name.put("lastName", params.readString("lastName"));
				if (params.contains("titleBefore")) name.put("titleBefore", params.readString("titleBefore"));
				if (params.contains("titleAfter")) name.put("titleAfter", params.readString("titleAfter"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "Missing value. Either 'guestName' or ('firstName' and 'lastName') must be sent.");
			}
			return ac.getMembersManager().createSponsoredMember(ac.getSession(), vo, namespace, name, password, email, sponsor, validityTo);
		}
	},

	/*#
	 * Creates a sponsored membership for the given user.
	 *
	 * Can be called with specific sponsor. If the parameter sponsor is null, then the user
	 * which called this method will be set as a sponsor.
	 *
	 * @param vo int id of virtual organization
	 * @param userToBeSponsored int id of user, that will be sponsored by sponsor
	 * @param namespace String used for selecting external system in which guest user account will be created
	 * @param password String password
	 * @param sponsor int id of sponsoring user
	 * @param validityTo (Optional) String the last day, when the sponsorship is active, yyyy-mm-dd format.
	 * @return RichMember sponsored member
	 */
	/*#
	 * Creates a sponsored membership for the given user.
	 *
	 * Can be called with specific sponsor. If the parameter sponsor is null, then the user
	 * which called this method will be set as a sponsor.
	 *
	 * @param vo int id of virtual organization
	 * @param userToBeSponsored int id of user, that will be sponsored by sponsor
	 * @param namespace String used for selecting external system in which guest user account will be created
	 * @param password String password
	 * @param validityTo (Optional) String the last day, when the sponsorship is active, yyyy-mm-dd format.
	 * @return RichMember sponsored member
	 */
	setSponsoredMember {
		@Override
		public RichMember call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			String password = params.readString("password");
			Vo vo =  ac.getVoById(params.readInt("vo"));
			String namespace = params.readString("namespace");
			LocalDate validityTo = null;
			if (params.contains("validityTo")) {
				validityTo = params.readLocalDate("validityTo");
			}
			User sponsor = null;
			if(params.contains("sponsor")) {
				sponsor = ac.getUserById(params.readInt("sponsor"));
			}
			User userToBeSponsored;
			if(params.contains("userToBeSponsored")) {
				userToBeSponsored = ac.getUserById(params.readInt("userToBeSponsored"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "Missing value. The 'userToBeSponsored' must be sent.");
			}
			return ac.getMembersManager().setSponsoredMember(ac.getSession(), vo, userToBeSponsored, namespace, password, sponsor, validityTo);
		}
	},

	/*#
	 * Creates new sponsored members in a given VO and namespace.
	 *
	 * Can be called either by a user with role SPONSOR, in that case the user becomes the sponsor,
	 * or by a user with role REGISTRAR that must specify the sponsoring user using ID.
	 *
	 * Since there may be error while creating some of the members and we cannot simply rollback the transaction and start over,
	 * exceptions during member creation are not thrown and the returned map has this structure:
	 *
	 * name -> {"status" -> "OK" or "Error...", "login" -> login, "password" -> password}
	 *
	 * Keys are names given to this method and values are maps containing keys "status", "login" and "password".
	 * "status" has as its value either "OK" or message of exception which was thrown during creation of the member.
	 * "login" contains login (e.g. učo) if status is OK, "password" contains password if status is OK.
	 *
	 * @param guestNames List<String> names of members to create, single name should have the format
	 *                                {firstName};{lastName} to be parsed well
	 * @param vo int VO ID
	 * @param namespace String namespace selecting remote system for storing the password
	 * @param sponsor int sponsor's ID
	 * @param email (optional) preferred email that will be set to the created user. If no email
	 *              is provided, "no-reply@muni.cz" is used.
	 * @param validityTo (Optional) String the last day, when the sponsorship is active, yyyy-mm-dd format.
	 * @return Map<String, Map<String, String> newly created sponsored member, their password and status of creation
	 */
	createSponsoredMembers {
		@Override
		public Map<String, Map<String, String>> call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			Vo vo =  ac.getVoById(params.readInt("vo"));
			String namespace = params.readString("namespace");
			LocalDate validityTo = null;
			if (params.contains("validityTo")) {
				validityTo = params.readLocalDate("validityTo");
			}
			String email = null;
			if (params.contains("email")) {
				email = params.readString("email");
			}
			User sponsor = null;
			if(params.contains("sponsor")) {
				sponsor = ac.getUserById(params.readInt("sponsor"));
			}
			List<String> names;
			if (params.contains("guestNames")) {
				names = new ArrayList<>(params.readList("guestNames", String.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "Missing value: 'guestNames' must be sent.");
			}
			return ac.getMembersManager().createSponsoredMembers(ac.getSession(), vo, namespace, names, email, sponsor, validityTo);
		}
	},

	/*#
	 * Transform non-sponsored member to sponsored one with defined sponsor
	 *
	 * @param sponsoredMember int member's ID
	 * @param sponsor int sponsor's ID
	 * @param validityTo (Optional) String the last day, when the sponsorship is active, yyyy-mm-dd format.
	 * @return RichMember sponsored member which was newly set
	 */
	setSponsorshipForMember {
		@Override
		public RichMember call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			Member sponsoredMember = ac.getMemberById(params.readInt("sponsoredMember"));
			User sponsor = null;
			LocalDate validityTo = null;
			if (params.contains("validityTo")) {
				validityTo = params.readLocalDate("validityTo");
			}
			if (params.contains("sponsor")) {
				sponsor = ac.getUserById(params.readInt("sponsor"));
			}
			return ac.getMembersManager().setSponsorshipForMember(ac.getSession(), sponsoredMember, sponsor, validityTo);
		}
	},

	/*#
	 * Transform sponsored member to non-sponsored one. Delete all his sponsors.
	 *
	 * @param sponsoredMember int member's ID
	 *
	 * @return RichMember non-sponsored member
	 */
	unsetSponsorshipForMember {
		@Override
		public RichMember call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			Member sponsoredMember = ac.getMemberById(params.readInt("sponsoredMember"));
			return ac.getMembersManager().unsetSponsorshipForMember(ac.getSession(), sponsoredMember);
		}
	},

	/*#
	 * For an existing member, assigns a new sponsor.
	 *
	 * Can be called only by VO admin.
	 *
	 * @param member int id of sponsored member, optional
	 * @param sponsor int id of sponsoring user, optional
	 * @param validityTo (Optional) String the last day, when the sponsorship is active, yyyy-mm-dd format.
	 * @return RichMember sponsored member
	 */
	sponsorMember {
		@Override
		public RichMember call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			Member sponsored = ac.getMemberById(params.readInt("member"));
			User sponsor = ac.getUserById(params.readInt("sponsor"));
			LocalDate validityTo = null;
			if (params.contains("validityTo")) {
				validityTo = params.readLocalDate("validityTo");
			}
			return ac.getMembersManager().sponsorMember(ac.getSession(), sponsored, sponsor, validityTo);
		}
	},

	/*#
	 * Removes sponsor of existing member.
	 *
	 * Can be called only by VO admin.
	 *
	 * @param member int id of sponsored member, optional
	 * @param sponsor int id of sponsoring user that is to be removed, optional
	 */
	removeSponsor {
		@Override
		public Void call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			Member sponsoredMember = ac.getMemberById(params.readInt("member"));
			User sponsorToRemove = ac.getUserById(params.readInt("sponsor"));
			ac.getMembersManager().removeSponsor(ac.getSession(), sponsoredMember, sponsorToRemove);
			return null;
		}
	},

	/*#
	 * Update the sponsorship of given member for given sponsor.
	 *
	 * @param member int id of sponsored member, optional
	 * @param sponsor int id of sponsoring user that is to be removed
	 * @param validityTo String the last day, when the sponsorship is active, yyyy-mm-dd format.
	 *                          can be set to null never expire
	 * @throw PrivilegeException insufficient permissions
	 * @throw SponsorshipDoesNotExistException if the given user is not sponsor of the given member
	 * @throw MemberNotExistsException if there is no such member
	 * @throw UserNotExistsException if there is no such user
	 */
	updateSponsorshipValidity {
		@Override
		public Void call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();

			Member sponsoredMember = ac.getMemberById(params.readInt("member"));
			User sponsor = ac.getUserById(params.readInt("sponsor"));
			LocalDate newValidity = params.readLocalDate("validityTo");

			ac.getMembersManager().updateSponsorshipValidity(ac.getSession(), sponsoredMember, sponsor, newValidity);
			return null;
		}
	},

	/*#
	 * Changes expiration date for a sponsored member acording to VO rules.
	 *
	 * Can be called only by REGISTRAR or VOADMIN.
	 *
	 * @param vo int VO ID, optional
	 * @param member int id of sponsored member, optional
	 * @param sponsor int id of sponsoring user, optional
	 * @return String new expiration date
	 */
	extendExpirationForSponsoredMember {
		@Override
		public String call(ApiCaller ac, Deserializer params) throws PerunException {
			params.stateChangingCheck();
			Member sponsored = ac.getMemberById(params.readInt("member"));
			User sponsor = ac.getUserById(params.readInt("sponsor"));
			return ac.getMembersManager().extendExpirationForSponsoredMember(ac.getSession(), sponsored, sponsor);
		}
	},

	/*#
	 * Gets members sponsored by a given user in a VO. User is specified by user id.
	 *
	 * @param vo int VO ID
	 * @param sponsor int id of sponsoring user
	 * @return List<RichMember> sponsored members
	 */
	/*#
	 * Gets members from VO who are sponsored.
	 *
	 * @param vo int VO ID
	 * @return List<RichMember> sponsored members
	 */
	/*#
	 * Gets members with attributes sponsored by a given user in a VO. User is specified by user id.
	 *
	 * @param vo int VO ID
	 * @param sponsor int id of sponsoring user
	 * @param attrNames List<String> list of attribute names
	 * @return List<RichMember> sponsored members with attributes
	 */
	getSponsoredMembers {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer params) throws PerunException {
			Vo vo = ac.getVoById(params.readInt("vo"));
			if(params.contains("attrNames")) {
				User sponsor = ac.getUserById(params.readInt("sponsor"));
				List<String> attrNames = params.readList("attrNames", String.class);
				return ac.getMembersManager().getSponsoredMembers(ac.getSession(), vo, sponsor, attrNames);
			} else {
				if (params.contains("sponsor")) {
					User sponsor = ac.getUserById(params.readInt("sponsor"));
					return ac.getMembersManager().getSponsoredMembers(ac.getSession(), vo, sponsor);
				} else {
					return ac.getMembersManager().getSponsoredMembers(ac.getSession(), vo);
				}
			}
		}
	},

	/*#
	 * Gets list of sponsored members with sponsors.
	 *
	 * @param vo int id of virtual organization from which are the sponsored members chosen
	 * @param attrNames List<String> list of attribute names
	 * @throw VoNotExistsException if given VO does not exist
	 * @return List<MemberWithSponsors> list of members with sponsors
	 */
	getSponsoredMembersAndTheirSponsors {
		@Override
		public List<MemberWithSponsors> call(ApiCaller ac, Deserializer params) throws PerunException {
			Vo vo = ac.getVoById(params.readInt("vo"));
			List<String> attrNames = params.contains("attrNames") ? params.readList("attrNames",String.class) : Collections.emptyList();
			return ac.getMembersManager().getSponsoredMembersAndTheirSponsors(ac.getSession(), vo, attrNames);
		}
	},

	/*#
	 * Gets users sponsoring a given user in a VO.
	 *
	 * @deprecated - use usersManager/getSponsorsForMember
	 *
	 * Can be called by user in role REGISTRAR.
	 *
	 * @param member int member id
	 * @param attrNames List<String> names of attributes to return, empty to return all attributes
	 * @return List<RichUser> sponsors
	 */
	/*#
	 * Gets users sponsoring a given user in a VO.
	 *
	 * @deprecated - use usersManager/getSponsorsForMember
	 *
	 * Can be called by user in role REGISTRAR.
	 *
	 * @param vo int VO ID
	 * @param extSourceName String external source name, usually SAML IdP entityID
	 * @param extLogin String external source login, usually eduPersonPrincipalName
	 * @param attrNames List<String> names of attributes to return, empty to return all attributes
	 * @return List<RichUser> sponsors
	 */
	getSponsors {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer params) throws PerunException {
			Member member = null;
			if (params.contains("member")) {
				member = ac.getMemberById(params.readInt("member"));
			} else if (params.contains("vo") && params.contains("extSourceName") && params.contains("extLogin")) {
				Vo vo = ac.getVoById(params.readInt("vo"));
				User user = ac.getUsersManager().getUserByExtSourceNameAndExtLogin(ac.getSession(), params.readString("extSourceName"), params.readString("extLogin"));
				member = ac.getMembersManager().getMemberByUser(ac.getSession(), vo, user);
			}
			List<String> attrNames = params.contains("attrNames") ? params.readList("attrNames",String.class) : null;
			return ac.getUsersManager().getSponsors(ac.getSession(), member, attrNames);
		}
	},

	/*#
	 * Creates a new member and sets all member's attributes from the candidate.
	 * Also stores the associated user if doesn't exist. This method is used by the registrar.
	 *
	 * @param vo int VO <code>id</code>
	 * @param extSourceName String Name of the extSource
	 * @param extSourceType String Type of the extSource
	 * @param login String User's login within extSource
	 * @param candidate Candidate Candidate JSON object
	 * @return Member Created member
	 */
	/*#
	 * Creates a new member and sets all member's attributes from the candidate.
	 * Also stores the associated user if doesn't exist. This method is used by the registrar.
	 * This method also add user to all groups in list.
	 * Empty list of groups is ok, the behavior is then same like in the method without list of groups.
	 *
	 * @param vo int VO ID
	 * @param extSourceName String Name of the extSource
	 * @param extSourceType String Type of the extSource
	 * @param login String User's login within extSource
	 * @param candidate Candidate Candidate JSON object
	 * @param groups List<Group> List of groups where member need to be add too (must be from the same vo)
	 * @return Member Created member
	 */
	/*#
	 * Creates a new member from user.
	 *
	 * @param vo int VO <code>id</code>
	 * @param user int User <code>id</code>
	 * @return Member Created member
	 */
	/*#
	 * Creates a new member from user.
	 * This method also add user to all groups in list.
	 * Empty list of groups is ok, the behavior is then same like in the method without list of groups.
	 *
	 * @param vo int VO ID
	 * @param user int User ID
	 * @param groups List<Group> List of groups where member need to be add too (must be from the same vo)
	 * @return Member Created member
	 */
	/*#
	 * Creates a new member from candidate returned by the method VosManager.findCandidates which fills Candidate.userExtSource.
	 * <strong>This method runs asynchronously</strong>
	 *
	 * @param vo int VO ID
	 * @param candidate Candidate Candidate JSON object
	 * @return Member Created member
	 */
	/*#
	 * Creates a new member from candidate returned by the method VosManager.findCandidates which fills Candidate.userExtSource.
	 * This method also add user to all groups in list.
	 * Empty list of groups is ok, the behavior is then same like in the method without list of groups.
	 * <strong>This method runs asynchronously</strong>
	 *
	 * @param vo int VO <code>id</code>
	 * @param candidate Candidate Candidate JSON object
	 * @param groups List<Group> List of groups where member need to be add too (must be from the same vo)
	 * @return Member Created member
	 */
	createMember {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("extSourceName") && parms.contains("extSourceType") && parms.contains("login")) {
				if (parms.contains("groups")) {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readString("extSourceName"),
							parms.readString("extSourceType"),
							parms.readString("login"),
							parms.read("candidate", Candidate.class),
							parms.readList("groups", Group.class));
				} else {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readString("extSourceName"),
							parms.readString("extSourceType"),
							parms.readString("login"),
							parms.read("candidate", Candidate.class));
				}
			} else if(parms.contains("user") && parms.contains("vo")) {
				if (parms.contains("groups")) {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							ac.getUserById(parms.readInt("user")),
							parms.readList("groups", Group.class));
				} else {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							ac.getUserById(parms.readInt("user")));
				}
			} else if(parms.contains("extSource") && parms.contains("vo") && parms.contains("login")) {
				if (parms.contains("groups")) {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							ac.getExtSourceById(parms.readInt("extSource")),
							parms.readString("login"),
							parms.readList("groups", Group.class));
				} else {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							ac.getExtSourceById(parms.readInt("extSource")),
							parms.readString("login"));
				}
			} else {
				if (parms.contains("groups")) {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.read("candidate", Candidate.class),
							parms.readList("groups", Group.class));
				} else {
					return ac.getMembersManager().createMember(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.read("candidate", Candidate.class));
				}
			}
		}
	},

	/*#
	 * Find member of a VO by his login in an external source.
	 *
	 * @param vo int VO <code>id</code>
	 * @param userExtSource UserExtSource UserExtSource JSON object
	 * @return Member Found member
	 */
	getMemberByUserExtSource {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().getMemberByUserExtSource(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.read("userExtSource", UserExtSource.class));
		}
	},

	/*#
	 * Returns a member by their <code>id</code>.
	 *
	 * @param id int Member <code>id</code>
	 * @return Member Found member
	 */
	getMemberById {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().getMemberById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns a member by VO and User.
	 *
	 * @param vo int VO <code>id</code>
	 * @param user int User <code>id</code>
	 * @return Member Found member
	 */
	getMemberByUser {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().getMemberByUser(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Returns members for a user.
	 *
	 * @param user int User <code>id</code>
	 * @return List<Member> Found members
	 */
	getMembersByUser {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().getMembersByUser(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Returns all members of a VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @return List<Member> VO members
	 */
	/*#
	 * Returns all members of a VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @param status String VALID | INVALID | EXPIRED | DISABLED
	 * @return List<Member> VO members
	 */
	getMembers {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("status")) {
				return ac.getMembersManager().getMembers(ac.getSession(), ac.getVoById(parms.readInt("vo")), Status.valueOf(parms.readString("status")));
			} else {
				return ac.getMembersManager().getMembers(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
	 * Returns all members of a VO with additional information.
	 *
	 * @param vo int VO <code>id</code>
	 * @return List<RichMember> VO members
	 */
	/*#
	 * Returns all members of a VO with additional information.
	 *
	 * @param vo int VO <code>id</code>
	 * @param status String VALID | INVALID | EXPIRED | DISABLED
	 * @return List<RichMember> VO members
	 */
	getRichMembers {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("status")) {
				return ac.getMembersManager().getRichMembers(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						Status.valueOf(parms.readString("status")));
			} else {
				return ac.getMembersManager().getRichMembers(ac.getSession(),
						ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
 	 * Get all RichMembers with attributes specific for list of attrsNames from the vo and have only
 	 * status which is contain in list of statuses.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 * If listOfStatuses is empty or null, return all possible statuses.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @param allowedStatuses List<String> Allowed statuses (VALID | INVALID | EXPIRED | DISABLED)
 	 * @return List<RichMember> List of richMembers with specific attributes from Vo
 	 */
	/*#
 	 * Get all RichMembers with attributes specific for list of attrsNames from the vo.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @return List<RichMember> List of RichMembers with specific attributes from Vo
 	 */
	/*#
 	 * Get all RichMembers with attributes specific for list of attrsNames from the group and have only
 	 * status which is contain in list of statuses.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 * If listOfStatuses is empty or null, return all possible statuses.
 	 *
 	 * If lookingInParentGroup is true, get all these richMembers only for parentGroup of this group.
 	 * If this group is top level group, so get richMembers from members group.
 	 *
 	 * @param group int Group <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @param allowedStatuses List<String> Allowed statuses (VALID | INVALID | EXPIRED | DISABLED)
 	 * @param lookingInParentGroup boolean If true, look up in a parent group
 	 * @return List<RichMember> List of richMembers with specific attributes from group
 	 */
	/*#
 	 * Get all RichMembers with attributes specific for list of attrsNames from the group.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 *
 	 * If lookingInParentGroup is true, get all these richMembers only for parentGroup of this group.
 	 * If this group is top level group, so get richMembers from members group.
 	 *
 	 * @param group int Group <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @param lookingInParentGroup boolean If true, look up in a parent group
 	 * @return List<RichMember> List of richMembers with specific attributes from Group
 	 */
	/*#
	 * Get all RichMembers with attributes specific for list of attrsNames.
	 * Attributes are defined by member (user) and resource (facility) objects.
	 * It returns also user-facility (in userAttributes of RichMember) and
	 * member-resource (in memberAttributes of RichMember) attributes.
	 * Members are defined by group and are filtered by list of allowed statuses.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attrsNames List<String> Attribute names
	 * @param allowedStatuses List<String> Allowed statuses (VALID | INVALID | EXPIRED | DISABLED)
	 * @return List<RichMember> List of richMembers with selected specific attributes
	 */
	getCompleteRichMembers {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if(parms.contains("vo")) {
				if (parms.contains("allowedStatuses")) {
					if (parms.contains("attrsNames")) {
						// with selected attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getVoById(parms.readInt("vo")),
								parms.readList("attrsNames", String.class),
								parms.readList("allowedStatuses", String.class));
					} else {
						// with all attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getVoById(parms.readInt("vo")), null,
								parms.readList("allowedStatuses", String.class));
					}
				} else {
					if (parms.contains("attrsNames")) {
						// with selected attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getVoById(parms.readInt("vo")),
								parms.readList("attrsNames", String.class));
					} else {
						// with all attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getVoById(parms.readInt("vo")), null);
					}
				}
			} else {
				if (parms.contains("allowedStatuses")) {
					if (parms.contains("attrsNames")) {
						if (parms.contains("resource")) {
							// with selected attributes
							return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
									ac.getGroupById(parms.readInt("group")),
									ac.getResourceById(parms.readInt("resource")),
									parms.readList("attrsNames", String.class),
									parms.readList("allowedStatuses", String.class));
						} else {
							// with selected attributes
							return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
									ac.getGroupById(parms.readInt("group")),
									parms.readList("attrsNames", String.class),
									parms.readList("allowedStatuses", String.class),
									parms.readBoolean("lookingInParentGroup"));
						}
					} else {
						// with all attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getGroupById(parms.readInt("group")),
								null,
								parms.readList("allowedStatuses", String.class),
								parms.readBoolean("lookingInParentGroup"));
					}
				} else {
					if (parms.contains("attrsNames")) {
						// with selected attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attrsNames", String.class),
								parms.readBoolean("lookingInParentGroup"));
					} else {
						// with all attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getGroupById(parms.readInt("group")),
								null,
								parms.readBoolean("lookingInParentGroup"));
					}
				}
			}
		}
	},

	/*#
 	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for vo.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @param attrsNames List<String> List of attrsNames for selected attributes
 	 * @return List<RichMember> List of RichMembers in Vo
 	 */
	/*#
 	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for group.
 	 *
 	 * @exampleParam attrsNames [ "urn:perun:user:attribute-def:def:preferredMail" , "urn:perun:member:attribute-def:def:mail" ]
 	 * @param group int Group <code>id</code>
 	 * @param attrsNames List<String> List of attrsNames for selected attributes
 	 * @return List<RichMember> List of RichMembers in Group
 	 */
	getRichMembersWithAttributesByNames {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("vo")) {
				return ac.getMembersManager().getRichMembersWithAttributesByNames(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readList("attrsNames", String.class));
			} else {
				return ac.getMembersManager().getRichMembersWithAttributesByNames(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						parms.readList("attrsNames", String.class));
			}
		}
	},

	/*#
 	 * Get all RichMembers of VO with specified status. RichMember object contains user, member, userExtSources and member/user attributes.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @param status String Status (VALID | INVALID | EXPIRED | DISABLED)
 	 * @return List<RichMember> List of RichMembers with all member/user attributes, empty list if there are no members in VO with specified status
 	 */
	/*#
 	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for vo.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @param attrsDef List<AttributeDefinition> List of attrDefs only for selected attributes
 	 * @return List<RichMember> List of RichMembers in VO
 	 */
	/*#
 	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for group.
 	 *
 	 * @param group int Group <code>id</code>
 	 * @param attrsDef List<AttributeDefinition> List of attrDefs only for selected attributes
 	 * @return List<RichMember> List of RichMembers in Group
 	 */
	/*#
 	 * Get all RichMembers of VO. RichMember object contains user, member, userExtSources and member/user attributes.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @return List<RichMember> List of rich members with all member/user attributes, empty list if there are no members in VO
 	 */
	getRichMembersWithAttributes {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("status")) {
				return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						Status.valueOf(parms.readString("status")));
			} else if (parms.contains("attrsDef")) {
				if(parms.contains("vo")) {
					return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readList("attrsDef", AttributeDefinition.class));
				} else {
					return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
							ac.getGroupById(parms.readInt("group")),
							parms.readList("attrsDef", AttributeDefinition.class));
				}
			} else if (parms.contains("group")) {
				return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
						parms.readList("allowedStatuses", String.class),
						ac.getGroupById(parms.readInt("group")));
			} else {
				return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
						ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
	 * Returns a RichMember with all non-empty user/member attributes by it's member <code>id</code>.
	 *
	 * @param id int Member <code>id</code>
	 * @throw MemberNotExistsException When member with <code>id</code> doesn't exists
	 * @return RichMember Found RichMember by it's <code>id</code>
	 */
	getRichMemberWithAttributes {
		@Override
		public RichMember call(ApiCaller ac, Deserializer parms) throws PerunException {

			Member mem = ac.getMemberById(parms.readInt("id"));
			return ac.getMembersManager().getRichMemberWithAttributes(ac.getSession(), mem);
		}
	},

	/*#
	 * Returns a RichMember without attributes by it's member <code>id</code>.
	 *
	 * @param id int Member <code>id</code>
	 * @throw MemberNotExistsException When member with <code>id</code> doesn't exists
	 * @return RichMember Found member by it's <code>id</code>
	 */
	getRichMember {
		@Override
		public RichMember call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getMembersManager().getRichMemberById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns count of VO members with specified status.
	 *
	 * @param vo int VO <code>id</code>
	 * @param status String Status (VALID | INVALID | EXPIRED | DISABLED)
	 * @return int Members count
	 */
	/*#
	 * Returns count of all VO members.
	 *
	 * @param vo int VO <code>id</code>
	 * @return int Members count
	 */
	getMembersCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms)
		throws PerunException {
			if (parms.contains("status")) {
				return ac.getMembersManager().getMembersCount(ac.getSession(), ac.getVoById(parms.readInt("vo")), Status.valueOf(parms.readString("status")));
			} else {
				return ac.getMembersManager().getMembersCount(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			}
		}
	},

	/*#
	 * Deletes all VO members.
	 *
	 * @param vo int VO <code>id</code>
	 */
	deleteAllMembers {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getMembersManager().deleteAllMembers(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			return null;
		}
	},

	/*#
	 * Searches for members by their name.
	 *
	 * @param searchString String String to search by
	 * @return List<Member> Found members
	 */
	findMembersByName {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findMembersByName(ac.getSession(), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for members in a VO by their name.
	 *
	 * @param searchString String String to search by
	 * @param vo int VO <code>id</code> to search in
	 * @return List<Member> Found members
	 */
	findMembersByNameInVo {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findMembersByNameInVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for members in a Group by their name.
	 *
	 * @param searchString String String to search by
	 * @param group int Group <code>id</code> to search in
	 * @return List<Member> Found members
	 */
	findMembersInGroup {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findMembersInGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for members in a parent group of supplied group by their name.
	 *
	 * @param searchString String String to search by
	 * @param group int Group <code>id</code>, in whose parent group to search in
	 * @return List<Member> Found members
	 */
	findMembersInParentGroup {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findMembersInParentGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for rich members in a Group by their name.
	 *
	 * @param searchString String String to search by
	 * @param group int Group <code>id</code> to search in
	 * @return List<RichMember> Found members
	 */
	findRichMembersInGroup {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findRichMembersWithAttributesInGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for rich members in a parent group of supplied group by their name.
	 *
	 * @param searchString String String to search by
	 * @param group int Group <code>id</code>, in whose parent group to search in
	 * @return List<RichMember> Found members
	 */
	findRichMembersInParentGroup {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findRichMembersWithAttributesInParentGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for members in a VO.
	 *
	 * @param searchString String String to search by
	 * @param vo int VO <code>id</code>
	 * @return List<Members> Found members
	 */
	findMembersInVo {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findMembersInVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for members in a VO.
	 *
	 * @param searchString String String to search by
	 * @param vo int VO <code>id</code>
	 * @return List<RichMembers> Found members
	 */
	findRichMembersInVo {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findRichMembersInVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readString("searchString"));
		}
	},

	/*#
	 * Searches for members in a VO, listing with additional attributes.
	 *
	 * @param searchString String String to search by
	 * @param vo int VO <code>id</code>
	 * @return List<RichMembers> Found members
	 */
	findRichMembersWithAttributesInVo {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().findRichMembersWithAttributesInVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readString("searchString"));
		}
	},

	/*#
 	 * Return list of richMembers for specific vo by the searchString with attributes specific for list of attrsNames
 	 * and who have only status which is contain in list of statuses.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 * If listOfStatuses is empty or null, return all possible statuses.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @param allowedStatuses List<String> Allowed statuses
 	 * @param searchString String String to search by
 	 * @return List<RichMember> List of founded richMembers with specific attributes from Vo for searchString with allowed statuses
 	 */
	/*#
 	 * Return list of richMembers for specific vo by the searchString with attrs specific for list of attrsNames.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 *
 	 * @param vo int Vo <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @param searchString String String to search by
 	 * @return List<RichMember> List of founded richMembers with specific attributes from Vo for searchString
 	 */
	/*#
	 * Return list of richMembers for specific vo by the searchString with attrs specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 * By parameter onlySponsored we can return only sponsored members for specific vo.
	 *
	 * @param vo int Vo <code>id</code>
	 * @param attrsNames List<String> Attribute names
	 * @param searchString String String to search by
	 * @param onlySponsored Boolean true, if only sponsored members should be returned, false otherwise
	 * @return List<RichMember> List of founded richMembers with specific attributes from Vo for searchString
	 */
	/*#
 	 * Return list of richMembers for specific group by the searchString with attributes specific for list of attrsNames
 	 * and who have only status which is contain in list of statuses.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 * If listOfStatuses is empty or null, return all possible statuses.
 	 *
 	 * If lookingInParentGroup is true, find all these richMembers only for parentGroup of this group.
 	 * If this group is top level group, so find richMembers from members group.
 	 *
 	 * @param group int Group <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @param allowedStatuses List<String> Allowed statuses
 	 * @param searchString String String to search by
 	 * @param lookingInParentGroup boolean If true, look up in a parent group
 	 * @return List<RichMember> List of founded richMembers with specific attributes from Group for searchString
 	 */
	/*#
 	 * Return list of richMembers from perun by the searchString with attributes specific for list of attrsNames
 	 * and who have only status which is contain in list of statuses.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 * If listOfStatuses is empty or null, return all possible statuses.
 	 *
 	 * @param attrsNames List<String> Attribute names
 	 * @param allowedStatuses List<String> Allowed statuses
 	 * @param searchString String String to search by
 	 * @param lookingInParentGroup boolean If true, look up in a parent group
 	 * @return List<RichMember> List of founded richMembers with specific attributes from perun for searchString
 	 */
	/*#
 	 * Return list of richMembers for specific group by the searchString with attrs specific for list of attrsNames.
 	 * If attrsNames is empty or null return all attributes for specific richMembers.
 	 *
 	 * If lookingInParentGroup is true, find all these richMembers only for parentGroup of this group.
 	 * If this group is top level group, so find richMembers from members group.
 	 *
 	 * @param group int Group <code>id</code>
 	 * @param attrsNames List<String> Attribute names
 	 * @param searchString String String to search by
 	 * @param lookingInParentGroup boolean If true, look up in a parent group
 	 * @return List<RichMember> List of founded richMembers with specific attributes from Group for searchString
 	 */
	findCompleteRichMembers {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("vo")) {
				if(parms.contains("allowedStatuses")) {
					return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readList("attrsNames", String.class),
							parms.readList("allowedStatuses", String.class),
							parms.readString("searchString"));
				} else {
					boolean onlySponsored = false;
					if(parms.contains("onlySponsored")) onlySponsored = parms.readBoolean("onlySponsored");
					return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readList("attrsNames", String.class),
							parms.readString("searchString"),
							onlySponsored);
				}
			} else {
				if(parms.contains("allowedStatuses")) {
					if(parms.contains("group")) {
						return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attrsNames", String.class),
								parms.readList("allowedStatuses", String.class),
								parms.readString("searchString"),
								parms.readBoolean("lookingInParentGroup"));
					} else {
						return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
								parms.readList("attrsNames", String.class),
								parms.readList("allowedStatuses", String.class),
								parms.readString("searchString"));
					}
				} else {
					return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
							ac.getGroupById(parms.readInt("group")),
							parms.readList("attrsNames", String.class),
							parms.readString("searchString"),
							parms.readBoolean("lookingInParentGroup"));
				}
			}
		}
	},

	/*#
	 * Set membership status of a member.
	 *
	 * @param member int Member <code>id</code>
	 * @param status String VALID | INVALID | EXPIRED | DISABLED
	 * @exampleParam status "VALID"
	 * @return Member Member with status after change
	 */
	setStatus {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Status status = Status.valueOf(parms.readString("status"));
			return ac.getMembersManager().setStatus(ac.getSession(), ac.getMemberById(parms.readInt("member")), status);
		}
	},

	/*#
	 * Set date to which will be member suspended in his VO.
	 *
	 * For almost unlimited time please use time in the far future.
	 *
	 * @deprecated use vosManager setBan
	 * @param member int Member <code>id</code>
	 * @param suspendedTo String date in format yyyy-MM-dd to which member will be suspended
	 */
	suspendMemberTo {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			String suspendedToString = parms.readString("suspendedTo");

			Date suspendedTo;
			try {
				suspendedTo = BeansUtils.getDateFormatterWithoutTime().parse(suspendedToString);
			} catch (ParseException ex) {
				throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, "SuspendedTo is not in correct format yyyy-MM-dd and can't be parser correctly!");
			}

			ac.getMembersManager().suspendMemberTo(ac.getSession(), ac.getMemberById(parms.readInt("member")), suspendedTo);

			return null;
		}
	},

	/*#
	 * Remove suspend state from Member - remove date to which member should be considered as suspended in the VO.
	 *
	 * WARNING: this will remove the date even if it is in the past (so member is no longer considered as suspended)
	 *
	 * @deprecated use vosManager removeBan
	 * @param member int Member <code>id</code>
	 */
	unsuspendMember {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getMembersManager().unsuspendMember(ac.getSession(), ac.getMemberById(parms.readInt("member")));

			return null;
		}
	},

	/*#
	 * Returns member by his login in external source, name of external source and vo
	 *
	 * @param vo int Vo <code>id</code>
	 * @param extSourceName String Ext source name
	 * @param extLogin String Ext source login
	 * @return Member Member object
	 */
	getMemberByExtSourceNameAndExtLogin {

		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getMembersManager().getMemberByExtSourceNameAndExtLogin(ac.getSession(),
					ac.getVoById(parms.readInt("vo")),
					parms.readString("extSourceName"),
					parms.readString("extLogin"));
		}
	},

	/*#
	 * Validate all attributes for member and set member's status to VALID.
	 *
	 * This method runs asynchronously. It immediately return member with <b>original</b> status and
	 * after asynchronous validation successfully finishes it switch member's
	 * status to VALID. If validation ends with error, member keeps his status.
	 *
	 * @param member int Member <code>id</code>
	 * @return Member Member with original status
	 */
	validateMemberAsync {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			return ac.getMembersManager().validateMemberAsync(ac.getSession(), ac.getMemberById(parms.readInt("member")));
		}
	},

	/*#
	 *  Checks if the user can apply for membership in VO.
	 *  Decision is based on VO rules for: extendMembershipRules and doNotAllowLoa.
	 *
	 *  @param vo int VO <code>id</code>
	 *  @param loa String Level of Assurance (LoA) of user
	 *  @param user User User to check
	 *  @exampleResponse 1
	 *  @return int 1 if true | 0 if false
	 */
	canBeMember {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getMembersManager().canBeMember(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.read("user", User.class) , parms.readString("loa"))) {
				return 1;
			} else {
				return 0;
			}
		}
	},

	/*#
 	 * Checks if the user can apply for membership in VO.
 	 * Decision is based on VO rules for: extendMembershipRules and doNotAllowLoa.
 	 *
 	 * @param vo int VO <code>id</code>
 	 * @param user User User to check
 	 * @param loa String Level of Assurance (LoA) of user
 	 * @throw ExtendMembershipException When user can't become member of VO, reason is specified in exception text.
 	 * @exampleResponse 1
 	 * @return int 1 if true or throws exception if false
 	 */
	canBeMemberWithReason {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getMembersManager().canBeMemberWithReason(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.read("user", User.class) , parms.readString("loa"))) {
				return 1;
			} else {
				return 0;
			}
		}
	},

	/*#
	 * Return <code>1 == true</code> if membership can be extended or if VO has no rules for the membershipExpiration.
	 * Otherwise return <code>0 == false</code>.
	 *
	 * @param member int Member <code>id</code>
	 * @return int 1 if true | 0 if false
	 */
	canExtendMembership {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getMembersManager().canExtendMembership(ac.getSession(), ac.getMemberById(parms.readInt("member")))) {
				return 1;
			} else {
				return 0;
			}
		}
	},

	/*#
	 * Returns the date to which will be extended member's expiration time.
	 *
	 * @param member int Member <code>id</code>
	 */
	/*#
	 * Returns the date to which will be extended member's expiration time.
	 *
	 * @param vo int Vo <code>id</code>
	 * @param user int User <code>id</code>
	 */
	/*#
	 * Returns the date to which will be extended member's expiration time.
	 * Calculation is done just based on provided LoA and VO's membership expiration rules.
	 *
	 * @param vo int Vo <code>id</code>
	 * @param loa String LoA of user
	 */
	getNewExtendMembership {
		@Override
		public String call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("member")) {
				Date d = ac.getMembersManager().getNewExtendMembership(ac.getSession(),ac.getMemberById(parms.readInt("member")));
				if (d != null) {
					return BeansUtils.getDateFormatterWithoutTime().format(d);
				}
				return null;
			} else if (parms.contains("user") && parms.contains("vo")) {
				Member m = ac.getMembersManager().getMemberByUser(ac.getSession(),
						ac.getVoById(parms.readInt("vo")), ac.getUserById(parms.readInt("user")));
				Date d = ac.getMembersManager().getNewExtendMembership(ac.getSession(), m);
				if (d != null) {
					return BeansUtils.getDateFormatterWithoutTime().format(d);
				}
				return null;
			} else if (parms.contains("vo") && parms.contains("loa")) {
				Date d = ac.getMembersManager().getNewExtendMembership(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readString("loa"));
				if (d != null) {
					return BeansUtils.getDateFormatterWithoutTime().format(d);
				}
				return null;
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "member or (user, vo) or (vo, loa)");
			}
		}
	},

	/*#
	 * Send mail to user's preferred email address with link for non-authz password reset.
	 * Correct authz information is stored in link's URL.
	 *
	 * @param member int Member to get user to send link mail to
	 * @param namespace String Namespace to change password in (member must have login in it)
	 * @param emailAttributeURN urn of the attribute with stored mail
	 * @param language language of the message
	 */
	sendPasswordResetLinkEmail {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getMembersManager().sendPasswordResetLinkEmail(ac.getSession(), ac.getMemberById(parms.readInt("member")),
					parms.readString("namespace"), parms.getServletRequest().getRequestURL().toString(),
					parms.readString("emailAttributeURN"), parms.readString("language"));

			return null;

		}
	}

}
