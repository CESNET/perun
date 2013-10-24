package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checked version of ExtendMembershipException.
 *
 * @author Michal Prochazka
 * @version $Id$
 */
public class ExtendMembershipException extends PerunException {
    static final long serialVersionUID = 0;
    private final static Logger log = LoggerFactory.getLogger(ExtendMembershipException.class);
    private Reason reason;
    
    public ExtendMembershipException(Reason reason, String message) {
        super(message);
        
        this.reason = reason;
        
        log.error("Internal Error Exception:", this);
    }

    public ExtendMembershipException(String message, Throwable cause) {
        super(message, cause);
        
        log.error("Internal Error Exception:", this);
    }

    public ExtendMembershipException(Throwable cause) {
        super(cause);
        
        log.error("Internal Error Exception:", this);
    }
    
    public Reason getReason() {
      return this.reason;
    }
    
    public enum Reason {
      NOUSERLOA, // User do not have LoA defined, byt VO has rules for membership expiration per LoA
      INSUFFICIENTLOA, // User has LoA which is not allowed in the VO
      INSUFFICIENTLOAFOREXTENSION, // User cannot extend membership because has insufficient LoA
      OUTSIDEEXTENSIONPERIOD, // We are not in grace period, so do not allow extension
    }
}
