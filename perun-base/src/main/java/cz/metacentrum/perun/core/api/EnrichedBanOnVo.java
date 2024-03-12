package cz.metacentrum.perun.core.api;

import java.util.Objects;

/**
 * @author Matej Hakoš <492968@muni.cz>
 */
public class EnrichedBanOnVo {
  private Vo vo;
  private RichMember member;
  private BanOnVo ban;

  public EnrichedBanOnVo() {
  }

  public EnrichedBanOnVo(RichMember member, Vo vo, BanOnVo ban) {
    this.ban = ban;
    this.vo = vo;
    this.member = member;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    EnrichedBanOnVo other = (EnrichedBanOnVo) obj;
    return vo.equals(other.vo) && member.equals(other.member) && ban.equals(other.ban);
  }

  public BanOnVo getBan() {
    return ban;
  }

  public void setBan(BanOnVo ban) {
    this.ban = ban;
  }

  public RichMember getMember() {
    return member;
  }

  public void setMember(RichMember member) {
    this.member = member;
  }

  public Vo getVo() {
    return vo;
  }

  public void setVo(Vo vo) {
    this.vo = vo;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getVo(), getMember(), getBan());
  }

  @Override
  public String toString() {
    return "EnrichedBanOnVo{'" + "member=" + member + ", vo=" + vo + ", ban=" + ban + "}";
  }
}
