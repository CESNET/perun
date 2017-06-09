package cz.metacentrum.perun.cabinet.strategy.impl;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.strategy.AbstractPublicationSystemStrategy;
import cz.metacentrum.perun.cabinet.strategy.PublicationSystemStrategy;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Utils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of publication connector for MU (prezentator).
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class MUStrategy extends AbstractPublicationSystemStrategy {

	private Logger log = LoggerFactory.getLogger(MUStrategy.class);

	private String publicationsRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<TELO>\n<P>\n<TFORMA>\n" +
			"&lt;publication&gt;" +
			"&lt;authors&gt;&lt;names&gt;<F_PUBL_AUTORI FORMAT=\"plne_jmeno\" ODDELOVAC=\"#\" VSE=\"1\"/> " +
			"&lt;/names&gt; " +
			"&lt;uco&gt;<F_PUBL_AUTORI FORMAT=\"uco\" ODDELOVAC=\"&lt;/uco&gt;&lt;uco&gt;\" VSE=\"1\" />&lt;/uco&gt; " +
			"&lt;/authors&gt; " +
			"&lt;id&gt;<F_PUBL_ID/>&lt;/id&gt; " +
			"&lt;main&gt;<F_PUBL UDAJE= \"main\" FORMAT=\"text\"/>&lt;/main&gt; " +
			"&lt;isbn&gt;<F_PUBL_ISBN FORMAT=\"text\"/>&lt;/isbn&gt; " +
			"&lt;doi&gt;<F_PUBL_DOI/>&lt;/doi&gt; " +
			"&lt;year&gt;<F_PUBL_ROK/>&lt;/year&gt; " +
			"&lt;title&gt;<F_PUBL UDAJE=\"nazev\" FORMAT=\"text\"/>&lt;/title&gt; " +
			"&lt;/publication&gt;" +
			"</TFORMA>\n<UL>\n" +
			"<DATA_PUBL>\n" +
			"REPLACE_BY_CRITERIA\n" +
			"</DATA_PUBL>\n" +
			"</UL>\n</P>\n</TELO>";

	private final static String criteriaBegin = "<PUBL_AUTOR UCO=\"uco\"/> and (<PUBL_ROK ROK=\"";
	private final static String criteriaYearBegin = " or <PUBL_ROK ROK=\"";
	private final static String criteriaYearEnd = "\"/>";
	private final static String criteriaEnd = ")";

	@Override
	public List<Publication> parseHttpResponse(HttpResponse response) throws CabinetException {
		try {
			return parseResponse(EntityUtils.toString(response.getEntity(), "utf-8"));
		} catch (IOException e) {
			throw new CabinetException(ErrorCodes.IO_EXCEPTION, e);
		}
	}

	@Override
	public HttpUriRequest getHttpRequest(String uco, int yearSince, int yearTill, PublicationSystem ps) {
		//prepare request body
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		try {
			entityBuilder.addPart("typ", new StringBody("xml", ContentType.create("text/plain", Consts.UTF_8)));
			entityBuilder.addPart("kodovani", new StringBody("utf-8", ContentType.create("text/plain", Consts.UTF_8)));
			entityBuilder.addPart("keyfile", new ByteArrayBody(buildRequestKeyfile(Integer.parseInt(uco), yearSince, yearTill).getBytes(), "template.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		//prepare post request
		HttpPost post = new HttpPost(ps.getUrl());
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(ps.getUsername(), ps.getPassword());
		post.addHeader(BasicScheme.authenticate(credentials, "utf-8", false));//cred, enc, proxy
		post.setEntity(entityBuilder.build());
		return post;

	}

	/**
	 * Parse String response as XML document and retrieve Publications from it.
	 * @param xml XML response from MU Prezentator
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
			publicationsQuery = xpath.compile("/P/UL/publication");
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

		publication.setExternalId(((Double)getValueFromXpath(node, "./id/text()", XPathConstants.NUMBER)).intValue());
		publication.setMain(((String)getValueFromXpath(node, "./main/text()", XPathConstants.STRING)));

		//optional properties
		String isbn = (String) getValueFromXpath(node, "./isbn/text()", XPathConstants.STRING);
		publication.setIsbn((isbn != null) ? isbn : "");

		String title = (String) getValueFromXpath(node, "./title/text()", XPathConstants.STRING);
		publication.setTitle((title != null) ? title : "");

		String doi = (String) getValueFromXpath(node, "./doi/text()", XPathConstants.STRING);
		publication.setDoi((doi != null) ? doi : "");

		int year = ((Double)getValueFromXpath(node, "./year/text()", XPathConstants.NUMBER)).intValue();
		publication.setYear(year);

		// parse authors
		List<Author> authors = new ArrayList<Author>();
		String[] names = ((String)getValueFromXpath(node, "./authors/names/text()", XPathConstants.STRING)).split("#");
		for (String name : names) {
			try {
				Author author = new Author();
				Map<String,String> commonName = Utils.parseCommonName(name);
				author.setFirstName(commonName.get("firstName"));
				author.setLastName(WordUtils.capitalize(commonName.get("lastName")));
				authors.add(author);
			} catch (Exception ex) {
				log.error("Exception [{}] caught while processing authors of response: [{}]", ex, node);
			}
		}

		publication.setAuthors(authors);

		return publication;

	}

	/**
	 * Build request key file for MU Prezentator
	 * (define what data in which format to retrieve)
	 *
	 * @param uco UČO of user to get publications for
	 * @param year1 lower year
	 * @param year2 higher year
	 * @return Request key file (xml)
	 */
	protected String buildRequestKeyfile(int uco, int year1, int year2) {
		assert uco > 0;
		String criteria = buildYearsCriteria(year1, year2);
		criteria = criteria.replace("uco", Integer.toString(uco));
		return publicationsRequest.replace("REPLACE_BY_CRITERIA", criteria);
	}

	/**
	 * Build years criteria XML for a request to MU Prezentator.
	 *
	 * @param year1 lower year
	 * @param year2 higher year
	 * @return XML criteria string
	 */
	protected static String buildYearsCriteria(int year1, int year2) {
		assert year1 <= year2;

		StringBuilder sb = new StringBuilder();
		sb.append(criteriaBegin);
		sb.append(year1).append(criteriaYearEnd);
		int year = year1+1;
		while (year <= year2) {
			sb.append(criteriaYearBegin).append(year).append(criteriaYearEnd);
			year++;
		}
		sb.append(criteriaEnd);
		return sb.toString();
	}

}
