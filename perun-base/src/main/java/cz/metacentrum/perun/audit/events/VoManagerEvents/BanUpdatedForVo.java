package cz.metacentrum.perun.audit.events.VoManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnVo;

public class BanUpdatedForVo extends AuditEvent {

  private BanOnVo banOnVo;
  private int memberId;
  private int voId;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public BanUpdatedForVo() {
  }

  public BanUpdatedForVo(BanOnVo banOnVo, int memberId, int voId) {
    this.banOnVo = banOnVo;
    this.memberId = memberId;
    this.voId = voId;
    this.message = formatMessage("Ban %s was updated for memberId %d on voId %d.", banOnVo, memberId, voId);
  }

  public BanOnVo getBanOnVo() {
    return banOnVo;
  }

  public int getMemberId() {
    return memberId;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public int getVoId() {
    return voId;
  }

  @Override
  public String toString() {
    return message;
  }

}
