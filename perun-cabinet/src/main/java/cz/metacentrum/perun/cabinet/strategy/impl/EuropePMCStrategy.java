package cz.metacentrum.perun.cabinet.strategy.impl;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.AbstractPublicationSystemStrategy;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * EuropePMC publication system
 *
 * Remote API Documentation: https://europepmc.org/RestfulWebService
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EuropePMCStrategy extends AbstractPublicationSystemStrategy {

	private static Logger log = LoggerFactory.getLogger(EuropePMCStrategy.class);

	@Override
	public List<Publication> parseHttpResponse(HttpResponse response) throws CabinetException {
		try {
			return parseResponse(EntityUtils.toString(response.getEntity(), "utf-8"));
		} catch (IOException e) {
			throw new CabinetException(ErrorCodes.IO_EXCEPTION, e);
		}
	}

	@Override
	public HttpUriRequest getHttpRequest(String orcid, int yearSince, int yearTill, PublicationSystem ps) {

		// yearTill is expected 0
		// yearSince holds specific year

		// set params
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("query", "AUTHORID:\""+orcid+"\" sort_date:y PUB_YEAR:"+yearSince+""));
		formparams.add(new BasicNameValuePair("pageSize", "50"));
		formparams.add(new BasicNameValuePair("resultType", "core"));

		// prepare valid uri
		URI uri = null;
		try {
			uri = new URI(ps.getUrl() + URLEncodedUtils.format(formparams, "UTF-8"));
			// log response into /var/log/perun/perun-cabinet.log
			//log.debug("URI: {}", uri);

		} catch (URISyntaxException e) {
			log.error("Wrong URL syntax for contacting OrcID europepmc publication system.", e);
		}

		return new HttpGet(uri);

	}

	/**
	 * Parse String response as XML document and retrieve Publications from it.
	 * @param xml XML response from EuropePMC
	 * @return List of Publications
	 * @throws CabinetException If anything fails
	 */
	protected List<Publication> parseResponse(String xml) throws CabinetException {

		assert xml != null;
		List<Publication> result = new ArrayList<Publication>();
		//hook for titles with &
		xml= xml.replace("&", "&amp;");

		//log.debug("RESPONSE: "+xml);

		//Create new document factory builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new CabinetException("Error when creating newDocumentBuilder.", ex);
		}

		Document doc;
		try {
			doc = builder.parse(new InputSource(new StringReader(xml)));
		} catch (SAXParseException ex) {
			throw new CabinetException("Error when parsing uri by document builder.", ErrorCodes.MALFORMED_HTTP_RESPONSE, ex);
		} catch (SAXException ex) {
			throw new CabinetException("Problem with parsing is more complex, not only invalid characters.", ErrorCodes.MALFORMED_HTTP_RESPONSE, ex);
		} catch (IOException ex) {
			throw new CabinetException("Error when parsing uri by document builder. Problem with input or output.", ErrorCodes.MALFORMED_HTTP_RESPONSE, ex);
		}

		//Prepare xpath expression
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		XPathExpression publicationsQuery;
		try {
			publicationsQuery = xpath.compile("/responseWrapper/resultList/result");
		} catch (XPathExpressionException ex) {
			throw new CabinetException("Error when compiling xpath query.", ex);
		}

		NodeList nodeList;
		try {
			nodeList = (NodeList) publicationsQuery.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			throw new CabinetException("Error when evaluate xpath query on document.", ex);
		}

		//Test if there is any nodeset in result
		if(nodeList.getLength() == 0) {
			//There is no results, return empty subjects
			return result;
		}

		//Iterate through nodes and convert them to Map<String,String>
		for(int i=0; i<nodeList.getLength(); i++) {
			Node singleNode = nodeList.item(i);
			// remove node from original structure in order to keep access time constant (otherwise is exp.)
			singleNode.getParentNode().removeChild(singleNode);
			try {
				Publication publication = convertNodeToPublication(singleNode, xPathFactory);
				result.add(publication);
			} catch (InternalErrorException ex) {
				log.error("Unable to parse Publication:", ex);
			}
		}

		return result;
	}

	/**
	 * Convert node of response to Publication
	 *
	 * @param node XML node to convert
	 * @return Publication instance
	 * @throws InternalErrorException
	 */
	private Publication convertNodeToPublication(Node node, XPathFactory xPathFactory) throws InternalErrorException, CabinetException {

		Publication publication = new Publication();

		publication.setExternalId(((Double)getValueFromXpath(node, "./id/text()", XPathConstants.NUMBER)).intValue());

		String title = (String) getValueFromXpath(node, "./title/text()", XPathConstants.STRING);
		publication.setTitle((title != null) ? title : "");

		//optional properties
		String issn = (String) getValueFromXpath(node, "./journalInfo/journal/ISSN/text()", XPathConstants.STRING);
		publication.setIsbn((issn != null) ? issn : "");

		String isbn = (String) getValueFromXpath(node, "./bookOrReportDetails/isbn13/text()", XPathConstants.STRING);
		if (publication.getIsbn() == null) {
			publication.setIsbn((isbn != null) ? isbn : "");
		}

		String doi = (String) getValueFromXpath(node, "./doi/text()", XPathConstants.STRING);
		publication.setDoi((doi != null) ? doi : "");

		int year = ((Double)getValueFromXpath(node, "./pubYear/text()", XPathConstants.NUMBER)).intValue();
		publication.setYear(year);

		XPath xpath = xPathFactory.newXPath();
		XPathExpression authorsQuery;
		try {
			authorsQuery = xpath.compile("./authorList/author");
		} catch (XPathExpressionException ex) {
			throw new CabinetException("Error when compiling xpath query.", ex);
		}

		NodeList nodeList;
		try {
			nodeList = (NodeList) authorsQuery.evaluate(node, XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			throw new CabinetException("Error when evaluate xpath query on document.", ex);
		}

		if(nodeList.getLength() == 0) {
			// There are no authors !! Which is weird, return rest of publication
			return publication;
		} else {

			List<Author> authors = new ArrayList<Author>();

			//Iterate through nodes and convert them to Map<String,String>
			for(int i=0; i<nodeList.getLength(); i++) {
				Node singleNode = nodeList.item(i);
				// remove node from original structure in order to keep access time constant (otherwise is exp.)
				singleNode.getParentNode().removeChild(singleNode);
				try {
					String firstName = ((String)getValueFromXpath(singleNode, "./firstName/text()", XPathConstants.STRING));
					String lastName = ((String)getValueFromXpath(singleNode, "./lastName/text()", XPathConstants.STRING));
					String initials = ((String)getValueFromXpath(singleNode, "./initials/text()", XPathConstants.STRING));
					Author author = new Author();
					if (firstName == null || firstName.isEmpty()) {
						author.setFirstName(initials.trim());
					} else {
						author.setFirstName(firstName.trim());
					}
					author.setLastName(WordUtils.capitalize(lastName.trim()));
					authors.add(author);
				} catch (InternalErrorException ex) {
					log.error("Exception [{}] caught while processing authors of response: [{}]", ex, node);
				}
			}

			publication.setAuthors(authors);

		}

		// Make up citation

		List<Author> authors = publication.getAuthors();
		String main = "";
		for (int i=0; i<authors.size(); i++){
			if (i == 0) {
				main += authors.get(i).getLastName().toUpperCase() + " " + authors.get(i).getFirstName();
			} else {
				main += authors.get(i).getFirstName() + " " + authors.get(i).getLastName().toUpperCase();
			}
			main += " a ";
		}
		if (main.length() > 3) {
			main = main.substring(0, main.length()-3)+ ". ";
		}
		main = main.replaceAll("\\s{2,}", " ");
		main += publication.getTitle() + ((publication.getTitle().endsWith(".")) ? " " : ". ");
		main += (publication.getYear() != 0) ? publication.getYear()+". " : "";

		String journalTitle = (String) getValueFromXpath(node, "./journalInfo/journal/title/text()", XPathConstants.STRING);
		String journalYear = (String) getValueFromXpath(node, "./journalInfo/yearOfPublication/text()", XPathConstants.STRING);
		String journalIssue = (String) getValueFromXpath(node, "./journalInfo/volume/text()", XPathConstants.STRING);
		String pages = (String) getValueFromXpath(node, "./bookOrReportDetails/numberOfPages/text()", XPathConstants.STRING);

		main += (!journalTitle.isEmpty()) ? journalTitle+", " : "";
		main += (!journalYear.isEmpty()) ? " roč. "+journalYear+", " : "";
		main += (!journalIssue.isEmpty()) ? " č. "+journalIssue+", " : "";
		main += (!pages.isEmpty()) ? "s. "+pages+"," : "";
		main += (isbn != null && !isbn.isEmpty()) ? " ISBN: "+isbn+"." : "";
		main += (issn != null && !issn.isEmpty()) ? " ISSN: "+issn+"." : "";
		main += (doi != null && !doi.isEmpty()) ? " DOI: "+doi+"." : "";
		publication.setMain(main);

		return publication;

	}

}
