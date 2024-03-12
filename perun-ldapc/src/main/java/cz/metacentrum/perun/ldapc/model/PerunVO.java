package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.List;

public interface PerunVO extends PerunEntry<Vo> {

  public void addMemberToVO(int voId, Member member);

  /**
   * Create vo in LDAP.
   *
   * @param vo the vo
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void addVo(Vo vo);

  /**
   * Delete existing vo in LDAP.
   *
   * @param vo the vo
   * @throws InternalErrorException if NameNotFoundException is thrown
   */
  public void deleteVo(Vo vo);

  /**
   * Find Vo in LDAP and return shortName of this Vo.
   *
   * @param voId vo id
   * @return shortName of vo with vo id
   * @throws InternalErrorException if shortName has not right format (null, not exists, 0 length, more than 1 shortName
   *                                exist)
   */
  public String getVoShortName(int voId);

  public void removeMemberFromVO(int voId, Member member);

  public void synchronizeMembers(Vo vo, List<Member> members);

  public void synchronizeVo(Vo vo, Iterable<Attribute> attrs, List<Member> members);

  public void updateVo(Vo vo);

}
