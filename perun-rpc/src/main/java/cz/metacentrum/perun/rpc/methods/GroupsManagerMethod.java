package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsPageQuery;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleAssignmentType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ObjectIDMismatchException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public enum GroupsManagerMethod implements ManagerMethod {

  /*#
   * Creates a subgroup of a group.
   * Group object must contain name. Description is optional. Other parameters are ignored.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupExistsException When group exists
   * @throw GroupRelationNotAllowed When the group relation cannot be created, because it's not allowed
   * @throw GroupRelationAlreadyExists When the group relation already exists
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param parentGroup int Parent Group <code>id</code>
   * @param group Group JSON Group class
   * @return Group Newly created group
   * @exampleParam group { "name" : "My testing Group" }
   */
  /*#
   * Creates a subgroup of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupExistsException When group exists
   * @throw GroupRelationNotAllowed When the group relation cannot be created, because it's not allowed
   * @throw GroupRelationAlreadyExists When the group relation already exists
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param parentGroup int Parent Group <code>id</code>
   * @param name String name of a group
   * @param description String description of a group
   * @return Group Newly created group
   * @exampleParam parentGroup 1
   * @exampleParam description "A description with information"
   * @exampleParam name "My testing Group"
   */
  /*#
   * Creates a new top-level group in the specific VO defined by object vo in parameter.
   * Important: voId in object group is ignored.
   * Group object must contain name. Description and parentGroupId are optional. Other parameters are ignored.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupExistsException When group exists
   * @throw GroupRelationNotAllowed When the group relation cannot be created, because it's not allowed
   * @throw GroupRelationAlreadyExists When the group relation already exists
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int Parent VO <code>id</code>
   * @param group Group JSON Group class - parentGroupId must be set to null in order to work
   * @return Group Newly created group
   * @exampleParam vo 1
   * @exampleParam group { "name" : "My testing Group" }
   */
  /*#
   * Creates a new group in the specific VO.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupExistsException When group exists
   * @throw GroupRelationNotAllowed When the group relation cannot be created, because it's not allowed
   * @throw GroupRelationAlreadyExists When the group relation already exists
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int Parent VO <code>id</code>
   * @param name String name of a group
   * @param description String description of a group
   * @return Group Newly created group
   * @exampleParam vo 1
   * @exampleParam description "A description with information"
   * @exampleParam name "My testing Group"
   */
  createGroup {
    @Override
    public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("group")) {
        if (parms.contains("parentGroup")) {
          return ac.getGroupsManager().createGroup(ac.getSession(), ac.getGroupById(parms.readInt("parentGroup")),
              parms.read("group", Group.class));
        } else if (parms.contains("vo")) {
          Group group = parms.read("group", Group.class);
          if (group.getParentGroupId() == null) {
            return ac.getGroupsManager().createGroup(ac.getSession(), ac.getVoById(parms.readInt("vo")), group);
          } else {
            throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Top-level groups can't have parentGroupId set!");
          }
        } else {
          throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or parentGroup");
        }
      } else if (parms.contains("name") && parms.contains("description")) {
        if (parms.contains("parentGroup")) {
          String name = parms.readString("name");
          String description = parms.readString("description");
          Group group = new Group(name, description);
          return ac.getGroupsManager()
              .createGroup(ac.getSession(), ac.getGroupById(parms.readInt("parentGroup")), group);
        } else if (parms.contains("vo")) {
          String name = parms.readString("name");
          String description = parms.readString("description");
          Group group = new Group(name, description);
          return ac.getGroupsManager().createGroup(ac.getSession(), ac.getVoById(parms.readInt("vo")), group);
        } else {
          throw new RpcException(RpcException.Type.MISSING_VALUE, "vo or parentGroup");
        }
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "group or (name and description)");
      }
    }
  },

  /*#
   * Create union of two groups, where "operandGroup" is technically set as subgroup of "resultGroup".
   * Members from "operandGroup" are added to "resultGroup" as INDIRECT members. Union is honored also
   * in all group member changing operations.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupRelationNotAllowed When the group relation cannot be created, because it's not allowed
   * @throw GroupRelationAlreadyExists When the group relation already exists
   * @throw WrongAttributeValueException When the value of the attribute is illegal or wrong
   * @throw WrongReferenceAttributeValueException When the attribute of the reference has illegal value
   * @throw VoNotExistsException When the groups' VO doesn't exist
   *
   * @param resultGroup int <code>id</code> of Group to have included "operandGroup"
   * @param operandGroup int <code>id</code> of Group to be included into "resultGroup"
   * @return Group Result group
   */
  createGroupUnion {
    @Override
    public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getGroupsManager().createGroupUnion(ac.getSession(), ac.getGroupById(parms.readInt("resultGroup")),
          ac.getGroupById(parms.readInt("operandGroup")));
    }
  },

  /*#
   * Deletes a group. Group is not deleted, if contains members or is assigned to any resource.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw RelationExistsException When the relation already exists
   * @throw GroupAlreadyRemovedException When the group has already been removed
   * @throw GroupAlreadyRemovedFromResourceException When group has already been removed from the resource
   * @throw GroupRelationDoesNotExist When group relation does not exist
   * @throw GroupRelationCannotBeRemoved When group relation cannot be removed
   *
   * @param group int Group <code>id</code>
   */
  /*#
   * Forcefully deletes a group (remove all group members, remove group from resources).
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw RelationExistsException When the relation already exists
   * @throw GroupAlreadyRemovedException When the group has already been removed
   * @throw GroupAlreadyRemovedFromResourceException When group has already been removed from the resource
   * @throw GroupRelationDoesNotExist When group relation does not exist
   * @throw GroupRelationCannotBeRemoved When group relation cannot be removed
   *
   * @param group int Group <code>id</code>
   * @param force boolean If true use force delete.
   */
  deleteGroup {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      boolean force = false;
      if (parms.contains("force")) {
        force = parms.readBoolean("force");
      }
      ac.getGroupsManager().deleteGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")), force);
      return null;
    }
  },

  /*#
   * Forcefully deletes a list of groups (remove all group members, remove group from resources).
   *
   * @throw GroupNotExistsException If any group not exists in perun
   * @throw GroupAlreadyRemovedException If any groups is already deleted
   * @throw RelationExistsException If group has subgroups or member (forceDelete is false)
   * @throw GroupAlreadyRemovedFromResourceException  If any group is already removed from resource
   * @throw GroupRelationDoesNotExist If the relation doesn't exist
   * @throw GroupRelationCannotBeRemoved When the group relation cannot be removed
   *
   * @param groups int[] Array of Group IDs
   * @param forceDelete boolean If true use force delete.
   */
  deleteGroups {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      //TODO: optimalizovat?
      int[] ids = parms.readArrayOfInts("groups");
      List<Group> groups = new ArrayList<>(ids.length);
      for (int i : ids) {
        groups.add(ac.getGroupById(i));
      }

      ac.getGroupsManager().deleteGroups(ac.getSession(), groups, parms.readBoolean("forceDelete"));
      return null;
    }
  },

  /*#
   * Removes union of two groups, when "operandGroup" is technically removed from subgroups of "resultGroup".
   * Members from "operandGroup" are removed from "resultGroup" if they were INDIRECT members sourcing from this
   * group only.
   *
   * @throw GroupNotExistsException If any group not exists in perun
   * @throw GroupRelationDoesNotExist If the relation doesn't exist
   * @throw GroupRelationCannotBeRemoved When the group relation cannot be removed
   *
   * @param resultGroup int <code>id</code> of Group to have removed "operandGroup" from subgroups
   * @param operandGroup int <code>id</code> of Group to be removed from "resultGroup" subgroups
   */
  removeGroupUnion {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getGroupsManager().removeGroupUnion(ac.getSession(), ac.getGroupById(parms.readInt("resultGroup")),
          ac.getGroupById(parms.readInt("operandGroup")));
      return null;
    }
  },

  /*#
   * Removes unions between groups, where "operandGroups" are technically removed from subgroups of "resultGroup".
   * Members from all "operandGroups" are removed from "resultGroup" if they were INDIRECT members sourcing from these
   * groups only.
   *
   * @throw GroupNotExistsException If any group not exists in perun
   * @throw GroupRelationDoesNotExist If the relation doesn't exist
   * @throw GroupRelationCannotBeRemoved When the group relation cannot be removed
   *
   * @param resultGroup int <code>id</code> of Group to have removed "operandGroup" from subgroups
   * @param operandGroups List<Integer> list of Groups to be removed from "resultGroup" subgroups
   */
  removeGroupUnions {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      //TODO: optimalizovat?
      int[] ids = parms.readArrayOfInts("operandGroups");
      List<Group> operandGroups = new ArrayList<>(ids.length);
      for (int i : ids) {
        operandGroups.add(ac.getGroupById(i));
      }
      ac.getGroupsManager().removeGroupUnions(ac.getSession(), ac.getGroupById(parms.readInt("resultGroup")),
              operandGroups);
      return null;
    }
  },

  /*#
   * Updates a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupExistsException when the group with the same name already exists in the same vo
   * @throw ObjectIDMismatchException when IDs in passed group object doesn't match their actual value in Perun
   *
   * @param group Group JSON Group class
   * @return Group Updated group
   */
  updateGroup {
    @Override
    public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      Group newGroup = parms.read("group", Group.class);
      Group realGroup = ac.getGroupById(newGroup.getId());
      // Check IDs
      if (realGroup.getVoId() != newGroup.getVoId()) {
        throw new ObjectIDMismatchException("VO ID in passed group object doesn't match the actual value.");
      }
      if (!Objects.equals(realGroup.getParentGroupId(), newGroup.getParentGroupId())) {
        throw new ObjectIDMismatchException("Parent group ID in passed group object doesn't match the actual value.");
      }
      // pass group name and description to internal object to safely resolve authorization
      realGroup.setName(newGroup.getName());
      realGroup.setDescription(newGroup.getDescription());
      return ac.getGroupsManager().updateGroup(ac.getSession(), realGroup);
    }
  },

  /*#
   * Moves "movingGroup" (including subGroups) under "destinationGroup" as subGroup within same Vo.
   * Indirect group members are also processed during move operation.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw WrongAttributeValueException When the value of the attribute is illegal or wrong
   * @throw WrongReferenceAttributeValueException When the attribute of the reference has illegal value
   *
   * @param destinationGroup int <code>id</code> of Group to have "movingGroup" as subGroup
   * @param movingGroup int <code>id</code> of Group to be moved under "destinationGroup"
   */
  /*#
   * Moves "movingGroup" (including subGroups) from it`s location to top-level.
   * Indirect group members are also processed during move operation.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw WrongAttributeValueException When the value of the attribute is illegal or wrong
   * @throw WrongReferenceAttributeValueException When the attribute of the reference has illegal value
   *
   * @param movingGroup int <code>id</code> of Group to be moved under "destinationGroup"
   */
  moveGroup {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("destinationGroup")) {
        ac.getGroupsManager().moveGroup(ac.getSession(), ac.getGroupById(parms.readInt("destinationGroup")),
            ac.getGroupById(parms.readInt("movingGroup")));
      } else {
        ac.getGroupsManager().moveGroup(ac.getSession(), null, ac.getGroupById(parms.readInt("movingGroup")));
      }
      return null;
    }
  },

  /*#
   * Returns a group by <code>id</code>.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param id int Group <code>id</code>
   * @return Group Found group
   */
  getGroupById {
    @Override
    public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getGroupById(ac.getSession(), parms.readInt("id"));
    }
  },

  /*#
   * Returns a group by VO and Group name.
   *
   * IMPORTANT: need to use full name of group (ex. 'toplevel:a:b', not the shortname which is in this example 'b')
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param vo int VO <code>id</code>
   * @param name String Group name
   * @return Group Found group
   */
  getGroupByName {
    @Override
    public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getGroupByName(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.readString("name"));
    }
  },

  /*#
   * Returns groups by their ids.
   *
   * @param ids List<Integer> list of groups IDs
   * @return List<Group> groups with specified IDs
   */
  getGroupsByIds {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getGroupsByIds(ac.getSession(), parms.readList("ids", Integer.class));
    }
  },

  /*#
   * Return all operand groups for specified result groups (all INCLUDED groups).
   * If "reverseDirection" is TRUE than return all result groups for specified operand group (where group is INCLUDED).
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int <code>id</code> of Group to get groups in union.
   * @param reverseDirection boolean FALSE (default) return INCLUDED groups / TRUE = return groups where INCLUDED
   * @return List<Group> List of groups in union relation.
   */
  getGroupUnions {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getGroupUnions(ac.getSession(), ac.getGroupById(parms.readInt("group")),
          parms.readBoolean("reverseDirection"));
    }
  },


  /*#
   * Adds members to a group. If already a member of the group, the member will be skipped.
   * Non-empty list of members expected, if empty, no member will be added.
   *
   * @param group int <code>id</code> of the group that the members will be added to <code>id</code>
   * @param members List<Integer> <code>id</code> of members that will be added to the group <code>id</code>
   */
  addMembers {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<Integer> memberInts = parms.readList("members", Integer.class);
      if (memberInts == null) {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "Non-empty list of members not sent.");
      }
      Group group = ac.getGroupById(parms.readInt("group"));
      List<Member> members = new ArrayList<>();
      for (Integer memberInt : memberInts) {
        Member member = ac.getMemberById(memberInt);
        if (!ac.getGroupsManager().isDirectGroupMember(ac.getSession(), group, member)) {
          members.add(member);
        }
      }
      ac.getGroupsManager().addMembers(ac.getSession(), group, members);

      return null;
    }
  },

  /*#
   * Copies direct members from one group to other groups in the same VO. The members are copied without their
   * member-group attributes.
   * Copies all direct members if members list is empty or null.
   *
   * @param sourceGroup int <code>id</code> of the group to copy members from
   * @param destinationGroups List<Integer> <code>id</code> of groups to copy members to
   * @param members List<Integer> <code>id</code> of members that will be copied
   */
  copyMembers {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<Member> members = new ArrayList<>();
      if (parms.contains("members")) {
        List<Integer> memberIds = parms.readList("members", Integer.class);
        for (Integer id : memberIds) {
          members.add(ac.getMemberById(id));
        }
      }

      List<Group> groups = new ArrayList<>();
      List<Integer> groupIds = parms.readList("destinationGroups", Integer.class);
      for (Integer id : groupIds) {
        groups.add(ac.getGroupById(id));
      }

      ac.getGroupsManager()
          .copyMembers(ac.getSession(), ac.getGroupById(parms.readInt("sourceGroup")), groups, members);
      return null;
    }
  },

  /*#
   * Adds a member to a group.
   *
   * @throws MemberNotExistsException When member doesn't exist
   * @throws AlreadyMemberException When already member
   * @throws GroupNotExistsException When group doesn't exist
   * @throws WrongAttributeValueException If any member attribute value, required by resource (on which the group is
   * assigned), is wrong
   * @throws WrongAttributeAssignmentException Thrown while assigning atribute to wrong entity
   * @throws AttributeNotExistsException When attribute doesn't exist
   * @throw WrongReferenceAttributeValueException When the attribute of the reference has illegal value
   * @throws ExternallyManagedException When the group is externally managed
   *
   * @param group int Group <code>id</code>
   * @param member int Member <code>id</code>
   */
  /*#
   * Adds a member to the group. If already a member of a group, the group will be skipped.
   * Non-empty list of groups expected, if empty, no member will be added.
   *
   * @param groups List<Integer> List of <code>id</code> of groups that the member will be added to <code>id</code>
   * @param member int <code>id</code> of a member that will be added to the groups <code>id</code>
   */
  addMember {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("group")) {
        ac.getGroupsManager().addMember(ac.getSession(), ac.getGroupById(parms.readInt("group")),
            ac.getMemberById(parms.readInt("member")));
      } else if (parms.contains("groups")) {
        List<Integer> groupsInts = parms.readList("groups", Integer.class);
        if (groupsInts == null) {
          throw new RpcException(RpcException.Type.MISSING_VALUE, "Non-empty list of groups not sent.");
        }
        List<Group> groups = new ArrayList<>();
        for (Integer groupInt : groupsInts) {
          groups.add(ac.getGroupById(groupInt));
        }
        ac.getGroupsManager().addMember(ac.getSession(), groups, ac.getMemberById(parms.readInt("member")));

      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "Parameter not provided. 'group' or 'groups' missing.");
      }
      return null;
    }
  },

  /*#
   * Removes members from a group.
   * Non-empty list of members expected. In case of empty list, no member is removed from the group.
   * If member is not in the group or the membership is indirect, it is skipped without a warning but the rest of the
   *  members are processed.
   *
   * @throw MemberNotExistsException When member doesn't exist
   * @throw NotGroupMemberException  When member is not in the group
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw WrongAttributeAssignmentException When assigning atribute to wrong entity
   * @throw AttributeNotExistsException When attribute doesn't exist
   * @throw ExternallyManagedException When the group is externally managed
   *
   * @param group int Group <code>id</code>
   * @param members List<Integer> Members - can be empty <code>id</code>
   */
  removeMembers {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      List<Integer> memberInts = parms.readList("members", Integer.class);
      if (memberInts == null) {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "Non-empty list of members not sent.");
      }
      Group group = ac.getGroupById(parms.readInt("group"));
      List<Member> members = new ArrayList<>();
      for (Integer memberInt : memberInts) {
        try {
          Member member = ac.getGroupsManager().getGroupMemberById(ac.getSession(), group, memberInt);
          if (MembershipType.DIRECT.equals(member.getMembershipType())) {
            members.add(member);
          }
        } catch (NotGroupMemberException e) {
          //skipped because user is not member of this group so we don't need to remove him
        }
      }
      ac.getGroupsManager().removeMembers(ac.getSession(), group, members);
      return null;
    }
  },


  /*#
   * Removes a member from a group.
   *
   * @throw MemberNotExistsException When member doesn't exist
   * @throw NotGroupMemberException  When member is not in the group
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw WrongAttributeAssignmentException When assigning atribute to wrong entity
   * @throw AttributeNotExistsException When attribute doesn't exist
   * @throw ExternallyManagedException When the group is externally managed
   *
   * @param group int Group <code>id</code>
   * @param member int Member <code>id</code>
   */
  /*#
   * Removes a member from groups. If a member is not in the group or is indirect, it is skipped without a warning,
   * but the rest of groups are processed.
   * Non-empty list of groups expected. In case of empty list, member is not removed.
   *
   * @throw MemberNotExistsException When member doesn't exist
   * @throw NotGroupMemberException  When member is not in the group
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw WrongAttributeAssignmentException When assigning atribute to wrong entity
   * @throw AttributeNotExistsException When attribute doesn't exist
   * @throw ExternallyManagedException When the group is externally managed
   *
   * @param groups List<Integer> Group - can be empty <code>id</code>
   * @param member int Member <code>id</code>
   */
  removeMember {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("group")) {
        ac.getGroupsManager().removeMember(ac.getSession(), ac.getGroupById(parms.readInt("group")),
            ac.getMemberById(parms.readInt("member")));
      } else if (parms.contains("groups")) {
        List<Integer> groupsInts = parms.readList("groups", Integer.class);
        if (groupsInts == null) {
          throw new RpcException(RpcException.Type.MISSING_VALUE, "Non-empty list of groups not sent.");
        }
        List<Group> groups = new ArrayList<>();
        for (Integer groupInt : groupsInts) {
          try {
            Group group = ac.getGroupById(groupInt);
            Member member = ac.getGroupsManager().getGroupMemberById(ac.getSession(), group, parms.readInt("member"));
            if (MembershipType.DIRECT.equals(member.getMembershipType())) {
              groups.add(group);
            }
          } catch (NotGroupMemberException e) {
            //skipped because user is not member of this group so we don't need to remove him
          }
        }
        ac.getGroupsManager().removeMember(ac.getSession(), ac.getMemberById(parms.readInt("member")), groups);
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE,
            "non-empty parameter 'groups' or parameter 'group' not sent");
      }

      return null;
    }
  },

  /*#
   * Returns members of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return List<Member> Group members
   */
  getGroupMembers {
    @Override
    public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getGroupMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));

    }
  },

  /*#
   * Returns direct members of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return List<Member> Group members
   */
  getGroupDirectMembers {
    @Override
    public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getGroupDirectMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));

    }
  },

  /*#
   * Returns members of a group.
   * RichMember contains User object.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return List<RichMember> Group members
   */
  getGroupRichMembers {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getGroupRichMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns direct members of a group.
   * RichMember contains User object.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return List<RichMember> Group members
   */
  getGroupDirectRichMembers {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getGroupDirectRichMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns members of a group.
   * RichMember contains User object and attributes.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return List<RichMember> Group members
   */
  getGroupRichMembersWithAttributes {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getGroupRichMembersWithAttributes(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Return groups which supply the last FACILITYADMIN in some Facility.
   *
   * @throw GroupNotExistsException When a group doesn't exist
   * @throw PrivilegeException Insufficient rights
   *
   * @param groups int[] Array of Group IDs
   * @return List<Group> groups which supply the last FACILITYADMIN in some Facility
   */
  isGroupLastAdminInSomeFacility {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      int[] ids = parms.readArrayOfInts("groups");
      List<Group> groups = new ArrayList<>(ids.length);
      for (int i : ids) {
        groups.add(ac.getGroupById(i));
      }

      return ac.getGroupsManager()
                 .isGroupLastAdminInSomeFacility(ac.getSession(), groups);
    }
  },

  /*#
   * Return groups which supply the last VOADMIN in some VO.
   *
   * @throw GroupNotExistsException When a group doesn't exist
   * @throw PrivilegeException Insufficient rights
   *
   * @param groups int[] Array of Group IDs
   * @return List<Group> groups which supply the last VOADMIN in some VO
   */
  isGroupLastAdminInSomeVo {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      int[] ids = parms.readArrayOfInts("groups");
      List<Group> groups = new ArrayList<>(ids.length);
      for (int i : ids) {
        groups.add(ac.getGroupById(i));
      }
      return ac.getGroupsManager()
                 .isGroupLastAdminInSomeVo(ac.getSession(), groups);
    }
  },

  /*#
   * Return true if Member is member of the Group
   *
   * @param group int Group ID
   * @param member int Member ID
   * @return boolean True if Member is member of the Group
   *
   * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
   */
  isGroupMember {
    @Override
    public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().isGroupMember(ac.getSession(), ac.getGroupById(parms.readInt("group")),
          ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   * Return true if group synchronization has been suspended.
   *
   * @return boolean True if group synchronization is suspended
   *
   */
  isSuspendedGroupSynchronization {
    @Override
    public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().isSuspendedGroupSynchronization(ac.getSession());
    }
  },

  /*#
   * Returns count of group members.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return int Members count
   */
  getGroupMembersCount {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getGroupMembersCount(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns count of direct group members.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return int Direct Members count
   */
  getGroupDirectMembersCount {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getGroupDirectMembersCount(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns counts of group members by their status in VO.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return Map<String, Integer> map of member status in VO to count of group members with the status
   */
  getGroupMembersCountsByVoStatus {
    @Override
    public Map<String, Integer> call(ApiCaller ac, Deserializer parms) throws PerunException {
      Map<Status, Integer> counts = ac.getGroupsManager()
          .getGroupMembersCountsByVoStatus(ac.getSession(), ac.getGroupById(parms.readInt("group")));

      // convert Status to String
      return counts.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
  },

  /*#
   * Returns counts of group members by their group status.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return Map<String, Integer> map of member status in group to count of group members with the status
   */
  getGroupMembersCountsByGroupStatus {
    @Override
    public Map<String, Integer> call(ApiCaller ac, Deserializer parms) throws PerunException {
      Map<MemberGroupStatus, Integer> counts = ac.getGroupsManager()
          .getGroupMembersCountsByGroupStatus(ac.getSession(), ac.getGroupById(parms.readInt("group")));

      // convert MemberGroupStatus to String
      return counts.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    }
  },

  /*#
   * Get all groups from all vos. Returned groups are filtered based on the principal rights.
   *
   * @return List<Group> Groups
   */
  /*#
   * Returns all groups in a VO.
   *
   * @throw VoNotExistsException When the Vo doesn't exist
   *
   * @param vo int VO <code>id</code>
   * @return List<Group> Groups
   */
  getAllGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("vo")) {
        return ac.getGroupsManager().getAllGroups(ac.getSession(), ac.getVoById(parms.readInt("vo")));
      }
      return ac.getGroupsManager().getAllGroups(ac.getSession());
    }
  },

  /*#
   * Get all groups with their specified attributes. If the attrNames are null or empty,
   * all group attributes are returned.
   *
   * @param attrNames List<String> list of attribute names to get
   * @return List<RichGroup> list of all groups with specified attributes
   */
  getAllRichGroups {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer params) throws PerunException {
      List<String> attrNames = null;
      if (params.contains("attrNames")) {
        attrNames = params.readList("attrNames", String.class);
      }
      return ac.getGroupsManager().getAllRichGroups(ac.getSession(), attrNames);
    }
  },

  /*#
   * Get page of groups from the given vo.
   * Query parameter specifies offset, page size, sorting order, sorting column and string to search groups by
   * (by default it searches in names, ids, uuids and descriptions), last parameter is optional and by default it
   * finds all groups in vo.
   *
   * @param vo int Vo <code>id</code>
   * @param query GroupsPageQuery Query with page information
   * @param attrNames List<String> List of attribute names
   *
   * @return Paginated<RichGroup> page of requested rich groups
   * @throw VoNotExistsException if there is no such vo
   * @throw MemberNotExistsException if member was specified but not found
   * @throw GroupNotExistsException if group was not found for given parameters
   * @throw MemberGroupMismatchException if given Member is not in specified group
   */
  getGroupsPage {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getGroupsPage(ac.getSession(), ac.getVoById(parms.readInt("vo")), parms.read("query", GroupsPageQuery.class),
              parms.readList("attrNames", String.class));
    }
  },

  /*#
   * Get page of subgroups from the parent group.
   * Query parameter specifies offset, page size, sorting order, sorting column and string to search groups by
   * (by default it searches in names, ids, uuids and descriptions), last parameter is optional and by default it
   * finds all subgroups for the given parent group.
   *
   * @param group int Group <code>id</code>
   * @param query GroupsPageQuery Query with page information
   * @param attrNames List<String> List of attribute names
   *
   * @return Paginated<RichGroup> page of requested rich groups
   * @throw GroupNotExistsException if there is no such query group
   */
  getSubgroupsPage {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getSubgroupsPage(ac.getSession(), ac.getGroupById(parms.readInt("group")),
          parms.read("query", GroupsPageQuery.class), parms.readList("attrNames", String.class));
    }
  },

  /*#
   * Returns all groups in a VO by a hierarchy.
   * Example: [Group => [Group => [Group => []], Group => []]]
   *
   * @param vo int VO <code>id</code>
   * @return List<Object> Groups with subgroups
   */
  getAllGroupsWithHierarchy {
    @Override
    public List<Object> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<Object> convertedGroups = new ArrayList<Object>();
      // Every list must contain as a first field the group object which represents the group. First list contains
      // null on the first position.
      convertedGroups.add(0, null);

      Map<Group, Object> groups =
          ac.getGroupsManager().getAllGroupsWithHierarchy(ac.getSession(), ac.getVoById(parms.readInt("vo")));

      for (Group group : groups.keySet()) {
        convertedGroups.add(ac.convertGroupsWithHierarchy(group, (Map<Group, Object>) groups.get(group)));
      }
      return convertedGroups;
    }
  },

  /*#
   * Returns a parent group of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw ParentGroupNotExistsException When the parent group doesn't exist
   *
   * @param group int Child group <code>id</code>
   * @return Group Parent group
   */
  getParentGroup {
    @Override
    public Group call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getParentGroup(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns direct subgroups of a group (single level)
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param parentGroup int Group id
   * @return List<Group> Child groups
   */
  getSubGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getSubGroups(ac.getSession(), ac.getGroupById(parms.readInt("parentGroup")));
    }
  },

  /*#
   * Returns all subgroups of a group (the whole subtree)
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group id
   * @return List<Group> Child groups
   */
  getAllSubGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getAllSubGroups(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Adds an admin to a group.
   *
   * @throw GroupNotExistsException
   * @throw AlreadyAdminException When user has already been Admin
   * @throw UserNotExistsException When user doesn't exist
   *
   * @param group int Group <code>id</code>
   * @param user int User <code>id</code>
   */
  /*#
   * Adds an group admin to a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw AlreadyAdminException When user has already been the Admin
   *
   * @param group int Group <code>id</code>
   * @param authorizedGroup int Group <code>id</code>
   */
  addAdmin {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("user")) {
        ac.getGroupsManager()
            .addAdmin(ac.getSession(), ac.getGroupById(parms.readInt("group")), ac.getUserById(parms.readInt("user")));
      } else {
        ac.getGroupsManager().addAdmin(ac.getSession(), ac.getGroupById(parms.readInt("group")),
            ac.getGroupById(parms.readInt("authorizedGroup")));
      }
      return null;
    }
  },

  /*#
   * Removes an admin of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw UserNotAdminException When the user is not an admin
   * @throw UserNotExistsException Whern the user doesn't exist
   *
   * @param group int Group <code>id</code>
   * @param user int User <code>id</code>
   */
  /*#
   * Removes a group admin of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupNotAdminException - NO DESCRIPTION
   *
   * @param group int Group <code>id</code>
   * @param authorizedGroup int Group <code>id</code>
   */
  removeAdmin {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("user")) {
        ac.getGroupsManager().removeAdmin(ac.getSession(), ac.getGroupById(parms.readInt("group")),
            ac.getUserById(parms.readInt("user")));
      } else {
        ac.getGroupsManager().removeAdmin(ac.getSession(), ac.getGroupById(parms.readInt("group")),
            ac.getGroupById(parms.readInt("authorizedGroup")));
      }
      return null;
    }
  },

  /*#
   * Get list of all group administrators for supported role and specific group.
   * If some group is administrator of the given group, all VALID members are included in the list.
   * If onlyDirectAdmins is == true, return only direct admins of the group for supported role.
   *
   * Supported roles: GroupAdmin
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @param onlyDirectAdmins int if == true, get only direct user administrators (if == false, get both direct and
   * indirect)
   *
   * @return List<User> list of all group administrators of the given group for supported role
   */
  /*#
   * Returns administrators of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @deprecated
   * @param group int Group <code>id</code>
   * @return List<User> Group admins
   */
  getAdmins {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        if (parms.contains("onlyDirectAdmins")) {
          return AuthzResolver.getAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")), Role.GROUPADMIN,
              parms.readBoolean("onlyDirectAdmins"));
        } else {
          return AuthzResolver.getAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")), Role.GROUPADMIN,
              false);
        }
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Returns direct administrators of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @deprecated
   * @param group int Group <code>id</code>
   * @return List<User> Group admins
   */
  getDirectAdmins {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")), Role.GROUPADMIN, true);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Returns administrator groups of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return List<Group> admins
   */
  getAdminGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getAdminGroups(ac.getSession(), ac.getGroupById(parms.readInt("group")), Role.GROUPADMIN);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get list of all richUser administrators for the group and supported role with specific attributes.
   * If some group is administrator of the given group, all VALID members are included in the list.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * Supported roles: GroupAdmin
   *
   * If "onlyDirectAdmins" is == true, return only direct admins of the group for supported role with specific
   * attributes.
   * If "allUserAttributes" is == true, do not specify attributes through list and return them all in objects
   * richUser. Ignoring list of specific attributes.
   *
   * @param group int Group <code>id</code>
   * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
   * @param allUserAttributes int if == true, get all possible user attributes and ignore list of specificAttributes
   * (if false, get only specific attributes)
   * @param onlyDirectAdmins int if == true, get only direct group administrators (if false, get both direct and
   * indirect)
   *
   * @return List<RichUser> list of RichUser administrators for the group and supported role with attributes
   */
  /*#
   * Get all Group admins as RichUsers
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @deprecated
   * @param group int Group <code>id</code>
   * @return List<RichUser> admins
   */
  getRichAdmins {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        if (parms.contains("onlyDirectAdmins")) {
          return AuthzResolver.getRichAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")),
              parms.readList("specificAttributes", String.class), Role.GROUPADMIN,
              parms.readBoolean("onlyDirectAdmins"), parms.readBoolean("allUserAttributes"));
        } else {
          return AuthzResolver.getRichAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")),
              new ArrayList<>(), Role.GROUPADMIN, false, false);
        }
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Group admins as RichUsers with all their non-null user attributes
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw UserNotExistsException When the user doesn't exist
   *
   * @deprecated
   * @param group int Group <code>id</code>
   * @return List<RichUser> admins with attributes
   */
  getRichAdminsWithAttributes {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getRichAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")), new ArrayList<>(),
            Role.GROUPADMIN, false, true);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   *
   * Get list of all richGroups with all attributes assigned to the resource filtered by specific member.
   * Allowed namespaces of attributes are group, group-resource and member-group.
   *
   * @throw MemberNotExistsException When the member doesn't exist
   * @throw ResourceNotExistsException When the resource doesn't exist
   *
   * @param member int Member <code>id</code>
   * @param resource int Resource <code>id</code>
   * @return List<RichGroup> groups with all group, group-resource and member-group attributes (non-empty)
   */
  /*#
   *
   * Get list of all richGroups with selected attributes assigned to the resource filtered by specific member.
   * Allowed namespaces of attributes are group, group-resource and member-group.
   *
   * You will get only attributes which you are authorized to read. You must specify names of requested attributes
   * by their URNs in attrNames. Empty list means no attributes to return.
   *
   * @throw MemberNotExistsException When the member doesn't exist
   * @throw ResourceNotExistsException When the resource doesn't exist
   *
   * @param member int Member <code>id</code>
   * @param resource int Resource <code>id</code>
   * @param attrNames List<String> names of attributes
   * @return List<RichGroup> groups with attributes
   */
  /*#
   *
   * Get list of all richGroups with all attributes assigned to resource.
   * Allowed namespaces of attributes are group and group-resource.
   *
   * @throw MemberNotExistsException When the member doesn't exist
   * @throw ResourceNotExistsException When the resource doesn't exist
   *
   * @param resource int Resource <code>id</code>
   * @return List<RichGroup> groups with all group and group-resource attributes (non-empty)
   */
  /*#
   *
   * Get list of all richGroups with selected attributes assigned to resource.
   * Allowed namespaces of attributes are group and group-resource.
   *
   * You will get only attributes which you are authorized to read. You must specify names of requested attributes
   * by their URNs in attrNames.
   *
   * @throw MemberNotExistsException When the member doesn't exist
   * @throw ResourceNotExistsException When the resource doesn't exist
   *
   * @param resource int Resource <code>id</code>
   * @param attrNames List<String> names of attributes
   * @return List<RichGroup> groups with attributes
   */
  getRichGroupsAssignedToResourceWithAttributesByNames {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }

      if (parms.contains("member")) {
        return ac.getGroupsManager().getRichGroupsAssignedToResourceWithAttributesByNames(ac.getSession(),
            ac.getMemberById(parms.readInt("member")), ac.getResourceById(parms.readInt("resource")), attrNames);
      } else {
        return ac.getGroupsManager().getRichGroupsAssignedToResourceWithAttributesByNames(ac.getSession(),
            ac.getResourceById(parms.readInt("resource")), attrNames);
      }
    }
  },

  /*#
   * Get all Group admins as RichUsers with specific attributes (from user namespace)
   *
   * @throw VoNotExistsException When the Vo doesn't exist
   *
   * @deprecated
   * @param group int Group <code>id</code>
   * @param specificAttributes List<String> list of attributes URNs
   * @return List<RichUser> admins with attributes
   */
  getRichAdminsWithSpecificAttributes {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

      try {
        return AuthzResolver.getRichAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")),
            parms.readList("specificAttributes", String.class), Role.GROUPADMIN, false, false);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Group admins, which are assigned directly,
   *  as RichUsers with specific attributes (from user namespace)
   *
   * @throw VoNotExistsException When the Vo doesn't exist
   *
   * @deprecated
   * @param group int Group <code>id</code>
   * @param specificAttributes List<String> list of attributes URNs
   * @return List<RichUser> direct admins with attributes
   */
  getDirectRichAdminsWithSpecificAttributes {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

      try {
        return AuthzResolver.getRichAdmins(ac.getSession(), ac.getGroupById(parms.readInt("group")),
            parms.readList("specificAttributes", String.class), Role.GROUPADMIN, true, false);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Returns direct descendant groups of a VO.
   *
   * @throw VoNotExistsException When the Vo doesn't exist
   *
   * @param vo int VO <code>id</code>
   * @return List<Group> Children groups
   */
  getGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getGroups(ac.getSession(), ac.getVoById(parms.readInt("vo")));

    }
  },

  /*#
   * Returns groups count in a VO.
   *
   * @throw VoNotExistsException When the Vo doesn't exist
   *
   * @param vo int VO <code>id</code>
   * @return int Groups count
   */
  /*#
   * Gets count of all groups.

   * @return int groups count
   */
  getGroupsCount {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("vo")) {
        return ac.getGroupsManager().getGroupsCount(ac.getSession(), ac.getVoById(parms.readInt("vo")));
      } else {
        return ac.getGroupsManager().getGroupsCount(ac.getSession());
      }
    }
  },

  /*#
   * Returns subgroups count of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param parentGroup int Parent group <code>id</code>
   * @return int Subgroups count
   */
  getSubGroupsCount {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager().getSubGroupsCount(ac.getSession(), ac.getGroupById(parms.readInt("parentGroup")));
    }
  },

  /*#
   * Delete all groups in a VO.
   *
   * @throw VoNotExistsException When the Vo doesn't exist
   * @throw GroupAlreadyRemovedException If there is at least 1 group not affected by deleting from DB
   * @throw GroupAlreadyRemovedFromResourceException If there is at least 1 group on resource affected by deleting
   * from DB
   * @throw GroupRelationDoesNotExist When the group relation doesn't exist
   * @throw GroupRelationCannotBeRemoved When the group relation cannot be removed
   *
   * @param vo int VO <code>id</code>
   */
  deleteAllGroups {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getGroupsManager().deleteAllGroups(ac.getSession(), ac.getVoById(parms.readInt("vo")));
      return null;
    }
  },

  /*#
   * Forces group synchronization.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupSynchronizationAlreadyRunningException When the group synchronization has already been running
   * @throw GroupSynchronizationNotEnabledException When group doesn't have synchronization enabled
   *
   * @param group int Group <code>id</code>
   */
  forceGroupSynchronization {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

      ac.getGroupsManager().forceGroupSynchronization(ac.getSession(), ac.getGroupById(parms.readInt("group")));
      return null;
    }
  },

  /*#
   * Force synchronization for all subgroups (recursively - whole tree) of the group (useful for group structure)
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   */
  forceAllSubGroupsSynchronization {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

      ac.getGroupsManager().forceAllSubGroupsSynchronization(ac.getSession(), ac.getGroupById(parms.readInt("group")));
      return null;
    }
  },

  /*#
   * Forces group structure synchronization.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   * @throw GroupStructureSynchronizationAlreadyRunningException When the group structure synchronization has already
   *  been running
   *
   * @param group int Group <code>id</code>
   */
  forceGroupStructureSynchronization {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getGroupsManager()
          .forceGroupStructureSynchronization(ac.getSession(), ac.getGroupById(parms.readInt("group")));
      return null;
    }
  },

  /*#
   * Returns parent VO of a group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Group <code>id</code>
   * @return Vo Parent VO
   */
  getVo {
    @Override
    public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getVo(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns members of a parent group.
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int Child group <code>id</code>
   * @return List<Member> Parent group members
   */
  getParentGroupMembers {
    @Override
    public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getParentGroupMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns members of a parent group.
   * RichMember contains User object.
   *
   * @param group int Child group <code>id</code>
   * @return List<RichMember> Parent group members
   */
  getParentGroupRichMembers {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getParentGroupRichMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns members of a parent group.
   * RichMember contains User object and attributes.
   *
   * @param group int Child group <code>id</code>
   * @return List<RichMember> Parent group members
   */
  getParentGroupRichMembersWithAttributes {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager()
          .getParentGroupRichMembersWithAttributes(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Returns groups for a member.
   *
   * @param member int Member <code>id</code>
   * @return List<Group> Groups of the member
   */
  getMemberGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getMemberGroups(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   * Returns groups with specific attribute for a member.
   *
   * @throw WrongAttributeAssignmentException Thrown while assigning atribute to wrong entity.
   * @throw MemberNotExistsException When the member doesn't exist
   * @throw AttributeNotExistsException When the attribute doesn't exist
   *
   * @param member int Member <code>id</code>
   * @param attribute Attribute attribute object with value
   * @return List<Group> Groups of the member
   */
  getMemberGroupsByAttribute {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager()
          .getMemberGroupsByAttribute(ac.getSession(), ac.getMemberById(parms.readInt("member")),
              parms.read("attribute", Attribute.class));
    }
  },


  /*#
   * Returns sub-list of all RichGroups, each containing selected attributes, starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role, each containing selected attributes, starting at
   * fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role assignment type, each containing selected attributes,
   *  starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role and its assignment type, each containing selected
   * attributes, starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups, each containing all attributes, starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role, each containing all attributes, starting at
   * fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param fromIndex int begin index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role assignment type, each containing all attributes,
   * starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @param fromIndex int begin index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role and its assignment type, each containing all
   * attributes, starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @param fromIndex int begin index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups, each containing selected attributes, starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role, each containing selected attributes, starting at
   * first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role assignment type, each containing selected attributes,
   *  starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role and its assignment type, each containing selected
   * attributes, starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups, each containing all attributes, starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role, each containing all attributes, starting at first
   * index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role assignment type, each containing all attributes,
   * starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role and its assignment type, each containing all
   * attributes, starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups, each containing selected attributes, starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role, each containing selected attributes, starting at
   * fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role assignment type, each containing selected attributes,
   *  starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role and its assignment type, each containing selected
   * attributes, starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of all RichGroups, each containing all attributes, starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role, each containing all attributes, starting at
   * fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role assignment type, each containing all attributes,
   * starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns sub-list of all RichGroups filtered by user's role and its assignment type, each containing all
   * attributes, starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns full list of all RichGroups containing selected attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of all RichGroups filtered by user's role, containing selected attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of all RichGroups filtered by user's role assignment type, containing selected attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of all RichGroups filtered by user's role and its assignment type, containing selected
   * attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of all RichGroups containing all attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns full list of all RichGroups filtered by user's role, containing all attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns full list of all RichGroups filtered by user's role assignment type, containing all attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing all attributes
   */
  /*#
   * Returns full list of all RichGroups filtered by user's role and its assignment type, containing all attributes.
   *
   * @throw VoNotExistsException When Vo doesn't exist
   *
   * @param vo int <code>id</code> of vo
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing all attributes
   */
  getAllRichGroupsWithAttributesByNames {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<RichGroup> listOfRichGroups = ac.getGroupsManager()
          .getAllRichGroupsWithAttributesByNames(ac.getSession(), ac.getVoById(parms.readInt("vo")),
              parms.contains("attrNames") ? parms.readList("attrNames", String.class) : null,
              parms.contains("roles") ? parms.readList("roles", String.class) : new ArrayList<>(),
              parms.contains("types") ?
                  parms.readList("types", String.class).stream().map(RoleAssignmentType::valueOf).toList() :
                  new ArrayList<>());

      if (listOfRichGroups == null) {
        listOfRichGroups = new ArrayList<>();
      }

      if (parms.contains("fromIndex") && parms.contains("toIndex")) {
        return ac.getSublist(listOfRichGroups, parms.readInt("fromIndex"), parms.readInt("toIndex"));
      } else if (parms.contains("fromIndex")) {
        int toIndex = listOfRichGroups.size();
        return ac.getSublist(listOfRichGroups, parms.readInt("fromIndex"), toIndex);
      } else if (parms.contains("toIndex")) {
        return ac.getSublist(listOfRichGroups, 0, parms.readInt("toIndex"));
      } else {
        return listOfRichGroups;
      }
    }
  },

  /*#
   * Returns sub-list of member's RichGroups, each containing selected attributes, starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role, each containing selected attributes, starting
   * at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role assignment type, each containing selected
   * attributes, starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role and its assignment type, each containing
   * selected attributes, starting at fromIndex (included)
   * and ending at the size of the original list.
   *
   * Example: [1,2,3,4], fromIndex=1 => [2,3,4]
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups, each containing selected attributes, starting at first index of the
   * original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role, each containing selected attributes, starting
   * at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role assignment type, each containing selected
   * attributes, starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role and its assignment type, each containing
   * selected attributes, starting at first index of the original
   * list (included) and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], toIndex=2 => [1,2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups, each containing selected attributes, starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role, each containing selected attributes, starting
   * at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role assignment type, each containing selected
   * attributes, starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns sub-list of member's RichGroups filtered by user's role and its assignment type, each containing
   * selected attributes, starting at fromIndex (included)
   * and ending at the toIndex (included).
   *
   * Example: [1,2,3,4], fromIndex=1, toIndex=2 => [2,3]
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param fromIndex int begin index of returned subList, included
   * @param toIndex int end index of returned subList, included
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of member's RichGroups containing selected attributes.
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of member's RichGroups filtered by user's role, containing selected attributes.
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of member's RichGroups filtered by user's role assignment type, containing selected attributes.
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns full list of member's RichGroups filtered by user's role and its assignment type, containing selected
   * attributes.
   *
   *
   * "members" group is not included!
   *
   * Supported are attributes from these namespaces:
   *  - group
   *  - member-group
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  getMemberRichGroupsWithAttributesByNames {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<RichGroup> listOfRichGroups = ac.getGroupsManager()
          .getMemberRichGroupsWithAttributesByNames(ac.getSession(), ac.getMemberById(parms.readInt("member")),
              parms.readList("attrNames", String.class),
              parms.contains("roles") ? parms.readList("roles", String.class) : new ArrayList<>(),
              parms.contains("types") ?
                  parms.readList("types", String.class).stream().map(RoleAssignmentType::valueOf).toList() :
                  new ArrayList<>());

      if (listOfRichGroups == null) {
        listOfRichGroups = new ArrayList<>();
      }

      if (parms.contains("fromIndex") && parms.contains("toIndex")) {
        return ac.getSublist(listOfRichGroups, parms.readInt("fromIndex"), parms.readInt("toIndex"));
      } else if (parms.contains("fromIndex")) {
        int toIndex = listOfRichGroups.size();
        return ac.getSublist(listOfRichGroups, parms.readInt("fromIndex"), toIndex);
      } else if (parms.contains("toIndex")) {
        return ac.getSublist(listOfRichGroups, 0, parms.readInt("toIndex"));
      } else {
        return listOfRichGroups;
      }
    }
  },

  /*#
   * Returns RichSubGroups from parent group containing selected attributes (only 1 level sub groups).
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int <code>id</code> of group
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  getRichSubGroupsWithAttributesByNames {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager()
          .getRichSubGroupsWithAttributesByNames(ac.getSession(), ac.getGroupById(parms.readInt("group")),
              parms.readList("attrNames", String.class));
    }
  },

  /*#
   * Returns all AllRichSubGroups from parent group containing selected attributes (all level subgroups).
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int <code>id</code> of group
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns all AllRichSubGroups from parent group filtered by user's role, containing selected attributes (all
   * level subgroups).
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int <code>id</code> of group
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns all AllRichSubGroups from parent group filtered by user's role assignment type, containing selected
   * attributes (all level subgroups).
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int <code>id</code> of group
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  /*#
   * Returns all AllRichSubGroups from parent group filtered by user's role and its assignment type, containing
   * selected attributes (all level subgroups).
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param group int <code>id</code> of group
   * @param attrNames List<String> if attrNames is null method will return RichGroups containing all attributes
   * @param roles list of selected roles (if empty, then return groups by all roles)
   * @param types list of selected types of roles (if empty, then return by roles of all types)
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  getAllRichSubGroupsWithAttributesByNames {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getAllRichSubGroupsWithAttributesByNames(ac.getSession(), ac.getGroupById(parms.readInt("group")),
              parms.readList("attrNames", String.class),
              parms.contains("roles") ? parms.readList("roles", String.class) : new ArrayList<>(),
              parms.contains("types") ?
                  parms.readList("types", String.class).stream().map(RoleAssignmentType::valueOf).toList() :
                  new ArrayList<>());
    }
  },

  /*#
   * Returns RichGroup selected by id containing selected attributes
   *
   * @throw GroupNotExistsException When the group doesn't exist
   *
   * @param groupId int <code>id</code> of group
   * @param attrNames List<String> if attrNames is null method will return RichGroup containing all attributes
   * @return List<RichGroup> RichGroups containing selected attributes
   */
  getRichGroupByIdWithAttributesByNames {
    @Override
    public RichGroup call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getRichGroupByIdWithAttributesByNames(ac.getSession(), parms.readInt("groupId"),
          parms.contains("attrNames") ? parms.readList("attrNames", String.class) : null);
    }
  },

  /*#
   * Returns all groups of specific member including group "members".
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @return List<Group> Groups of member
   */
  getAllMemberGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getAllMemberGroups(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   * Returns all member's groups where member is in active state (is valid there)
   * Excluded members group.
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @return List<Group> Groups where member is in active state (valid)
   */
  getGroupsWhereMemberIsActive {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager()
          .getGroupsWhereMemberIsActive(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   * Returns all member's groups where member is in inactive state (it is not valid and it is expired there)
   * Excluded members group.
   *
   * @throw MemberNotExistsException When the member doesn't exist
   *
   * @param member int <code>id</code> of member
   * @return List<Group> Groups where member is in inactive state (expired)
   */
  getGroupsWhereMemberIsInactive {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager()
          .getGroupsWhereMemberIsInactive(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /**
   * Returns all member's groups where member is in active state (is valid there) Included members group.
   *
   * @throw MemberNotExistsException When the member doesn't exist
   * @param member int <code>id</code> of member
   * @return List<Group> All groups where member is in active state (valid)
   */
  getAllGroupsWhereMemberIsActive {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager()
          .getAllGroupsWhereMemberIsActive(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   * Returns all user's groups where user is in active state (is valid there) and are subgroups of the specified VO
   * Excluded members group.
   *
   * @throw UserNotExistsException When the user does not exist
   * @throw VoNotExistsException When the vo doesn't exist
   *
   * @param user int <code>id</code> of user
   * @param vo int <code>id</code> of vo in which groups are looked up
   * @return List<Group> Groups where user is in active state (valid), if the user is member of the given vo
   */
  getGroupsWhereUserIsActiveMember {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getGroupsWhereUserIsActiveMember(ac.getSession(), ac.getUserById(parms.readInt("user")),
              ac.getVoById(parms.readInt("vo")));
    }
  },

  /*#
   * Return all members of the group who are active (valid) in the group.
   *
   * Do not return expired members of the group.
   *
   * @throw GroupNotExistsException When the group does not exist
   *
   * @param group int <code>id</code> of group
   * @return List<Member> list of active (valid) members of the group
   */
  getActiveGroupMembers {
    @Override
    public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getActiveGroupMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Return all members of the group who are inactive (expired) in the group.
   *
   * Do not return active members of the group.
   *
   * @throw GroupNotExistsException When the group does not exist
   *
   * @param group int <code>id</code> of group
   * @return List<Member> list of inactive (expired) members of the group
   */
  getInactiveGroupMembers {
    @Override
    public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getGroupsManager().getInactiveGroupMembers(ac.getSession(), ac.getGroupById(parms.readInt("group")));
    }

  },

  /*#
   * Set membership status of a member in a group. Please note, that resulting Status after change is
   * calculated from all members sub-groups and groups in relation sourcing this member. If in any of them
   * is VALID, resulting status is still VALID.
   *
   * @param member int Member <code>id</code>
   * @param group int Group <code>id</code>
   * @param status String VALID | EXPIRED
   * @exampleParam status "EXPIRED"
   * @return Member Member with status after change
   */
  setGroupsMemberStatus {
    @Override
    public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      MemberGroupStatus status = MemberGroupStatus.valueOf(parms.readString("status"));
      return ac.getGroupsManager().setMemberGroupStatus(ac.getSession(), ac.getMemberById(parms.readInt("member")),
          ac.getGroupById(parms.readInt("group")), status);
    }
  },

  /*#
   * Suspend synchronizing groups and their structures. Groups being currently synchronized will finish.<<<
   *
   */
  suspendGroupSynchronization {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getGroupsManager().suspendGroupSynchronization(ac.getSession(), true);
      return null;
    }
  },

  /*#
   * Resumes previously suspended group (and group structure) synchronization.
   *
   */
  resumeGroupSynchronization {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getGroupsManager().suspendGroupSynchronization(ac.getSession(), false);
      return null;
    }
  },

  /*#
   * Extend member membership in given group using membershipExpirationRules attribute defined in Group.
   *
   * @param member int Member <code>id</code>
   * @param group int Group <code>id</code>
   *
   * @throw GroupNotExistsException If any group not exists in perun
   * @throw MemberNotExistsException When member doesn't exist
   * @throw ExtendMembershipException
   */
  extendMembershipInGroup {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getGroupsManager().extendMembershipInGroup(ac.getSession(), ac.getMemberById(parms.readInt("member")),
          ac.getGroupById(parms.readInt("group")));
      return null;
    }
  },

  /*#
   * Returns <code>1 == true</code> if member in given group can extend membership or if no rules were set for the
   * membershipExpiration
   * Otherwise return <code>0 == false</code>.
   *
   * @param member int Member <code>id</code>
   * @return int 1 if true | 0 if false
   *
   * @throw GroupNotExistsException If any group not exists in perun
   * @throw MemberNotExistsException When member doesn't exist
   */
  canExtendMembershipInGroup {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (ac.getGroupsManager().canExtendMembershipInGroup(ac.getSession(), ac.getMemberById(parms.readInt("member")),
          ac.getGroupById(parms.readInt("group")))) {
        return 1;
      } else {
        return 0;
      }
    }
  },

  /*#
   * Get unique paths of groups via which member is indirectly included to the group.
   * Cuts off after first included group.
   *
   * @param sess perun session
   * @param member member
   * @param group group in which the member is indirectly included
   * @return lists of groups [CURRENT GROUP -> SUBGROUP -> ... -> MEMBER'S SOURCE GROUP]
   */
  getIndirectMembershipPaths {
    @Override
    public List<List<Group>> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getIndirectMembershipPaths(ac.getSession(), ac.getMemberById(parms.readInt("member")),
              ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Get member in context of group.
   *
   * @param sess perun session
   * @param group int Group <code>id</code>
   * @param member int Member <code>id</code>
   * @return Member member in context of group
   */
  getGroupMemberById {
    @Override
    public Member call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getGroupMemberById(ac.getSession(), ac.getGroupById(parms.readInt("group")), parms.readInt("member"));
    }
  },

  /*#
   * Returns list of RichMembers with requested attributes by their member IDs from given group.
   * Skips invalid member IDs (unknown or not members of group).
   * Supports member, member-group (stored in memberAttributes) and user attributes (stored in userAttributes).
   *
   * @param group int Group <code>id</code>
   * @param members List<Integer> <code>id</code> of members to be returned
   * @param attrNames List<String> names of attributes
   * @return List<RichMember> Group members
   */
  getGroupRichMembersByIds {
    @Override
    public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .getGroupRichMembersByIds(ac.getSession(), parms.readInt("group"), parms.readList("members", Integer.class),
              parms.readList("attrNames", String.class));
    }
  },

  /*#
   * Sets flag required for including group to parent vo in a vo hierarchy.
   *
   * @param group id of group
   * @param vo id of parent vo
   * @throw VoNotExistsException if vo does not exist
   * @throw GroupNotExistsException if group does not exist
   * @throw RelationNotExistsException if group is not from parent vo's member vos
   * @throw RelationExistsException if group is already allowed to be included to parent vo
   */
  allowGroupToHierarchicalVo {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getGroupsManager().allowGroupToHierarchicalVo(ac.getSession(), ac.getGroupById(parms.readInt("group")),
          ac.getVoById(parms.readInt("vo")));
      return null;
    }
  },

  /*#
   * Sets flag required for including groups to parent vo in a vo hierarchy.
   *
   * @param groups List<Integer> <code>id</code> of groups
   * @param vo int Vo <code>id</code>
   * @throw VoNotExistsException if vo does not exist
   * @throw GroupNotExistsException if group does not exist
   * @throw RelationNotExistsException if group is not from parent vo's member vos
   * @throw RelationExistsException if group is already allowed to be included to parent vo
   */
  allowGroupsToHierarchicalVo {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<Group> groups = new ArrayList<>();
      for (Integer groupId : parms.readList("groups", Integer.class)) {
        groups.add(ac.getGroupById(groupId));
      }

      Vo vo = ac.getVoById(parms.readInt("vo"));

      ac.getGroupsManager().allowGroupsToHierarchicalVo(ac.getSession(), groups, vo);

      return null;
    }
  },

  /*#
   * Unsets flag required for including group to parent vo in a vo hierarchy.
   *
   * @param group id of group
   * @param vo id of parent vo
   * @throw VoNotExistsException if vo does not exist
   * @throw GroupNotExistsException if group does not exist
   * @throw RelationNotExistsException if group is not allowed to be included in parent vo
   */
  disallowGroupToHierarchicalVo {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      ac.getGroupsManager().disallowGroupToHierarchicalVo(ac.getSession(), ac.getGroupById(parms.readInt("group")),
          ac.getVoById(parms.readInt("vo")));
      return null;
    }
  },

  /*#
   * Unsets flag required for including groups to parent vo in a vo hierarchy.
   *
   * @param groups List<Integer> <code>id</code> of groups
   * @param vo int VO <code>id</code>
   * @throw VoNotExistsException if vo does not exist
   * @throw GroupNotExistsException if group does not exist
   * @throw RelationNotExistsException if group is not allowed to be included in parent vo
   */
  disallowGroupsToHierarchicalVo {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<Group> groups = new ArrayList<>();
      for (Integer groupId : parms.readList("groups", Integer.class)) {
        groups.add(ac.getGroupById(groupId));
      }

      Vo vo = ac.getVoById(parms.readInt("vo"));

      ac.getGroupsManager().disallowGroupsToHierarchicalVo(ac.getSession(), groups, vo);

      return null;
    }
  },

  /*#
   * Returns flag representing if the group can be included in the (parent) vo's groups
   *
   * @param group id of group
   * @param vo id of parent vo
   * @return boolean true if group can be included in vo's groups, false otherwise
   *
   * @throw VoNotExistsException if vo does not exist
   * @throw GroupNotExistsException if group does not exist
   */
  isAllowedGroupToHierarchicalVo {
    @Override
    public Boolean call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGroupsManager()
          .isAllowedGroupToHierarchicalVo(ac.getSession(), ac.getGroupById(parms.readInt("group")),
              ac.getVoById(parms.readInt("vo")));
    }
  },

  /*#
   * Returns all groups which can be included to VO.
   *
   * @param sess perun session
   * @param vo int parent VO <code>id</code>
   * @return list of groups allowed to hierarchical VO.
   * @throw VoNotExistsException if given VO does not exist
   */
  /*#
   * Returns groups which can be included to VO from specific member VO.
   *
   * @param sess perun session
   * @param vo int parent VO <code>id</code>
   * @param memberVo int member VO <code>id</code>
   * @return list of groups allowed to hierarchical VO.
   * @throw VoNotExistsException if given VO does not exist
   */
  getAllAllowedGroupsToHierarchicalVo {
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("memberVo")) {
        return ac.getGroupsManager()
            .getAllAllowedGroupsToHierarchicalVo(ac.getSession(), ac.getVoById(parms.readInt("vo")),
                ac.getVoById(parms.readInt("memberVo")));
      }
      return ac.getGroupsManager()
          .getAllAllowedGroupsToHierarchicalVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
    }

  };
}
