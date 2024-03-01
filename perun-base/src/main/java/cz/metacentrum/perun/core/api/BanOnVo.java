package cz.metacentrum.perun.core.api;

import java.util.Date;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class BanOnVo extends Ban {
  private int memberId;
  private int voId;

  public BanOnVo() {
  }

  public BanOnVo(int id, int memberId, int voId, Date validityTo, String description) {
    super(id, validityTo, description);
    this.memberId = memberId;
    this.voId = voId;
  }

  public int getMemberId() {
    return memberId;
  }

  public void setMemberId(int memberId) {
    this.memberId = memberId;
  }

  @Override
  public int getSubjectId() {
    return memberId;
  }

  @Override
  public int getTargetId() {
    return voId;
  }

  @Override
  public String getType() {
    return this.getClass().getSimpleName();
  }

  public int getVoId() {
    return voId;
  }

  public void setVoId(int voId) {
    this.voId = voId;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();

    Long validityInMiliseconds = null;
    if (getValidityTo() != null) {
      validityInMiliseconds = getValidityTo().getTime();
    }

    return str.append(getClass().getSimpleName()).append(":[id='").append(getId()).append("', memberId='")
        .append(memberId).append("', voId='").append(voId).append("', validityTo='").append(validityInMiliseconds)
        .append("', description='").append(getDescription()).append("']").toString();
  }
}
