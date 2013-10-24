package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * AttributeExistsRuntimeException
 * 
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @version $Id$
 */
@SuppressWarnings("serial")
public class AttributeExistsRuntimeException extends EntityExistsRuntimeException {

    public AttributeExistsRuntimeException() {
        super();
    }

    public AttributeExistsRuntimeException(Throwable cause) {
        super(cause);
    }

}
