package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Checked version of VOOTException.
 * 
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTException extends PerunException {
    static final long serialVersionUID = 0;
    
    String error;
    String error_description;
    
    public VOOTException(String message){
        super(message);
        error = message;
    }
    
    public VOOTException(String error, String errorDescription){
        super(error);
        this.error = error;
        this.error_description = errorDescription;
    }
    
    public VOOTException(Throwable cause){
        super(cause);
    }
    
    public VOOTException(String message, Throwable cause){
        super(message, cause);
    }
    
    public String getError() {
        return error;
    }

    public String getError_description() {
        return error_description;
    }
    
    @Override
    public String toString(){
        if(error_description != null){
            return "error: " + error + ",error_description: " + error_description;
        }else{
            return "error: " + error;
        }
    }
}