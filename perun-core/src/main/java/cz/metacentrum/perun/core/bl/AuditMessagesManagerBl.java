package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

import java.util.List;

/**
 * UsersManager manages users.
 *
 * @author Michal Stava
 */
public interface AuditMessagesManagerBl {
    
     /**
    * Returns countOfMessages messages from audit's logs.
    *
    * @param perunSession 
    * @param count Count of returned messages.
    * @return list of audit's messages
    * @throws InternalErrorException
    */
    List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException;
    
    /**
     * Log auditer message
     * 
     * @param sess
     * @param message
     * @throws InternalErrorException
     * @throws PrivilegeException
     */
    void log(PerunSession sess, String message) throws InternalErrorException;
}
