package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Vo;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForVo extends AuditEvent {

  private Attribute attribute;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeSetForVo() {
  }

  public AttributeSetForVo(Attribute attribute, Vo vo) {
    this.attribute = attribute;
    this.vo = vo;
    this.message = formatMessage("%s set for %s.", attribute, vo);
  }

  public Attribute getAttribute() {
    return attribute;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Vo getVo() {
    return vo;
  }

  @Override
  public String toString() {
    return message;
  }
}
