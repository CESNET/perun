package cz.metacentrum.perun.openapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

public class PerunException extends Exception {

    private String errorId;
    private String name;

    private PerunException(String message, Throwable cause, String name, String errorId) {
        super(message, cause);
        this.name = name;
        this.errorId = errorId;
    }

    /**
     * Converts JSON responseBody of HttpClientErrorException to PerunException.
     *
     * @param ex HttpClientErrorException containing
     * @return PerunException
     */
    public static PerunException to(HttpClientErrorException ex) {
    	try {
			cz.metacentrum.perun.openapi.model.PerunException pe = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.readValue(ex.getResponseBodyAsByteArray(), cz.metacentrum.perun.openapi.model.PerunException.class);
			return new PerunException(pe.getName() + ": " + pe.getMessage(), ex, pe.getName(), pe.getErrorId());
		} catch (IOException ioe) {
    		return new PerunException("cannot parse remote Exception", ex, "", "");
		}
    }

    /**
     * Gets the unique Perun id of the exception.
     *
     * @return id
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * Gets the name of the remote exception. E.g. " GroupNotExistsException".
     *
     * @return simple class name of remote exception
     */
    public String getName() {
        return name;
    }
}
