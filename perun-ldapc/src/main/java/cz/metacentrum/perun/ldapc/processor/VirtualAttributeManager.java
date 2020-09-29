package cz.metacentrum.perun.ldapc.processor;

import java.util.Collection;
import java.util.List;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunVirtualAttribute;

public interface VirtualAttributeManager<T extends PerunBean> {

	public void registerAttributeDependency(String parentName, PerunVirtualAttribute<T> dependentAttr);

	Collection<PerunVirtualAttribute<T>> getAttributeDependants(String name);
	
	Collection<PerunVirtualAttribute<T>> getAllAttributeDependants();

	List<String> getRegisteredAttributes();
	
}
