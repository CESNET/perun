package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RTMessage;
import cz.metacentrum.perun.core.api.RTMessagesManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.RTMessagesManagerBl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * RTMessage manager can create a new message and send it to RT like predefined service user.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class RTMessagesManagerEntry implements RTMessagesManager{

	private PerunBl perunBl;
	private RTMessagesManagerBl rtMessagesManagerBl;

	public RTMessagesManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.rtMessagesManagerBl = perunBl.getRTMessagesManagerBl();
	}

	public RTMessagesManagerEntry() {}

	@Override
	@Deprecated
	public RTMessage sendMessageToRT(PerunSession sess, Member member, String queue, String subject, String text) throws MemberNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getMembersManagerBl().checkMemberExists(sess, member);

		AuthzResolver.refreshAuthz(sess); //FIXME this is used for authz inicialization. maybe use something better for it.

		return rtMessagesManagerBl.sendMessageToRT(sess, member, queue, subject, text);
	}

	@Override
	public RTMessage sendMessageToRT(PerunSession sess, int voId, String queue, String subject, String text) {
		Utils.checkPerunSession(sess);

		AuthzResolver.refreshAuthz(sess); //FIXME this is used for authz inicialization. maybe use something better for it.

		return rtMessagesManagerBl.sendMessageToRT(sess, voId, queue, subject, text);
	}

	@Override
	public RTMessage sendMessageToRT(PerunSession sess, String queue, String subject, String text) {
		Utils.checkPerunSession(sess);

		AuthzResolver.refreshAuthz(sess); //FIXME this is used for authz inicialization. maybe use something better for it.

		return rtMessagesManagerBl.sendMessageToRT(sess, queue, subject, text);
	}

	@Override
	public RTMessage sendMessageToRT(PerunSession sess, int voId, String subject, String text) {
		Utils.checkPerunSession(sess);

		AuthzResolver.refreshAuthz(sess); //FIXME this is used for authz inicialization. maybe use something better for it.

		return rtMessagesManagerBl.sendMessageToRT(sess, voId, subject, text);
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public RTMessagesManagerBl getRTMessagesManagerBl() {
		return this.rtMessagesManagerBl;
	}

	public void setRTMessagesManagerBl(RTMessagesManagerBl rtMessagesManagerBl) {
		this.rtMessagesManagerBl = rtMessagesManagerBl;
	}
}
