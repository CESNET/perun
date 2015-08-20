package cz.metacentrum.perun.cabinet.service.impl;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IHttpService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.http.HttpStatus;

/**
 * Simple HTTP client which executes HTTP requests and return HTTP responses.
 * Request is get from PSStrategy files and response returned back to them.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class HttpServiceImpl implements IHttpService {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public String execute(HttpMethod request, PublicationSystem ps) throws CabinetException {

		HttpClient httpClient = new HttpClient();
		if (ps == null || ps.getUsername() == null || ps.getPassword() == null) {
			// without authentication
		} else {
			// authentication
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(ps.getUsername(), ps.getPassword());
			httpClient.getState().setCredentials(new AuthScope(ps.getUrl(), 443), credentials); //cred, enc, proxy
		}
		try {
			log.debug("Attemping to execute HTTP request...");
			int responseCode = httpClient.executeMethod(request);
			
			if (responseCode != HttpStatus.SC_OK) {
				throw new CabinetException("Can't contact publication system. HTTP error code: " + responseCode, ErrorCodes.HTTP_IO_EXCEPTION);
			}
			log.debug("HTTP request executed.");
		} catch (IOException ioe) {
			log.error("Failed to execute HTTP request.");
			throw new CabinetException(ErrorCodes.HTTP_IO_EXCEPTION,ioe);
		}
		String response;
		try {
			response = request.getResponseBodyAsString();
		} catch (IOException ex) {
			throw new CabinetException("Can't contact publication system.", ErrorCodes.HTTP_IO_EXCEPTION);
		}
		return response;
	}

}
