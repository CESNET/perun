package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.DispatchEventCondition;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

public class SimpleDispatchEventCondition implements DispatchEventCondition {

	private int requiredBeans = 0;
	private String handlerMethodName = null;
	
	@Override
	public void setBeansConditionByMask(int presentBeansMask) {
		requiredBeans = presentBeansMask;
	}

	@Override
	public void setBeansConditionByClasses(Class... beanClasses) throws InternalErrorException {
		requiredBeans = 0;
		for (Class class1 : beanClasses) {
			addFlagForBeanName(class1.getName());
		}
	}

	@Override
	public void setBeansConditionByNames(String... names) throws InternalErrorException {
		requiredBeans = 0;
		for(String name: names) {
			addFlagForBeanName(name);
		}
	}

	@Override
	public void setBeansCondition(List<String> names) throws InternalErrorException {
		requiredBeans = 0;
		if(names != null && !names.isEmpty())
			for(String name: names) 
				addFlagForBeanName(name);
	}

	@Override
	public void setHandlerMethodName(String name) {
		this.handlerMethodName = name;
	}

	@Override
	public String getHandlerMethodName() {
		return handlerMethodName;
	}

	@Override
	public boolean isApplicable(MessageBeans beans, String msg) {
		int presentMask = beans.getPresentBeansMask();
		
		return (requiredBeans & presentMask) == requiredBeans; 
	}

	private void addFlagForBeanName(String name) throws InternalErrorException {
		switch (name) {
		case "cz.metacentrum.perun.core.api.Attribute":
			requiredBeans |= MessageBeans.ATTRIBUTE_F;
			break;

		case "cz.metacentrum.perun.core.api.AttributeDefinition":
			requiredBeans |= MessageBeans.ATTRIBUTEDEF_F;
			break;
			
		case "cz.metacentrum.perun.core.api.Facility":
			requiredBeans |= MessageBeans.FACILITY_F;
			break;
			
		case "cz.metacentrum.perun.core.api.Group":
			requiredBeans |= MessageBeans.GROUP_F;
			break;
			
		case "cz.metacentrum.perun.core.api.Member":
			requiredBeans |= MessageBeans.MEMBER_F;
			break;
			
		case "cz.metacentrum.perun.core.api.Resource":
			requiredBeans |= MessageBeans.RESOURCE_F;
			break;
			
		case "cz.metacentrum.perun.core.api.User":
			requiredBeans |= MessageBeans.USER_F;
			break;
			
		case "cz.metacentrum.perun.core.api.UserExtSource":
			requiredBeans |= MessageBeans.USER_F;
			break;
			
		case "cz.metacentrum.perun.core.api.Vo":
			requiredBeans |= MessageBeans.VO_F;
			break;

		default:
			throw new InternalErrorException("Class " + name + " is not supported PerunBean for condition");
		}

	}

}
