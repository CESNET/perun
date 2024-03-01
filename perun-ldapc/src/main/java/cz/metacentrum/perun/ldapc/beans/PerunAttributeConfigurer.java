package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;

import java.util.List;

public interface PerunAttributeConfigurer<T extends PerunBean> {

  public List<PerunAttribute<T>> getAttributeDescriptions();

}
