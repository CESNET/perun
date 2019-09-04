package cz.metacentrum.perun.ldapc.beans;

import java.util.List;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;

public interface PerunAttributeConfigurer<T extends PerunBean> {

	public List<PerunAttribute<T>> getAttributeDescriptions();
	
}
