package cz.metacentrum.perun.cabinet.strategy.impl;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.strategy.AbstractPublicationSystemStrategy;
import cz.metacentrum.perun.cabinet.strategy.PublicationSystemStrategy;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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

import javax.xml.namespace.QName;
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
 *
 * Implementation of publication connector for OBD 3.0 from ZČU.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class OBD30Strategy extends AbstractPublicationSystemStrategy {

	private Logger log = LoggerFactory.getLogger(OBD30Strategy.class);

	// http://obd.zcu.cz:6443/fcgi/verso.fpl?fname=obd_exportt_xml&_a_prijmeni=Habernal&_diakritika=0&_a_jmeno=Ivan&_diakritika=0

	@Override
	public List<Publication> parseHttpResponse(HttpResponse response) throws CabinetException {
		try {
			return parseResponse(EntityUtils.toString(response.getEntity(), "utf-8"));
		} catch (IOException e) {
			throw new CabinetException(ErrorCodes.IO_EXCEPTION, e);
		}
	}

	@Override
	public HttpUriRequest getHttpRequest(String kodOsoby, int yearSince, int yearTill, PublicationSystem ps) {

		// set params
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("fname", "obd_exportt_xml"));
		formparams.add(new BasicNameValuePair("_v_vlo", String.valueOf(yearSince)));
		formparams.add(new BasicNameValuePair("_v_vld", String.valueOf(yearTill)));

		// search base on lastName + firstName of person
		String[] names = String.valueOf(kodOsoby).split(",");
		formparams.add(new BasicNameValuePair("_a_prijmeni", names[0]));
		formparams.add(new BasicNameValuePair("_diakritika", "0"));
		formparams.add(new BasicNameValuePair("_a_jmeno", names[1]));
		formparams.add(new BasicNameValuePair("_diakritika", "0"));

		// prepare valid uri
		URI uri = null;
		try {
			uri = new URI(ps.getUrl() + URLEncodedUtils.format(formparams, "UTF-8"));
			// log response into /var/log/perun/perun-cabinet.log
			//log.debug("URI: {}", uri);

		} catch (URISyntaxException e) {
			log.error("Wrong URL syntax for contacting OBD 3.0 publication system.", e);
		}

		return new HttpGet(uri);

	}

	/**
	 * Parse String response as XML document and retrieve Publications from it.
	 * @param xml XML response from OBD 3.0
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
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression publicationsQuery;
		try {
			publicationsQuery = xpath.compile("/zaznamy/zaznam");
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
				Publication publication = convertNodeToPublication(singleNode);
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
	private Publication convertNodeToPublication(Node node) throws InternalErrorException {

		Publication publication = new Publication();

		publication.setExternalId(Integer.valueOf(node.getAttributes().getNamedItem("id").getNodeValue()));

		NodeList titleList = (NodeList) getValueFromXpath(node, "./titul_list/titul", XPathConstants.NODESET);
		for(int i=0; i<titleList.getLength(); i++) {
			Node singleNode = titleList.item(i);

			String original = (String)getValueFromXpath(singleNode, "./original/text()", XPathConstants.STRING);
			// use original name (language) of publication
			if ("ano".equalsIgnoreCase(original)) {
				String title = (String) getValueFromXpath(singleNode, "./nazev/text()", XPathConstants.STRING);
				publication.setTitle((title != null) ? title : "");
			}

		}

		String isbn = (String) getValueFromXpath(node, "./isbn/text()", XPathConstants.STRING);
		isbn = (isbn != null) ? isbn : "";

		String issn = (String) getValueFromXpath(node, "./issn/text()", XPathConstants.STRING);
		issn = (issn != null) ? issn : "";

		if (!issn.isEmpty()) publication.setIsbn(issn);
		if (!isbn.isEmpty()) publication.setIsbn(isbn);

		int year = ((Double)getValueFromXpath(node, "./rok/text()", XPathConstants.NUMBER)).intValue();
		publication.setYear(year);

		String source = (String) getValueFromXpath(node, "./zdroj_nazev/text()", XPathConstants.STRING);
		source = (source != null) ? source : "";

		String source_year = (String) getValueFromXpath(node, "./rocnik/text()", XPathConstants.STRING);
		source_year = (source_year != null) ? source_year : "";

		String pages = (String) getValueFromXpath(node, "./strany/text()", XPathConstants.STRING);
		pages = (pages != null) ? pages : "";

		String issue = (String) getValueFromXpath(node, "./cislo/text()", XPathConstants.STRING);
		issue = (issue != null) ? issue : "";

		// parse authors
		List<Author> authors = new ArrayList<Author>();

		NodeList authorList = (NodeList) getValueFromXpath(node, "./autor_list/autor", XPathConstants.NODESET);
		for(int i=0; i<authorList.getLength(); i++) {
			Node singleNode = authorList.item(i);

			Author author = new Author();
			author.setTitleBefore((String)getValueFromXpath(singleNode, "./titul_pred/text()", XPathConstants.STRING));
			author.setTitleAfter((String)getValueFromXpath(singleNode, "./titul_za/text()", XPathConstants.STRING));
			author.setFirstName((String)getValueFromXpath(singleNode, "./jmeno/text()", XPathConstants.STRING));
			author.setLastName((String)getValueFromXpath(singleNode, "./prijmeni/text()", XPathConstants.STRING));
			authors.add(author);
		}

		publication.setAuthors(authors);

		// create main entry
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
		main += publication.getTitle() + ". ";
		main += (publication.getYear() != null) ? publication.getYear()+". " : "";

		main += (!source.isEmpty()) ? source+"," : "";
		main += (!source_year.isEmpty()) ? " roč. "+source_year+"," : "";
		main += (!issue.isEmpty()) ? " č. "+issue+"," : "";
		main += (!pages.isEmpty()) ? " s. "+pages+"," : "";
		main += (!isbn.isEmpty()) ? " ISBN: "+isbn+"." : "";
		main += (!issn.isEmpty()) ? " ISSN: "+issn+"." : "";
		publication.setMain(main);

		return publication;

	}

}
