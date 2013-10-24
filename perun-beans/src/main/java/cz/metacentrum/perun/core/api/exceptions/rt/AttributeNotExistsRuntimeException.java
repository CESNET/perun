package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * AttributeNotExistsRuntimeException
 * 
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @version $Id$
 */
@SuppressWarnings("serial")
public class AttributeNotExistsRuntimeException extends EntityNotExistsRuntimeException {

    public AttributeNotExistsRuntimeException() {
        super();
    }

    public AttributeNotExistsRuntimeException(Throwable cause) {
        super(cause);
    }

}
