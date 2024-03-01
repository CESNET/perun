package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Vo;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForVo extends AuditEvent {

  private AttributeDefinition attribute;
  private Vo vo;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForVo() {
  }

  public AttributeRemovedForVo(AttributeDefinition attribute, Vo vo) {
    this.attribute = attribute;
    this.vo = vo;
    this.message = formatMessage("%s removed for %s.", attribute, vo);
  }

  public AttributeDefinition getAttribute() {
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
