package cz.metacentrum.perun.ldapc.processor;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunVirtualAttribute;
import java.util.Collection;
import java.util.List;

public interface VirtualAttributeManager<T extends PerunBean> {

  Collection<PerunVirtualAttribute<T>> getAllAttributeDependants();

  Collection<PerunVirtualAttribute<T>> getAttributeDependants(String name);

  List<String> getRegisteredAttributes();

  public void registerAttributeDependency(String parentName, PerunVirtualAttribute<T> dependentAttr);

}
