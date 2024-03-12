package cz.metacentrum.perun.ldapc.model.impl;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunVirtualAttribute;
import cz.metacentrum.perun.ldapc.processor.VirtualAttributeManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class PerunVirtualAttributeDesc<T extends PerunBean> extends PerunAttributeDesc<T>
    implements PerunVirtualAttribute<T>, InitializingBean {

  @Autowired
  protected VirtualAttributeManager<T> attributeManager;

  protected String dependsOnPerunAttribute;
  protected String perunAttributeName;

  @Override
  public void afterPropertiesSet() throws Exception {
    attributeManager.registerAttributeDependency(dependsOnPerunAttribute, this);
  }

  public VirtualAttributeManager<T> getAttributeManager() {
    return attributeManager;
  }

  public String getDependsOnPerunAttribute() {
    return dependsOnPerunAttribute;
  }

  @Override
  public String getPerunAttributeName() {
    return this.perunAttributeName;
  }

  public void setAttributeManager(VirtualAttributeManager<T> attributeManager) {
    this.attributeManager = attributeManager;
  }

  public void setDependsOnPerunAttribute(String dependsOnPerunAttribute) {
    this.dependsOnPerunAttribute = dependsOnPerunAttribute;
  }

  public void setPerunAttributeName(String perunAttributeName) {
    this.perunAttributeName = perunAttributeName;
  }


}
