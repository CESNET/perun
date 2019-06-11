package cz.metacentrum.perun.ldapc.beans;

import java.util.List;

import cz.metacentrum.perun.core.bl.PerunBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.ldapc.model.PerunVO;

@Component
public class VOSynchronizer extends AbstractSynchronizer {

	private final static Logger log = LoggerFactory.getLogger(VOSynchronizer.class);

	@Autowired
	protected PerunVO perunVO;

	public void synchronizeVOs() {
		PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
		try {
			log.debug("Getting list of VOs");
			// List<Vo> vos = Rpc.VosManager.getVos(ldapcManager.getRpcCaller());
			List<Vo> vos = perun.getVosManagerBl().getVos(ldapcManager.getPerunSession());
			for (Vo vo : vos) {
				// Map<String, Object> params = new HashMap<String, Object>();
				// params.put("vo", new Integer(vo.getId()));

				try {
					log.debug("Synchronizing VO entry {}", vo);
					perunVO.synchronizeEntry(vo);
					try {
						log.debug("Getting list of VO {} members", vo.getId());
						// List<Member> members = ldapcManager.getRpcCaller().call("membersManager", "getMembers", params).readList(Member.class);
						List<Member> members = perun.getMembersManagerBl().getMembers(ldapcManager.getPerunSession(), vo, Status.VALID);
						log.debug("Synchronizing {} members of VO {}", members.size(), vo.getId());
						perunVO.synchronizeMembers(vo, members);
					} catch (PerunException e) {
						log.error("Error synchronizing members for VO " + vo.getId(), e);
					}
				} catch (InternalErrorException e) {
					log.error("Error synchronizing VO", e);
				}
			}
		} catch (InternalErrorException e) {
			log.error("Error getting list of VOs", e);
		}
	}
}
