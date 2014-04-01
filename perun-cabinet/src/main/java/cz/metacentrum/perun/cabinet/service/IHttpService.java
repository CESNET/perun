package cz.metacentrum.perun.cabinet.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

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
	HttpResponse execute(HttpUriRequest request) throws CabinetException;

}
