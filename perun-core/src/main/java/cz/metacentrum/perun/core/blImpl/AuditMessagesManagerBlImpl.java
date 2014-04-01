package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.AttributesManagerImpl;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AuditMessagesManager manages audit messages (logs). Implementation of
 * Business logic.
 *
 * @author Michal Stava
 */
public class AuditMessagesManagerBlImpl implements AuditMessagesManagerBl {

    private final static Logger log = LoggerFactory.getLogger(UsersManagerBlImpl.class);
    private Auditer auditer;
    private PerunBl perunBl;

    public void setAuditer(Auditer auditer) {
        this.auditer = auditer;
    }

    public Auditer getAuditer() {
        return auditer;
    }

    public PerunBl getPerunBl() {
        return perunBl;
    }

    public void setPerunBl(PerunBl perunBl) {
        this.perunBl = perunBl;
    }

    public List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException {

        return perunBl.getAuditer().getMessages(count);
    }

    public void log(PerunSession sess, String message) throws InternalErrorException {
        perunBl.getAuditer().log(sess, message);
    }
}
