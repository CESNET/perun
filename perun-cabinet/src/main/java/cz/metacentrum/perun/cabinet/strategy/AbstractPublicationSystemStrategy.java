package cz.metacentrum.perun.cabinet.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;

public abstract class AbstractPublicationSystemStrategy {

	private Logger log = LoggerFactory.getLogger(getClass());
	protected PublicationSystem configuration;

	public AbstractPublicationSystemStrategy(PublicationSystem config) {
		this.configuration = config;
	}

	public List<Publication> findPublications(int year1, int year2, int authorId) throws CabinetException {

		HttpResponse response;
		try {
			log.debug("Getting http request.");
			HttpUriRequest request = getFindPublicationsRequest(year1, year2, authorId);
			if (request == null)
				throw new NullPointerException("http request cannot be null");
			log.debug("Got http request.");
			HttpClient httpClient = new DefaultHttpClient();
			log.debug("Attemping to execute HTTP request...");
			response = httpClient.execute(request);
			log.debug("HTTP request executed. Http response received succesfully.");
		} catch (IOException ioe) {
			log.error("Failed to execute HTTP request.");
			throw new CabinetException(ErrorCodes.HTTP_IO_EXCEPTION, ioe);
		}
		List<Publication> publications = null;
		try {
			log.debug("Attemping to parse http response...");
			String xml = EntityUtils.toString(response.getEntity(),
					"utf-8");
			xml = xml.replaceAll("&", "&amp;");
			publications = parse(xml);
			log.debug("Http response parsed successfuly. Received {} publications",publications.size());
		} catch (IOException ioe) {
			log.error("Failed to parse http response [{}].", response.toString());
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

	public abstract HttpUriRequest getFindPublicationsRequest(int year1, int year2, int authorId);

}
