package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunVO;
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
public class VOSynchronizer extends AbstractSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(VOSynchronizer.class);

  @Autowired
  protected PerunVO perunVO;

  public void synchronizeVOs() {
    PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
    boolean shouldWriteExceptionLog = true;
    try {
      LOG.debug("Getting list of VOs");
      // List<Vo> vos = Rpc.VosManager.getVos(ldapcManager.getRpcCaller());
      List<Vo> vos = perun.getVosManagerBl().getVos(ldapcManager.getPerunSession());
      Set<Name> presentVos = new HashSet<Name>(vos.size());

      for (Vo vo : vos) {
        // Map<String, Object> params = new HashMap<String, Object>();
        // params.put("vo", new Integer(vo.getId()));

        presentVos.add(perunVO.getEntryDN(String.valueOf(vo.getId())));
        LOG.debug("Synchronizing VO entry {}", vo);

        LOG.debug("Getting list of attributes for vo {}", vo.getId());
        List<Attribute> attrs = new ArrayList<Attribute>();
        List<String> attrNames = fillPerunAttributeNames(perunVO.getPerunAttributeNames());
        try {
          attrs.addAll(perun.getAttributesManagerBl().getAttributes(ldapcManager.getPerunSession(), vo, attrNames));
        } catch (PerunRuntimeException e) {
          LOG.warn("Couldn't get attributes {} for vo {}: {}", attrNames, vo.getId(), e.getMessage());
          shouldWriteExceptionLog = false;
          throw new InternalErrorException(e);
        }
        LOG.debug("Got attributes {}", attrNames.toString());

        try {

          LOG.debug("Getting list of VO {} members", vo.getId());
          // List<Member> members = ldapcManager.getRpcCaller().call("membersManager", "getMembers", params).readList
          // (Member.class);
          List<Member> members = perun.getMembersManager().getMembers(ldapcManager.getPerunSession(), vo, Status.VALID);
          LOG.debug("Synchronizing {} members of VO {}", members.size(), vo.getId());
          perunVO.synchronizeVo(vo, attrs, members);
        } catch (PerunException e) {
          LOG.error("Error synchronizing VO " + vo.getId(), e);
          shouldWriteExceptionLog = false;
          throw new InternalErrorException(e);
        }
      }

      // search VO entries in LDAP and remove the ones not present in Perun
      try {
        removeOldEntries(perunVO, presentVos, LOG);
      } catch (InternalErrorException e) {
        LOG.error("Error removing old VO entries", e);
        shouldWriteExceptionLog = false;
        throw new InternalErrorException(e);
      }

    } catch (InternalErrorException e) {
      if (shouldWriteExceptionLog) {
        LOG.error("Error getting list of VOs", e);
      }
      throw new InternalErrorException(e);
    }
  }
}
