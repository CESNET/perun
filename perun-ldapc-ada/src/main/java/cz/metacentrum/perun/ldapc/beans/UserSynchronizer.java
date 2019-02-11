package cz.metacentrum.perun.ldapc.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.ldapc.model.PerunUser;

@Component
public class UserSynchronizer extends AbstractSynchronizer {

	private final static Logger log = LoggerFactory.getLogger(UserSynchronizer.class);

	@Autowired
	private PerunUser perunUser;
	
	public void synchronizeUsers() {
		Perun perun = ldapcManager.getPerunBl();
		try {
			log.debug("Getting list of users");
			List<User> users = perun.getUsersManager().getUsers(ldapcManager.getPerunSession());

			for(User user: users) {

				log.debug("Getting list of attributes for user {}", user.getId());
				List<Attribute> attrs = new ArrayList<Attribute>(); 
				for(String attrName: fillPerunAttributeNames(perunUser.getPerunAttributeNames())) {
					try {
						//log.debug("Getting attribute {} for user {}", attrName, user.getId());
						Attribute attr = perun.getAttributesManager().getAttribute(ldapcManager.getPerunSession(), user, attrName);
						/* very chatty
						if(attr == null) {
							log.debug("Got null for attribute {}", attrName);
						} else if (attr.getValue() == null) {
							log.debug("Got attribute {} with null value", attrName);
						} else {
							log.debug("Got attribute {} with value {}", attrName, attr.getValue().toString());
						}
						*/
						attrs.add(attr);
					} catch (PerunException e) {
						log.warn("No attribute {} found for user {}: {}", attrName, user.getId(), e.getMessage());
					}
				}
				log.debug("Got attributes {}", attrs.toString());

				try {
					log.debug("Synchronizing user {} with {} attrs", user, attrs.size());
					perunUser.synchronizeEntry(user, attrs);


					log.debug("Getting list of member groups for user {}", user.getId());
					Set<Integer> voIds = new HashSet<>();
					List<Member> members = perun.getMembersManager().getMembersByUser(ldapcManager.getPerunSession(), user);
					List<Group> groups = new ArrayList<Group>();
					for(Member member: members) {
						if(member.getStatus().equals(Status.VALID)) {
							voIds.add(member.getVoId());
							groups.addAll(perun.getGroupsManager().getAllMemberGroups(ldapcManager.getPerunSession(), member));
						}
					}

					log.debug("Synchronizing user {} with {} VOs and {} groups", user.getId(), voIds.size(), groups.size());
					perunUser.synchronizeMembership(user, voIds, groups);
					
					log.debug("Getting list of extSources for user {}", user.getId());
					List<UserExtSource> userExtSources = perun.getUsersManager().getUserExtSources(ldapcManager.getPerunSession(), user);
					log.debug("Synchronizing user {} with {} extSources", user.getId(), userExtSources.size());
					perunUser.synchronizePrincipals(user, userExtSources);
					
				} catch (PerunException e) {
					log.error("Error synchronizing user", e);
				}
			}
			
		} catch (PerunException e) {
			log.error("Error synchronizing users", e);
		}
		
	}
}
