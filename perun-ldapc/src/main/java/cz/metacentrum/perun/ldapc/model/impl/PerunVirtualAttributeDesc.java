package cz.metacentrum.perun.ldapc.model.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunVirtualAttribute;
import cz.metacentrum.perun.ldapc.processor.VirtualAttributeManager;

public class PerunVirtualAttributeDesc<T extends PerunBean> extends PerunAttributeDesc<T> 
implements PerunVirtualAttribute<T>, InitializingBean {

	@Autowired
	protected VirtualAttributeManager<T> attributeManager;

	protected String dependsOnPerunAttribute;
	protected String perunAttributeName;
	
	@Override
	public String getPerunAttributeName() {
		return this.perunAttributeName;
	}

	public void setPerunAttributeName(String perunAttributeName) {
		this.perunAttributeName = perunAttributeName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		attributeManager.registerAttributeDependency(dependsOnPerunAttribute, this);
	}

	public VirtualAttributeManager<T> getAttributeManager() {
		return attributeManager;
	}

	public void setAttributeManager(VirtualAttributeManager<T> attributeManager) {
		this.attributeManager = attributeManager;
	}

	public String getDependsOnPerunAttribute() {
		return dependsOnPerunAttribute;
	}

	public void setDependsOnPerunAttribute(String dependsOnPerunAttribute) {
		this.dependsOnPerunAttribute = dependsOnPerunAttribute;
	}

	
}
