package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.naming.Name;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GroupSynchronizer extends AbstractSynchronizer {

	private final static Logger log = LoggerFactory.getLogger(GroupSynchronizer.class);

	@Autowired
	protected PerunGroup perunGroup;

	public void synchronizeGroups() throws InternalErrorException {
		PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
		boolean shouldWriteExceptionLog = true;
		try {

			log.debug("Group synchronization - getting list of VOs");
			// List<Vo> vos = Rpc.VosManager.getVos(ldapcManager.getRpcCaller());
			List<Vo> vos = perun.getVosManagerBl().getVos(ldapcManager.getPerunSession());
			Set<Name> presentGroups = new HashSet<Name>();

			for(Vo vo : vos) {
				// Map<String, Object> params = new HashMap<String, Object>();
				// params.put("vo", new Integer(vo.getId()));

				try {
					log.debug("Getting list of groups for VO {}", vo);

					// List<Group> groups = ldapcManager.getRpcCaller().call("groupsManager",  "getAllGroups", params).readList(Group.class);
					List<Group> groups = perun.getGroupsManagerBl().getAllGroups(ldapcManager.getPerunSession(), vo);

					for(Group group : groups) {

						presentGroups.add(perunGroup.getEntryDN(
							String.valueOf(vo.getId()),
							String.valueOf(group.getId())));

						try {
							log.debug("Synchronizing group {}", group);
							//perunGroup.synchronizeEntry(group);

							// params.clear();
							// params.put("group", new Integer(group.getId()));

							log.debug("Getting list of members for group {}", group.getId());
							// List<Member> members = ldapcManager.getRpcCaller().call("groupsManager",  "getGroupMembers", params).readList(Member.class);
							List<Member> members = perun.getGroupsManagerBl().getActiveGroupMembers(ldapcManager.getPerunSession(), group, Status.VALID);
							log.debug("Synchronizing {} members of group {}", members.size(), group.getId());
							//perunGroup.synchronizeMembers(group, members);

							log.debug("Getting list of resources assigned to group {}", group.getId());
							// List<Resource> resources = Rpc.ResourcesManager.getAssignedResources(ldapcManager.getRpcCaller(), group);
							List<Resource> resources = perun.getResourcesManagerBl().getAssignedResources(ldapcManager.getPerunSession(), group);
							log.debug("Synchronizing {} resources assigned to group {}", resources.size(), group.getId());
							//perunGroup.synchronizeResources(group, resources);

							perunGroup.synchronizeGroup(group, members, resources);

						} catch (PerunException e) {
							log.error("Error synchronizing group", e);
							shouldWriteExceptionLog = false;
							throw new InternalErrorException(e);
						}
					}



				} catch (PerunException e) {
					if (shouldWriteExceptionLog) {
						log.error("Error synchronizing groups", e);
					}
					shouldWriteExceptionLog = false;
					throw new InternalErrorException(e);
				}
			}

			try {
				removeOldEntries(perunGroup, presentGroups, log);
			} catch (InternalErrorException e) {
				log.error("Error removing old group entries", e);
				shouldWriteExceptionLog = false;
				throw new InternalErrorException(e);
			}

		} catch (InternalErrorException e) {
			if (shouldWriteExceptionLog) {
				log.error("Error reading list of VOs", e);
			}
			throw new InternalErrorException(e);
		}

	}

}