package cz.metacentrum.perun.rpc.methods;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum MembersManagerMethod implements ManagerMethod {

	/*#
	 * Deletes only member data appropriated by member id.
	 *
	 * @param member int Member <code>id</code>
	 */
	deleteMember {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getMembersManager().deleteMember(ac.getSession(), ac.getMemberById(parms.readInt("member")));
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
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

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
			return ac.getMemberById(parms.readInt("id"));
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
	 * @param status String VALID | INVALID | SUSPENDED | EXPIRED | DISABLED
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
	 * @param status String VALID | INVALID | SUSPENDED | EXPIRED | DISABLED
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
 	 * @param allowedStatuses List<String> Allowed statuses (VALID | INVALID | SUSPENDED | EXPIRED | DISABLED)
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
 	 * @param allowedStatuses List<String> Allowed statuses (VALID | INVALID | SUSPENDED | EXPIRED | DISABLED)
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
						// with selected attributes
						return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attrsNames", String.class),
								parms.readList("allowedStatuses", String.class),
								parms.readBoolean("lookingInParentGroup"));
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
 	 * @param attrsNames List<String> List of attrNames for selected attributes
 	 * @return List<RichMember> List of RichMembers in Vo
 	 */
	/*#
 	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for group.
 	 *
 	 * @exampleParam attrsNames [ "urn:perun:user:attribute-def:def:preferredMail" , "urn:perun:member:attribute-def:def:mail" ]
 	 * @param group int Group <code>id</code>
 	 * @param attrsNames List<String> List of attrNames for selected attributes
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
 	 * @param status String Status (VALID | INVALID | SUSPENDED | EXPIRED | DISABLED)
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
	 * @param status String Status (VALID | INVALID | SUSPENDED | EXPIRED | DISABLED)
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
			ac.stateChangingCheck();

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
					return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readList("attrsNames", String.class),
							parms.readString("searchString"));
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
	 * @param status String VALID | INVALID | SUSPENDED | EXPIRED | DISABLED
	 * @exampleParam status "SUSPENDED"
	 * @return Member Member with status after change
	 */
	setStatus {
		@Override
		public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			Status status = Status.valueOf(parms.readString("status"));
			return ac.getMembersManager().setStatus(ac.getSession(), ac.getMemberById(parms.readInt("member")), status);
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
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

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
			ac.stateChangingCheck();

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
	 * @param namespace String Namespace to change password in (member must have login in)
	 * @param url String Base URL of Perun instance
	 */
	sendPasswordResetLinkEmail {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getMembersManager().sendPasswordResetLinkEmail(ac.getSession(), ac.getMemberById(parms.readInt("member")),
					parms.readString("namespace"), parms.getServletRequest().getRequestURL().toString());

			return null;

		}
	};
}
