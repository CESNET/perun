package cz.metacentrum.perun.core.api;


import java.util.List;
import java.util.Objects;

/**
 * Represents VO with its hierarchy (parent + member VOS).
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class EnrichedVo {
  private Vo vo;
  private List<Vo> memberVos;
  private List<Vo> parentVos;

  public EnrichedVo() {
  }

  public EnrichedVo(Vo vo, List<Vo> memberVos, List<Vo> parentVos) {
    this.vo = vo;
    this.memberVos = memberVos;
    this.parentVos = parentVos;
  }

  public Vo getVo() {
    return vo;
  }

  public void setVo(Vo vo) {
    this.vo = vo;
  }

  public List<Vo> getMemberVos() {
    return memberVos;
  }

  public void setMemberVos(List<Vo> memberVos) {
    this.memberVos = memberVos;
  }

  public List<Vo> getParentVos() {
    return parentVos;
  }

  public void setParentVos(List<Vo> parentVos) {
    this.parentVos = parentVos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedVo that = (EnrichedVo) o;
    return Objects.equals(getVo(), that.getVo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getVo());
  }

  @Override
  public String toString() {
    return "EnrichedVo{" +
        "vo=" + vo +
        ", memberVos=" + memberVos +
        ", parentVos=" + parentVos +
        "}";
  }
}
