package cz.metacentrum.perun.core.provisioning;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;

/**
 * Generates hashes based on ids of the beans.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class IdHasher implements Hasher {

  @Override
  public String hashFacility(Facility facility) {
    return "f-" + facility.getId();
  }

  @Override
  public String hashGroup(Group group) {
    return "g-" + group.getId();
  }

  @Override
  public String hashGroupResource(Group group, Resource resource) {
    return "g-r-" + group.getId() + "-" + resource.getId();
  }

  @Override
  public String hashMember(Member member) {
    return "m-" + member.getId();
  }

  @Override
  public String hashMemberGroup(Member member, Group group) {
    return "m-g-" + member.getId() + "-" + group.getId();
  }

  @Override
  public String hashMemberResource(Member member, Resource resource) {
    return "m-r-" + member.getId() + "-" + resource.getId();
  }

  @Override
  public String hashResource(Resource resource) {
    return "r-" + resource.getId();
  }

  @Override
  public String hashUser(User user) {
    return "u-" + user.getId();
  }

  @Override
  public String hashUserFacility(User user, Facility facility) {
    return "u-f-" + user.getId() + "-" + facility.getId();
  }

  @Override
  public String hashVo(Vo vo) {
    return "v-" + vo.getId();
  }
}
