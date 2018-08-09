package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Vo;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForVo extends AuditEvent {

	private final Attribute attribute;
	private final Vo vo;
	private final String message;

	public AttributeSetForVo(Attribute attribute, Vo vo) {
		this.attribute = attribute;
		this.vo = vo;
		this.message = String.format("%s set for %s.", attribute, vo);
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public Vo getVo() {
		return vo;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
