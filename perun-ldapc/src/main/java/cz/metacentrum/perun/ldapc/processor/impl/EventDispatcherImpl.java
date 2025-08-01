package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher;
import cz.metacentrum.perun.ldapc.processor.EventProcessor;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EventDispatcher runs continuously in a separate thread, polling the Perun BE for AuditMessages, parsing the affected
 * beans out of them and calling the adequate processor based on the present beans and message regex matching (defined
 * in `perun-ldapc.xml`).
 * Also loads/saves the id of the last processed message in a defined file for consistency.
 */
@org.springframework.stereotype.Service(value = "eventDispatcher")
public class EventDispatcherImpl implements EventDispatcher, Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(EventDispatcherImpl.class);

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  @Autowired
  private LdapProperties ldapProperties;
  @Autowired
  private LdapcManager ldapcManager;

  private int lastProcessedIdNumber;

  private boolean running = false;

  private List<Pair<DispatchEventCondition, EventProcessor>> registeredProcessors;

  @Override
  public void dispatchEvent(String msg, MessageBeans beans) {
    for (Pair<DispatchEventCondition, EventProcessor> subscription : registeredProcessors) {
      DispatchEventCondition condition = subscription.getLeft();
      EventProcessor processor = subscription.getRight();

      if (condition.isApplicable(beans, msg)) {
        String handlerName = condition.getHandlerMethodName();
        if (handlerName != null) {
          try {
            Method handler = processor.getClass().getMethod(handlerName, String.class, MessageBeans.class);
            if (handler != null) {
              LOG.debug("Dispatching message {} to method {}", msg, handler.toString());
              handler.invoke(processor, msg, beans);
            } else {
              LOG.debug("Handler not found, dispatching message {} to processor {}", msg,
                  processor.getClass().getName());
              processor.processEvent(msg, beans);
            }
          } catch (Exception e) {
            LOG.error("Error dispatching to handler " + handlerName + ": ", e);
          }

        } else {
          LOG.debug("Dispatching message {} to processor {}", msg, processor.getClass().getName());
          processor.processEvent(msg, beans);
        }
      }
    }
  }

  public int getLastProcessedIdNumber() {
    return lastProcessedIdNumber;
  }

  protected void loadLastProcessedId() {
    Path file = FileSystems.getDefault().getPath(ldapProperties.getLdapStateFile());
    try {
      List<String> idString = Files.readAllLines(file, Charset.defaultCharset());
      int lastId = idString.isEmpty() ? 0 : Integer.parseInt(idString.get(0));
      if (lastId >= 0) {
        this.lastProcessedIdNumber = lastId;
        LOG.info("Loaded last processed message id={}", lastId);
      } else {
        LOG.error("Wrong number for last processed message id {}, exiting.", idString);
        System.exit(-1);

      }
    } catch (IOException exception) {
      LOG.error("Error reading last processed message id from {}", ldapProperties.getLdapStateFile(), exception);
      System.exit(-1);
    }
  }

  @Override
  public void registerProcessor(EventProcessor processor, DispatchEventCondition condition) {
    if (registeredProcessors == null) {
      registeredProcessors = new ArrayList<Pair<DispatchEventCondition, EventProcessor>>(20);
    }
    registeredProcessors.add(new Pair<DispatchEventCondition, EventProcessor>(condition, processor));
  }

  protected MessageBeans resolveMessage(String msg, Integer idOfMessage) {

    List<PerunBean> listOfBeans;
    listOfBeans = AuditParser.parseLog(msg);

    //Debug information to check parsing of message.
    MessageBeans beans = new MessageBeansImpl();

    if (!listOfBeans.isEmpty()) {
      int i = 0;
      for (PerunBean p : listOfBeans) {
        i++;
        // if(p!=null)LOG.debug("There is object number " + i + ") " + p.serializeToString());
        // elseLOG.debug("There is unknown object which is null");
        beans.addBean(p);
      }
      //log.debug("Resolved{} beans ", beans.getBeansCount());
    }
    return beans;
  }

  @Override
  public void run() {

    if (!ldapProperties.propsLoaded()) {
      throw new RuntimeException("LdapcProperties is not autowired correctly!");
    }

    running = true;
    AuditMessage message = null;
    List<AuditMessage> messages;

    try {
      PerunSession perunSession = ldapcManager.getPerunSession();
      PerunBl perun = (PerunBl) ldapcManager.getPerunBl();

      if (lastProcessedIdNumber == 0) {
        loadLastProcessedId();
      }

      //If running is true, then this process will be continuously
      while (running) {

        messages = null;
        int sleepTime = 1000;
        //Waiting for new messages. If consumer failed in some internal case, waiting until it will be repaired
        // (waiting time is increases by each attempt)
        do {
          try {
            //IMPORTANT STEP1: Get new bulk of messages
            messages = perun.getAuditMessagesManagerBl()
                .pollConsumerMessages(perunSession, ldapProperties.getLdapConsumerName(), lastProcessedIdNumber);
          } catch (InternalErrorException ex) {
            LOG.error("Consumer failed due to {}. Sleeping for {} ms.", ex, sleepTime);
            Thread.sleep(sleepTime);
            sleepTime += sleepTime;
          }

          //If there are no messages, sleep for 1 sec and then try it again
          if (messages == null) {
            Thread.sleep(1000);
          }
        } while (messages == null);
        //If new messages exist, resolve them all
        Iterator<AuditMessage> messagesIterator = messages.iterator();
        while (messagesIterator.hasNext()) {
          message = messagesIterator.next();
          messagesIterator.remove();
          //Warning when two consecutive messages are separated by more than 15 ids
          if (lastProcessedIdNumber >= 0 && lastProcessedIdNumber < message.getId()) {
            if ((message.getId() - lastProcessedIdNumber) > 15) {
              LOG.debug("SKIP FLAG WARNING: lastProcessedIdNumber: " + lastProcessedIdNumber + " - newMessageNumber: " +
                        message.getId() + " = " + (lastProcessedIdNumber - message.getId()));
            }
          }
          lastProcessedIdNumber = message.getId();
          //IMPORTANT STEP2: Resolve next message
          MessageBeans presentBeans = this.resolveMessage(message.getEvent().getMessage(), message.getId());
          this.dispatchEvent(message.getEvent().getMessage(), presentBeans);
        }
        //After all messages has been resolved, test interrupting of thread and if its ok, wait and go for another
        // bulk of messages
        if (Thread.interrupted()) {
          running = false;
        } else {
          saveLastProcessedId();
          Thread.sleep(5000);
        }
      }
      //If ldapc is interrupted
    } catch (InterruptedException e) {
      Date date = new Date();
      LOG.error("Last message has ID='" + ((message != null) ? message.getId() : 0) + "' and was INTERRUPTED at " +
                DATE_FORMAT.format(date) + " due to interrupting.");
      running = false;
      Thread.currentThread().interrupt();
      //If some other exception is thrown
    } catch (Exception e) {
      Date date = new Date();
      LOG.error(
          "Last message has ID='" + ((message != null) ? message.getId() : 0) + "' and was bad PARSED or EXECUTE at " +
          DATE_FORMAT.format(date) + " due to exception " + e.toString());
      throw new RuntimeException(e);
    } finally {
      saveLastProcessedId();
    }
  }

  protected void saveLastProcessedId() {
    Path file = FileSystems.getDefault().getPath(ldapProperties.getLdapStateFile());
    try {
      Files.write(file, String.valueOf(lastProcessedIdNumber).getBytes());
    } catch (IOException e) {
      LOG.error("Error writing last processed message id={} to file {}", lastProcessedIdNumber,
              ldapProperties.getLdapStateFile(), e);
    }
  }

  public void setLastProcessedIdNumber(int lastProcessedIdNumber) {
    this.lastProcessedIdNumber = lastProcessedIdNumber;
  }

  /**
   * Stores the parsed out PerunBean objects, also creating a binary mask, which is used to check for the presence of
   * the bean flags to determine the EventProcessor to use.
   */
  private class MessageBeansImpl implements MessageBeans {

    int beanCount = 0;
    int presentBeans = 0;

    private Group group;
    private Group parentGroup;
    private Member member;
    private Vo vo;
    private User user;
    List<User> users = new ArrayList<>();
    private User specificUser;
    private Attribute attribute;
    private AttributeDefinition attributeDef;
    private UserExtSource userExtSource;
    private Resource resource;
    private Facility facility;

    @Override
    public void addBean(PerunBean perunBean) {
      if (perunBean == null) {
        LOG.warn("Not adding unknown (null) bean");
        return;
      }
      if (perunBean instanceof Group) {
        if (this.group == null) {
          this.group = (Group) perunBean;
        } else {
          this.parentGroup = (Group) perunBean;
        }
        presentBeans |= GROUP_F;
      } else if (perunBean instanceof Member) {
        if (this.member == null) {
          this.member = (Member) perunBean;
        } else {
          throw new InternalErrorException("More than one member come to method parseMessages!");
        }
        presentBeans |= MEMBER_F;
      } else if (perunBean instanceof Vo) {
        if (this.vo == null) {
          this.vo = (Vo) perunBean;
        } else {
          throw new InternalErrorException("More than one vo come to method parserMessages!");
        }
        presentBeans |= VO_F;
      } else if (perunBean instanceof User) {
        User u = (User) perunBean;
        users.add(u);
        if (u.isServiceUser() || u.isSponsoredUser()) {
          if (this.specificUser == null) {
            this.specificUser = u;
          }
        } else {
          if (this.user == null) {
            this.user = u;
          }
        }
        // only update flag for the first user to maintain functionality
        if (users.size() == 1) {
          presentBeans |= USER_F;
        }
      } else if (perunBean instanceof AttributeDefinition &&
                 perunBean instanceof cz.metacentrum.perun.core.api.Attribute) {
        if (this.attribute == null) {
          this.attribute = (cz.metacentrum.perun.core.api.Attribute) perunBean;
        } else {
          throw new InternalErrorException("More than one attribute come to method parseMessages!");
        }
        presentBeans |= ATTRIBUTE_F;
      } else if (perunBean instanceof AttributeDefinition) {
        if (this.attributeDef == null) {
          this.attributeDef = (AttributeDefinition) perunBean;
        } else {
          throw new InternalErrorException("More than one attribute come to method parseMessages!");
        }
        presentBeans |= ATTRIBUTEDEF_F;
      } else if (perunBean instanceof UserExtSource) {
        if (this.userExtSource == null) {
          this.userExtSource = (UserExtSource) perunBean;
        } else {
          throw new InternalErrorException("More than one userExtSource come to method parseMessages!");
        }
        presentBeans |= USEREXTSOURCE_F;
      } else if (perunBean instanceof Resource) {
        if (this.resource == null) {
          this.resource = (Resource) perunBean;
        } else {
          throw new InternalErrorException("More than one Resource come to method parseMessages!");
        }
        presentBeans |= RESOURCE_F;
      } else if (perunBean instanceof Facility) {
        if (this.facility == null) {
          this.facility = (Facility) perunBean;
        } else {
          throw new InternalErrorException("More than one Facility come to method parseMessages!");
        }
        presentBeans |= FACILITY_F;
      }
      beanCount++;
    }

    @Override
    public Attribute getAttribute() {
      return attribute;
    }

    @Override
    public AttributeDefinition getAttributeDef() {
      return attributeDef;
    }

    @Override
    public int getBeansCount() {
      return beanCount;
    }

    @Override
    public Facility getFacility() {
      return facility;
    }

    @Override
    public Group getGroup() {
      return group;
    }

    @Override
    public Member getMember() {
      return member;
    }

    @Override
    public Group getParentGroup() {
      return parentGroup;
    }

    @Override
    public Collection<Integer> getPresentBeansFlags() {
      Collection<Integer> result = new ArrayList<Integer>(10);
      int remain = presentBeans;
      for (int mask = 1; remain > 0; mask = mask << 1, remain = remain >> 1) {
        if ((remain & 1) == 1) {
          result.add(mask);
        }
      }
      return result;
    }

    @Override
    public int getPresentBeansMask() {
      return presentBeans;
    }

    @Override
    public Resource getResource() {
      return resource;
    }

    @Override
    public User getSpecificUser() {
      return specificUser;
    }

    @Override
    public User getUser() {
      return (user == null) ? specificUser : user;
    }

    @Override
    public List<User> getUsers() {
      return users;
    }

    @Override
    public UserExtSource getUserExtSource() {
      return userExtSource;
    }

    @Override
    public Vo getVo() {
      return vo;
    }

  }
}
