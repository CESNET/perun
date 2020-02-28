package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;

import java.util.List;

public abstract class AbstractPerunAttributeConfigurer<T extends PerunBean> implements PerunAttributeConfigurer<T> {

	private List<PerunAttribute<T>> attributeDescriptions;

	public List<PerunAttribute<T>> getAttributeDescriptions() {
		return attributeDescriptions;
	}

	public void setAttributeDescriptions(List<PerunAttribute<T>> attributeDescriptions) {
		this.attributeDescriptions = attributeDescriptions;
	}

}
