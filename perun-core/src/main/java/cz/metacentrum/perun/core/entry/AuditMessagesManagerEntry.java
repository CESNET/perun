package cz.metacentrum.perun.core.entry;

import java.util.List;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongRangeOfCountException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;




/**
 * AuditMessagesManager manages audit messages (logs). Entry Logic
 *
 * @author Michal Stava
 * @version $Id$
 */
public class AuditMessagesManagerEntry implements AuditMessagesManager {
    
  private AuditMessagesManagerBl auditMessagesManagerBl;
  private PerunBl perunBl;

  public AuditMessagesManagerEntry() {
  }
  
  public List<AuditMessage> getMessages(PerunSession perunSession) throws InternalErrorException, WrongRangeOfCountException {
      return this.getMessages(perunSession, AuditMessagesManager.COUNTOFMESSAGES);
  }

  public List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException, WrongRangeOfCountException {
   if(count<1) throw new WrongRangeOfCountException("Count of messages is less than 1. Can't be returned less than 1 message.");   
   return getAuditMessagesManagerBl().getMessages(perunSession, count);
  }
   
  public void log(PerunSession sess, String message) throws InternalErrorException, PrivilegeException {
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
      throw new PrivilegeException(sess, "log");
    }
    
    getAuditMessagesManagerBl().log(sess, message);
  }
  
  /**
   * Gets the AuditMessagesManagerBl for this instance.
   *
   * @return The AuditMessagesManagerBl.
   */
  public AuditMessagesManagerBl getAuditMessagesManagerBl() {
    return this.auditMessagesManagerBl;
  }

  /**
   * Sets the AuditMessagesManagerBl for this instance.
   *
   * @param AuditMessagesManagerBl The AuditMessagesManagerBl.
   */
  public void setAuditMessagesManagerBl(AuditMessagesManagerBl auditMessagesManagerBl)
  {
        this.auditMessagesManagerBl = auditMessagesManagerBl;
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }
  
  /**
   * Sets the perunBl for this instance.
   *
   * @param perunBl The perunBl.
   */
  public void setPerunBl(PerunBl perunBl)
  {
        this.perunBl = perunBl;
  }
}
