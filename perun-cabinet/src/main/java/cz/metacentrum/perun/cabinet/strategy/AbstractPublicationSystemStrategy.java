package cz.metacentrum.perun.cabinet.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public abstract class AbstractPublicationSystemStrategy {

	private Logger log = LoggerFactory.getLogger(getClass());
	protected PublicationSystem configuration;

	public AbstractPublicationSystemStrategy(PublicationSystem config) {
		this.configuration = config;
	}

	public List<Publication> findPublications(int year1, int year2, int authorId) throws CabinetException {

		HttpMethod request = null;
		try {
			log.debug("Getting http request.");
			request = getFindPublicationsRequest(year1, year2, authorId);
			if (request == null)
				throw new NullPointerException("http request cannot be null");
			log.debug("Got http request.");
			HttpClient httpClient = new HttpClient();
			// authentication
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword());
			httpClient.getState().setCredentials(new AuthScope(configuration.getUrl(), 443), credentials);
			log.debug("Attemping to execute HTTP request...");
			httpClient.executeMethod(request);
			log.debug("HTTP request executed. Http response received succesfully.");
		} catch (IOException ioe) {
			log.error("Failed to execute HTTP request.");
			throw new CabinetException(ErrorCodes.HTTP_IO_EXCEPTION, ioe);
		}
		List<Publication> publications = null;
		try {
			log.debug("Attemping to parse http response...");
			String xml = request.getResponseBodyAsString();
			xml = xml.replaceAll("&", "&amp;");
			publications = parse(xml);
			log.debug("Http response parsed successfuly. Received {} publications",publications.size());
		} catch (IOException ioe) {
			log.error("Failed to parse http response [{}].", request.getStatusText());
			throw new CabinetException(ErrorCodes.IO_EXCEPTION, ioe);
		} //catch (org.xml.sax.SAXParseException spe) {

		//}
		return (publications != null) ? publications
			: new ArrayList<Publication>();
	}

	protected String capitalize(String name) {
		return StringUtils.capitalize(name.toLowerCase());
	}

	public abstract List<Publication> parse(String xml);

	public abstract HttpMethod getFindPublicationsRequest(int year1, int year2, int authorId);

}
