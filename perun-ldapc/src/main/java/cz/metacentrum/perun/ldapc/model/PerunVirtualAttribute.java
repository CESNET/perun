package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.PerunBean;

public interface PerunVirtualAttribute<T extends PerunBean> extends PerunAttribute<T> {

	public String getPerunAttributeName();
	
	public void afterPropertiesSet() throws Exception;
}
