package cz.metacentrum.perun.cabinet.service;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Interface for executing HTTP request to external PS to retrieve users publications
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public interface IHttpService {

	/**
	 * Executes a HTTP request (i.e. HttpGet or HttpPost) and returns obtained httpResponse.
	 *
	 * @param request HTTP request which we get from PSStrategy files (required)
	 * @return HTTP response we pass back to PSStrategy files to be parsed as publications
	 * @throws CabinetException
	 */
	String execute(HttpMethod request, PublicationSystem ps) throws CabinetException;

}
