package cz.metacentrum.perun.ldapc.processor;

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
import java.util.Collection;
import java.util.List;

public interface EventDispatcher extends Runnable {

  public void dispatchEvent(String msg, MessageBeans beans);

  public void registerProcessor(EventProcessor processor, DispatchEventCondition condition);

  public void setLastProcessedIdNumber(int lastProcessedId);

  public interface MessageBeans {
    public static final int GROUP_F = 1 << 0;
    public static final int MEMBER_F = 1 << 1;
    public static final int VO_F = 1 << 2;
    public static final int USER_F = 1 << 3;
    public static final int ATTRIBUTE_F = 1 << 4;
    public static final int ATTRIBUTEDEF_F = 1 << 5;
    public static final int USEREXTSOURCE_F = 1 << 6;
    public static final int RESOURCE_F = 1 << 7;
    public static final int FACILITY_F = 1 << 8;

    public void addBean(PerunBean p);

    public Attribute getAttribute();

    public AttributeDefinition getAttributeDef();

    public int getBeansCount();

    public Facility getFacility();

    public Group getGroup();

    public Member getMember();

    public Group getParentGroup();

    public Collection<Integer> getPresentBeansFlags();

    public int getPresentBeansMask();

    public Resource getResource();

    public User getSpecificUser();

    public User getUser();

    public UserExtSource getUserExtSource();

    public Vo getVo();
  }

  public interface DispatchEventCondition {

    public String getHandlerMethodName();

    public boolean isApplicable(MessageBeans beans, String msg);

    public void setBeansCondition(List<String> names);

    public void setBeansConditionByClasses(Class... beanClasses);

    public void setBeansConditionByMask(int presentBeansMask);

    public void setBeansConditionByNames(String... names);

    public void setHandlerMethodName(String name);
  }
}
