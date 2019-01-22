package cz.metacentrum.perun.ldapc.beans;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.ldapc.model.PerunResource;

@Component
public class ResourceSynchronizer extends AbstractSynchronizer {

	private final static Logger log = LoggerFactory.getLogger(ResourceSynchronizer.class);

	@Autowired
	protected PerunResource perunResource;
	
	public void synchronizeResources() {
		Perun perun = ldapcManager.getPerunBl();
		try {
			log.debug("Resource synchronization - getting list of VOs");
			// List<Vo> vos = Rpc.VosManager.getVos(ldapcManager.getRpcCaller());
			List<Vo> vos = perun.getVosManager().getVos(ldapcManager.getPerunSession());
			for (Vo vo : vos) {
				// Map<String, Object> params = new HashMap <String, Object>();
				// params.put("vo", new Integer(vo.getId()));
				
				try {
					log.debug("Getting list of resources for VO {}", vo);
					//List<Resource> resources = ldapcManager.getRpcCaller().call("resourceManager", "getResources", params).readList(Resource.class);
					List<Resource> resources = perun.getResourcesManager().getResources(ldapcManager.getPerunSession(), vo);

					for(Resource resource: resources) {
						
						try { 
							log.debug("Getting list of resources for resource {}", resource.getId());
							// Facility facility = Rpc.ResourcesManager.getFacility(ldapcManager.getRpcCaller(), resource);
							Facility facility = perun.getResourcesManager().getFacility(ldapcManager.getPerunSession(), resource);

							// params.clear();
							// params.put("facility",  new Integer(facility.getId()));

							log.debug("Getting list of attributes for resource {}", resource.getId());
							List<Attribute> attrs = new ArrayList<Attribute>(); 
							for(String attrName: fillPerunAttributeNames(perunResource.getPerunAttributeNames())) {
								try {
									//log.debug("Getting attribute {} for resource {}", attrName, resource.getId());
									attrs.add(perun.getAttributesManager().getAttribute(ldapcManager.getPerunSession(), facility, attrName));
								} catch (PerunException e) {
									log.warn("No attribute {} found for resource {}: {}", attrName, resource.getId(), e.getMessage());
								}
							}
							log.debug("Got attributes {}", attrs.toString());

							log.debug("Synchronizing resource {} with {} attrs", resource, attrs.size());
							perunResource.synchronizeEntry(resource, attrs);

							log.debug("Getting list of assigned group for resource {}", resource.getId());
							List<Group> assignedGroups = perun.getResourcesManager().getAssignedGroups(ldapcManager.getPerunSession(), resource);

							log.debug("Synchronizing {} groups for resource {}", assignedGroups.size(), resource.getId());
							perunResource.synchronizeGroups(resource, assignedGroups);
						} catch (PerunException e) {
							log.error("Error synchronizing resource", e);
						}
					}

					
				} catch (PerunException e) {
					log.error("Error synchronizing resources", e);
				}
			}
		} catch (InternalErrorException | PrivilegeException e) {
			log.error("Error getting VO list", e);
		}

	}
	
}
