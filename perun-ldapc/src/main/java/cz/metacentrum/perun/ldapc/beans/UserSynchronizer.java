package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class UserSynchronizer extends AbstractSynchronizer implements ApplicationContextAware {

  private static final Logger LOG = LoggerFactory.getLogger(UserSynchronizer.class);
  private static AtomicInteger taskCount;
  private static boolean wasThreadException = false;
  private ApplicationContext context;
  private PerunUser[] perunUser = new PerunUser[5];

  @Override
  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
  }

  public void synchronizeUsers() {

    PerunBl perun = (PerunBl) ldapcManager.getPerunBl();

    ThreadPoolTaskExecutor syncExecutor = new ThreadPoolTaskExecutor();
    int poolIndex;
    boolean shouldWriteExceptionLog = true;

    for (poolIndex = 0; poolIndex < perunUser.length; poolIndex++) {
      perunUser[poolIndex] = context.getBean("perunUser", PerunUser.class);
    }

    try {

      LOG.debug("Getting list of users");
      List<User> users = perun.getUsersManagerBl().getUsers(ldapcManager.getPerunSession());
      users.sort(Comparator.comparingInt(User::getId));
      Set<Name> presentUsers = new HashSet<Name>(users.size());

      syncExecutor.setCorePoolSize(5);
      syncExecutor.setMaxPoolSize(8);
      //syncExecutor.setQueueCapacity(30);
      syncExecutor.initialize();

      poolIndex = 0;
      taskCount = new AtomicInteger(0);

      for (User user : users) {

        presentUsers.add(perunUser[0].getEntryDN(String.valueOf(user.getId())));

        LOG.debug("Getting list of attributes for user {}", user.getId());
        List<Attribute> attrs = new ArrayList<Attribute>();
        List<String> attrNames = fillPerunAttributeNames(perunUser[poolIndex].getPerunAttributeNames());
        try {
          //log.debug("Getting attribute {} for user {}", attrName, user.getId());
          attrs.addAll(perun.getAttributesManagerBl().getAttributes(ldapcManager.getPerunSession(), user, attrNames));
          /* very chatty
                        if(attr == null) {
                            log.debug("Got null for attribute {}", attrName);
                        } else if (attr.getValue() == null) {
                            log.debug("Got attribute {} with null value", attrName);
                        } else {
                            log.debug("Got attribute {} with value {}", attrName, attr.getValue().toString());
                        }
                        */
        } catch (PerunRuntimeException e) {
          LOG.warn("Couldn't get attributes {} for user {}: {}", attrNames, user.getId(), e.getMessage());
          shouldWriteExceptionLog = false;
          throw new InternalErrorException(e);
        }
        LOG.debug("Got attributes {}", attrNames.toString());

        try {
          //log.debug("Synchronizing user {} with {} attrs", user, attrs.size());
          //perunUser.synchronizeEntry(user, attrs);

          LOG.debug("Getting list of member groups for user {}", user.getId());
          Set<Integer> voIds = new HashSet<>();
          List<Member> members = perun.getMembersManagerBl().getMembersByUser(ldapcManager.getPerunSession(), user);
          List<Group> groups = new ArrayList<Group>();
          for (Member member : members) {
            if (member.getStatus().equals(Status.VALID)) {
              voIds.add(member.getVoId());
              groups.addAll(
                  perun.getGroupsManagerBl().getAllGroupsWhereMemberIsActive(ldapcManager.getPerunSession(), member));
            }
          }

          //log.debug("Synchronizing user {} with {} VOs and {} groups", user.getId(), voIds.size(), groups.size());
          //perunUser.synchronizeMembership(user, voIds, groups);

          LOG.debug("Getting list of extSources for user {}", user.getId());
          List<UserExtSource> userExtSources =
              perun.getUsersManagerBl().getUserExtSources(ldapcManager.getPerunSession(), user);

          List<Group> adminGroups =
              perun.getUsersManagerBl().getGroupsWhereUserIsAdmin(ldapcManager.getPerunSession(), user);
          List<Vo> adminVos = perun.getUsersManagerBl().getVosWhereUserIsAdmin(ldapcManager.getPerunSession(), user);
          List<Facility> adminFacilities =
              perun.getFacilitiesManagerBl().getFacilitiesWhereUserIsAdmin(ldapcManager.getPerunSession(), user);

          //log.debug("Synchronizing user {} with {} extSources", user.getId(), userExtSources.size());
          //perunUser.synchronizePrincipals(user, userExtSources);

          syncExecutor.execute(
              new SyncUsersWorker(poolIndex, user, attrs, voIds, groups, userExtSources, adminGroups, adminVos,
                  adminFacilities));
          taskCount.incrementAndGet();

        } catch (PerunRuntimeException e) {
          LOG.error("Error synchronizing user", e);
          shouldWriteExceptionLog = false;
          throw new InternalErrorException(e);
        }

        poolIndex = (poolIndex + 1) % perunUser.length;
      }

      try {
        removeOldEntries(perunUser[0], presentUsers, LOG);
      } catch (InternalErrorException e) {
        LOG.error("Error removing old user entries", e);
        shouldWriteExceptionLog = false;
        throw new InternalErrorException(e);
      }

    } catch (PerunRuntimeException e) {
      if (shouldWriteExceptionLog) {
        LOG.error("Error synchronizing users", e);
      }
      throw new InternalErrorException(e);

    } finally {
      // wait for all the tasks to get executed
      while (!syncExecutor.getThreadPoolExecutor().getQueue().isEmpty()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          break;
        }
      }
      // wait for all the tasks to complete (for at most 10 seconds)
      for (int i = 0; i < 10 && taskCount.get() > 0; i++) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          break;
        }
      }
      syncExecutor.shutdown();
      for (poolIndex = 0; poolIndex < perunUser.length; poolIndex++) {
        perunUser[poolIndex] = null;
      }
    }
    if (wasThreadException) {
      throw new InternalErrorException("Error synchronizing user in executed thread");
    }

  }

  private class SyncUsersWorker implements Runnable {

    public int poolIndex;
    public User user;
    public List<Attribute> attrs;
    public Set<Integer> voIds;
    public List<Group> groups;
    List<UserExtSource> userExtSources;
    List<Group> adminGroups;
    List<Vo> adminVos;
    List<Facility> adminFacilities;

    public SyncUsersWorker(int poolIndex, User user, List<Attribute> attrs, Set<Integer> voIds, List<Group> groups,
                           List<UserExtSource> userExtSources, List<Group> adminGroups, List<Vo> adminVos,
                           List<Facility> adminFacilities) {
      this.poolIndex = poolIndex;
      this.user = user;
      this.attrs = attrs;
      this.voIds = voIds;
      this.groups = groups;
      this.userExtSources = userExtSources;
      this.adminGroups = adminGroups;
      this.adminVos = adminVos;
      this.adminFacilities = adminFacilities;
    }

    public void run() {
      try {
        LOG.debug("Synchronizing user {} with {} attrs", user, attrs.size());
        //perunUser[poolIndex].synchronizeEntry(user, attrs);
        LOG.debug("Synchronizing user {} with {} VOs and {} groups", user.getId(), voIds.size(), groups.size());
        //perunUser[poolIndex].synchronizeMembership(user, voIds, groups);
        LOG.debug("Synchronizing user {} with {} extSources", user.getId(), userExtSources.size());
        //perunUser[poolIndex].synchronizePrincipals(user, userExtSources);
        LOG.debug("Synchronizing user {} as admin of {} groups, {} VOs and {} facilities", user.getId(),
            adminGroups.size(), adminVos.size(), adminFacilities.size());
        perunUser[poolIndex].synchronizeUser(user, attrs, voIds, groups, userExtSources, adminGroups, adminVos,
            adminFacilities);
      } catch (PerunRuntimeException e) {
        LOG.error("Error synchronizing user", e);
        UserSynchronizer.wasThreadException = true;

      } catch (Exception e) {
        LOG.error("Error synchronizing user", e);
        UserSynchronizer.wasThreadException = true;

      } finally {
        taskCount.decrementAndGet();
      }
    }

  }

}
