package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunResource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResourceSynchronizer extends AbstractSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceSynchronizer.class);

  @Autowired
  protected PerunResource perunResource;

  public void synchronizeResources() {
    PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
    boolean shouldWriteExceptionLog = true;
    try {
      LOG.debug("Resource synchronization - getting list of VOs");
      // List<Vo> vos = Rpc.VosManager.getVos(ldapcManager.getRpcCaller());
      List<Vo> vos = perun.getVosManagerBl().getVos(ldapcManager.getPerunSession());
      Set<Name> presentResources = new HashSet<Name>();

      for (Vo vo : vos) {
        // Map<String, Object> params = new HashMap <String, Object>();
        // params.put("vo", new Integer(vo.getId()));

        try {
          LOG.debug("Getting list of resources for VO {}", vo);
          //List<Resource> resources = ldapcManager.getRpcCaller().call("resourceManager", "getResources", params)
          // .readList(Resource.class);
          List<Resource> resources = perun.getResourcesManagerBl().getResources(ldapcManager.getPerunSession(), vo);

          for (Resource resource : resources) {

            presentResources.add(
                perunResource.getEntryDN(String.valueOf(vo.getId()), String.valueOf(resource.getId())));

            try {
              LOG.debug("Getting list of attributes for resource {}", resource.getId());
              List<Attribute> attrs = new ArrayList<Attribute>();
              /*
                             *  replaced with single call
                             *
                            for(String attrName: fillPerunAttributeNames(perunResource.getPerunAttributeNames())) {
                                try {
                                    //log.debug("Getting attribute {} for resource {}", attrName, resource.getId());
                                    attrs.add(perun.getAttributesManager().getAttribute(ldapcManager.getPerunSession()
                                    * , facility, attrName));
                                } catch (PerunException e) {
                                    log.warn("No attribute {} found for resource {}: {}", attrName, resource.getId(),
                                    * e.getMessage());
                                }
                            }
                            */
              List<String> attrNames = fillPerunAttributeNames(perunResource.getPerunAttributeNames());
              try {
                //log.debug("Getting attribute {} for resource {}", attrName, resource.getId());
                attrs.addAll(
                    perun.getAttributesManagerBl().getAttributes(ldapcManager.getPerunSession(), resource, attrNames));
              } catch (PerunRuntimeException e) {
                LOG.warn("No attributes {} found for resource {}: {}", attrNames, resource.getId(), e.getMessage());
                shouldWriteExceptionLog = false;
                throw new InternalErrorException(e);
              }
              LOG.debug("Got attributes {}", attrs.toString());

              LOG.debug("Synchronizing resource {} with {} attrs", resource, attrs.size());
              //perunResource.synchronizeEntry(resource, attrs);

              LOG.debug("Getting list of assigned group for resource {}", resource.getId());
              List<Group> assignedGroups =
                  perun.getResourcesManagerBl().getAssignedGroups(ldapcManager.getPerunSession(), resource);

              LOG.debug("Synchronizing {} groups for resource {}", assignedGroups.size(), resource.getId());
              //perunResource.synchronizeGroups(resource, assignedGroups);

              perunResource.synchronizeResource(resource, attrs, assignedGroups);

            } catch (PerunRuntimeException e) {
              if (shouldWriteExceptionLog) {
                LOG.error("Error synchronizing resource", e);
              }
              shouldWriteExceptionLog = false;
              throw new InternalErrorException(e);
            }
          }


        } catch (PerunRuntimeException e) {
          if (shouldWriteExceptionLog) {
            LOG.error("Error synchronizing resources", e);
          }
          shouldWriteExceptionLog = false;
          throw new InternalErrorException(e);
        }
      }

      try {
        removeOldEntries(perunResource, presentResources, LOG);
      } catch (InternalErrorException e) {
        LOG.error("Error removing old resource entries", e);
        shouldWriteExceptionLog = false;
        throw new InternalErrorException(e);
      }

    } catch (InternalErrorException e) {
      if (shouldWriteExceptionLog) {
        LOG.error("Error getting VO list", e);
      }
      throw new InternalErrorException(e);
    }

  }

}
