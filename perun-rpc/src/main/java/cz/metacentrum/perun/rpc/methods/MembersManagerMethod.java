package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import java.util.List;

import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
public enum MembersManagerMethod implements ManagerMethod {
	/*#
	 * Deletes only member data appropriated by member id.
	 * 
	 * @param member int Member ID
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
   * Creates a new member from candidate which is prepared for creating serviceUser.
   *  
   * In list serviceUserOwners can't be serviceUser, only normal users are allowed. 
   * <strong>This method runs asynchronously</strong>
   * 
   * @param vo int VO ID
   * @param candidate Candidate prepared future serviceUser
   * @param serviceUserOwners List<User> List of users who own serviceUser (can't be empty or contain serviceUser)
   */
  createServiceMember {
    @Override    
    public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.stateChangingCheck();
      
      return ac.getMembersManager().createServiceMember(ac.getSession(), 
              ac.getVoById(parms.readInt("vo")), 
              parms.read("candidate", Candidate.class), 
              parms.readList("serviceUserOwners", User.class));
    }
  },
  
  /*#
   * Creates a new member and sets all member's attributes from the candidate. 
   * Also stores the associated user if doesn't exist. This method is used by the registrar.
   * 
   * @param vo int VO ID
   * @param extSourceName String Name of the extSource
   * @param extSourceType String Type of the extSource
   * @param login User's login within extSource
   * @param candidate Canidate Canidate JSON object
   * @return Member Created member
   */
  /*#
   * Creates a new member from user.
   * 
   * @param vo int VO ID
   * @param user int User ID
   * @return Member Created member
   */
  /*#
   * Creates a new member from candidate returned by the method VosManager.findCandidates which fills Candidate.userExtSource.
   * <strong>This method runs asynchronously</strong>
   * 
   * @param vo int VO ID
   * @param candidate Canidate Canidate JSON object
   * @return Member Created member
   */  
  createMember {
    @Override
    public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.stateChangingCheck();

      if (parms.contains("extSourceName") && parms.contains("extSourceType") && parms.contains("login")) {
        return ac.getMembersManager().createMember(ac.getSession(),
            ac.getVoById(parms.readInt("vo")),
            parms.readString("extSourceName"),
            parms.readString("extSourceType"),
            parms.readString("login"),
            parms.read("candidate", Candidate.class));
      } else if(parms.contains("user") && parms.contains("vo")) {
          return ac.getMembersManager().createMember(ac.getSession(),
            ac.getVoById(parms.readInt("vo")),
            ac.getUserById(parms.readInt("user")));
      } else {
        return ac.getMembersManager().createMember(ac.getSession(),
            ac.getVoById(parms.readInt("vo")),
            parms.read("candidate", Candidate.class));
      }
    }
  },

  /*#
   * Find member of a VO by his login in an external source.
   * 
   * @param vo int VO ID
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
   * Returns a member by their ID.
   * 
   * @param id int Member ID
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
   * @param vo int VO ID
   * @param user int User ID
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
   * @param user int User ID
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
   * @param vo int VO ID
   * @return List<Member> VO members
   */
  /*#
   * Returns all members of a VO.
   * 
   * @param vo int VO ID
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
   * @param vo int VO ID
   * @return List<RichMember> VO members
   */
  /*#
   * Returns all members of a VO with additional information.
   * 
   * @param vo int VO ID
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
  
  getCompleteRichMembers {
  
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {

      if(parms.contains("vo")) {
          if (parms.contains("allowedStatuses[]")) {
              if (parms.contains("attrsNames[]")) {
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
              if (parms.contains("attrsNames[]")) {
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
          if (parms.contains("allowedStatuses[]")) {
              if (parms.contains("attrsNames[]")) {
                // with selected attributes
                return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")),
                        parms.readList("attrsNames", String.class),
                        parms.readList("allowedStatuses", String.class),
                        parms.readInt("lookingInParentGroup") == 1);
              } else {
                // with all attributes
                return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")),
                        null,
                        parms.readList("allowedStatuses", String.class),
                        parms.readInt("lookingInParentGroup") == 1);
              }
          } else {
              if (parms.contains("attrsNames[]")) {
                // with selected attributes
                return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")),
                        parms.readList("attrsNames", String.class),
                        parms.readInt("lookingInParentGroup") == 1);
              } else {
                // with all attributes
                return ac.getMembersManager().getCompleteRichMembers(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")),
                        null,
                        parms.readInt("lookingInParentGroup") == 1);
              }
          }
      }
    }
  },  

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

  getRichMembersWithAttributes {

    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if(parms.contains("status")) {
        return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
            ac.getVoById(parms.readInt("vo")),
            Status.valueOf(parms.readString("status")));
      } else if(parms.contains("attrsDef")) {
          if(parms.contains("vo")) {
              return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
                   ac.getVoById(parms.readInt("vo")),
                   parms.readList("attrsDef", AttributeDefinition.class));
          } else {
              return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
                   ac.getGroupById(parms.readInt("group")),
                   parms.readList("attrsDef", AttributeDefinition.class));
          }
      } else {
        return ac.getMembersManager().getRichMembersWithAttributes(ac.getSession(),
            ac.getVoById(parms.readInt("vo")));
      }

    }
  },
  /*#
   * Returns a rich member by their member ID.
   * 
   * @param id int Member ID
   * @return RichMember Found member
   */
  getRichMemberWithAttributes {

    @Override
    public RichMember call(ApiCaller ac, Deserializer parms) throws PerunException {

      Member mem = ac.getMemberById(parms.readInt("id"));
      return ac.getMembersManager().getRichMemberWithAttributes(ac.getSession(), mem);

    }
  },
  
  /*#
   * Returns a rich member without attributes by id of member.
   * 
   * @param id int id of Member
   * @return RichMember Found member
   */
  getRichMember {

    @Override
    public RichMember call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getMembersManager().getRichMemberById(ac.getSession(), parms.readInt("id"));

    }
  },
  
  /*#
   * Returns VO members count.
   * 
   * @param vo int VO ID
   * @param status String VALID | INVALID | SUSPENDED | EXPIRED | DISABLED
   * @return int Members count
   */
  /*#
   * Returns VO members count.
   * 
   * @param vo int VO ID
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
   * @param vo int VO ID
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
   * @param vo int VO ID to search in
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
   * @param group int Group ID to search in
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
   * @param group int Group ID, in whose parent group to search in
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
   * @param group int Group ID to search in
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
   * @param group int Group ID, in whose parent group to search in
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
   * @param vo int VO ID
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
   * @param vo int VO ID
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
   * @param vo int VO ID
   * @return List<RichMembers> Found members
   */
  findRichMembersWithAttributesInVo {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getMembersManager().findRichMembersWithAttributesInVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readString("searchString"));
    }
  },
  
  findCompleteRichMembers {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
        if(parms.contains("vo")) {
            if(parms.contains("allowedStatuses[]")) {
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
            if(parms.contains("allowedStatuses[]")) {
                 return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")),
                        parms.readList("attrsNames", String.class),
                        parms.readList("allowedStatuses", String.class),
                        parms.readString("searchString"),
                        parms.readInt("lookingInParentGroup") == 1); 
            } else {
                return ac.getMembersManager().findCompleteRichMembers(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")),
                        parms.readList("attrsNames", String.class),
                        parms.readString("searchString"),
                        parms.readInt("lookingInParentGroup") == 1);
            }
       }
    }
  },

  /*#
   * Sets a status of a member.
   * 
   * @param member int Member ID
   * @param status String VALID | INVALID | SUSPENDED | EXPIRED | DISABLED
   * @return Member Member object
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
   * Validate all atributes for member and set member's status to VALID.
   * This metod runs asynchronously.
   * 
   * It immideatelly return member with <b>ORIGINAL</b> status and
   * after asynchronous validation sucessfuly finishes it swich member's
   * status to VALID. If validation ends with error, memeber keeps his status.
   * 
   * @param member int Member ID
   * @return Member Member object
   */
  validateMemberAsync {
    @Override
    public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.stateChangingCheck();	

      return ac.getMembersManager().validateMemberAsync(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   *  Checks if the user can apply membership to the VO.
   *  It's based on extendMembershipRules on the doNotAllowLoa key.
   *  
   *  @param vo int VO ID
   *  @param loa String LOA
   *  @param user User User JSON object
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
  }; 
}
