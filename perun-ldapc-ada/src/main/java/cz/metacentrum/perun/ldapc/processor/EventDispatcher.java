package cz.metacentrum.perun.ldapc.processor;

import java.util.Collection;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Constant;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface EventDispatcher extends Runnable {

	public interface MessageBeans {
		public static final int GROUP_F		= 1 << 0;
		public static final int	MEMBER_F	= 1 << 1;
		public static final int	VO_F 		= 1 << 2;
		public static final int USER_F		= 1 << 3;
		public static final int ATTRIBUTE_F	= 1 << 4;
		public static final int ATTRIBUTEDEF_F	= 1 << 5;
		public static final int USEREXTSOURCE_F	= 1 << 6;
		public static final int RESOURCE_F	= 1 << 7;
		public static final int FACILITY_F	= 1 << 8;

		public int getPresentBeansMask();
		public Collection<Integer> getPresentBeansFlags();
		public int getBeansCount();
		
		public void addBean(PerunBean p) throws InternalErrorException;

		public Group getGroup();
		public Group getParentGroup();
		public Member getMember();
		public Vo getVo();
		public User getUser();
		public User getSpecificUser();
		public Attribute getAttribute();
		public AttributeDefinition getAttributeDef();
		public UserExtSource getUserExtSource();
		public Resource getResource();
		public Facility getFacility();
	}
	
	public interface DispatchEventCondition {
			
		public void setBeansConditionByMask(int presentBeansMask);
		public void setBeansConditionByClasses(Class... beanClasses) throws InternalErrorException;
		public void setBeansConditionByNames(String... names) throws InternalErrorException;
		public void setBeansCondition(List<String> names) throws InternalErrorException;
		public void setHandlerMethodName(String name);
		public String getHandlerMethodName();
		
		public boolean isApplicable(MessageBeans beans, String msg);
	}
	
	public void registerProcessor(EventProcessor processor, DispatchEventCondition condition);

	public void dispatchEvent(String msg, MessageBeans beans);
}
