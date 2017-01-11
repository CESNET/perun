package cz.metacentrum.perun.cabinet.bl.impl;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.HttpManagerBl;

/**
 * Simple HTTP client which executes HTTP requests and return HTTP responses.
 * Request is get from PSStrategy files and response returned back to them.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class HttpManagerBlImpl implements HttpManagerBl {

	private Logger log = LoggerFactory.getLogger(getClass());

	public HttpResponse execute(HttpUriRequest request) throws CabinetException {

		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 300000);
		HttpConnectionParams.setSoTimeout(httpParams, 300000);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpResponse response = null;
		try {
			log.debug("Attempting to execute HTTP request...");
			response = httpClient.execute(request);
			log.debug("HTTP request executed.");
		} catch (IOException ioe) {
			log.error("Failed to execute HTTP request.");
			throw new CabinetException(ErrorCodes.HTTP_IO_EXCEPTION,ioe);
		}
		return response;
	}

}
