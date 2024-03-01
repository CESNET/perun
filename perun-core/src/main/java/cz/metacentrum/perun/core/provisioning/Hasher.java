package cz.metacentrum.perun.core.provisioning;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;

/**
 * Component used to generate hashes for objects.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface Hasher {

  /**
   * Returns hash for facility.
   *
   * @param facility facility
   * @return facility hash
   */
  String hashFacility(Facility facility);

  /**
   * Returns hash for resource.
   *
   * @param resource resource
   * @return resource hash
   */
  String hashResource(Resource resource);

  /**
   * Returns hash for member.
   *
   * @param member member
   * @return member hash
   */
  String hashMember(Member member);

  /**
   * Returns hash for vo.
   *
   * @param vo vo
   * @return vo hash
   */
  String hashVo(Vo vo);

  /**
   * Returns hash for group.
   *
   * @param group group
   * @return group hash
   */
  String hashGroup(Group group);

  /**
   * Returns hash for group and resource.
   *
   * @param group    group
   * @param resource resource
   * @return group-resource hash
   */
  String hashGroupResource(Group group, Resource resource);

  /**
   * Returns hash for member and resource.
   *
   * @param member   member
   * @param resource resource
   * @return member-resource hash
   */
  String hashMemberResource(Member member, Resource resource);

  /**
   * Returns hash for member and group.
   *
   * @param member member
   * @param group  group
   * @return member-group hash
   */
  String hashMemberGroup(Member member, Group group);

  /**
   * Returns hash for user.
   *
   * @param user user
   * @return user hash
   */
  String hashUser(User user);

  /**
   * Returns hash for user and facility.
   *
   * @param user     user
   * @param facility facility
   * @return user-facility hash
   */
  String hashUserFacility(User user, Facility facility);
}
