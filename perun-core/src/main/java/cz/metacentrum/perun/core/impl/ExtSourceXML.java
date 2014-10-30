package cz.metacentrum.perun.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import java.io.IOException;
import javax.xml.xpath.XPathConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;


/**
 * @author Michal Stava stavamichal@gmail.com
 */
public class ExtSourceXML extends ExtSource implements ExtSourceApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceXML.class);
	
	private String query = null;
	private String loginQuery = null;
	private String file = null;
	private String uri = null;
	
	//URL connection
	private HttpURLConnection con = null;
	
	//Pattern for looking replacement in regex string
	private Pattern pattern = Pattern.compile("([^\\\\]|^)(\\\\\\\\)*\\/([^\\\\]|$)");


	public List<Map<String,String>> findSubjects(String searchString) throws InternalErrorException {
		return findSubjects(searchString, 0);
	}

	public List<Map<String,String>> findSubjects(String searchString, int maxResults) throws InternalErrorException {
		//TODO: Apostrophe can't be use now, need to find solution in the future (KNOWN BUG)
		if(searchString.contains("'")) throw new InternalErrorException("Character ' (apos) is not supported one. Please don't use it for searching.");		
		
		//Get Query attribute from extSources.xml config file
		query = (String) getAttributes().get("xpath");
		if (query == null || query.isEmpty()) {
			throw new InternalErrorException("query attributes is required");
		}
		
		//Replace '?' by searchString
		if(searchString == null) {
			throw new InternalErrorException("search string can't be null");
		}
		query = query.replaceAll("\\?", searchString);
		
		//Get file or uri of xml
		prepareEnviroment();

		return xpathParsing(query, maxResults);
	}

	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException {
		//TODO: Apostrophe can't be use now, need to find solution in the future
		if(login.contains("'")) throw new InternalErrorException("Character ' (apos) is not supported one. Please don't use it for searching.");	
		
		//Get Query attribute from extSources.xml config file
		query = (String) getAttributes().get("loginXpath");
		if (query == null || query.isEmpty()) {
			throw new InternalErrorException("query attributes is required");
		}
		
		//Replace '?' by searchString
		if(login == null || login.isEmpty()) {
			throw new InternalErrorException("login string can't be null or empty");
		}
		query = query.replaceAll("\\?", login);
		
		//Get file or uri of xml
		prepareEnviroment();

		List<Map<String, String>> subjects = this.xpathParsing(query, 0);

		if (subjects.size() > 1) {
			throw new SubjectNotExistsException("There are more than one results for the login: " + login);
		}

		if (subjects.size() == 0) {
			throw new SubjectNotExistsException(login);
		}

		return subjects.get(0);
	}

	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		// Get the query for the group subjects
		String queryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
		
		//If there is no query for group, throw exception
		if(queryForGroup == null) throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSEXTSOURCE_ATTRNAME + " can't be null.");
		
		//Get file or uri of xml
		prepareEnviroment();
		
		return xpathParsing(queryForGroup, 0);
	}
	
	private void prepareEnviroment() throws InternalErrorException {
		//Get file or uri of xml
		file = (String) getAttributes().get("file");
		if(file == null || file.isEmpty()) {
			file = null;
			uri = (String) getAttributes().get("uri");
			if(uri == null || uri.isEmpty()) {
				throw new InternalErrorException("File and uri are both empty, one must exists!.");
			}
		}
	}
	
	/**
	 * Get query and maxResults.
	 * Prepare document and xpathExpression by query.
	 * Get all nodes by xpath from document and parse them one by one.
	 * 
	 * The way of xml take from "file" or "uri" (configuration file)
	 * 
	 * @param query xpath query from config file
	 * @param maxResults never get more than maxResults results (0 mean unlimited)
	 *
	 * @return List of results, where result is Map<String,String> like <name, value>
	 * @throws InternalErrorException 
	 */
	private List<Map<String,String>> xpathParsing(String query, int maxResults) throws InternalErrorException {
		//Prepare result list
		List<Map<String, String>> subjects = new ArrayList<Map<String, String>>();
		
		//Create new document factory builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new InternalErrorException("Error when creating newDocumentBuilder.", ex);
		}
		
		Document doc;
		try {
			if(file != null && !file.isEmpty()) {
				doc = builder.parse(file);
			} else if(uri != null && !uri.isEmpty()) {
				doc = builder.parse(this.createTwoWaySSLConnection(uri));
			} else {
				throw new InternalErrorException("Document can't be parsed, because there is no way (file or uri) to this document in xpathParser.");
			}			
		} catch (SAXException ex) {
			throw new InternalErrorException("Error when parsing uri by document builder.", ex);
		} catch (IOException ex) {
			throw new InternalErrorException("Error when parsing uri by document builder.", ex);
		}
		
		//Prepare xpath expression
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression queryExpr;
		try {
			queryExpr = xpath.compile(query);
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when compiling xpath query.", ex);
		}
		
		//Call query on document node and get back nodesets
		NodeList nodeList;
		try {
			nodeList = (NodeList) queryExpr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when evaluate xpath query on document.", ex);
		}
		
		//Test if there is any nodeset in result
		if(nodeList.getLength() == 0) {
			//There is no results, return empty subjects
			return subjects;
		}
		
		//Iterate through nodes and convert them to Map<String,String>
		for(int i=0; i<nodeList.getLength(); i++) {
			Map<String,String> map = convertNodeToMap(nodeList.item(i));
			if(map != null) subjects.add(map);
			//Reducing results by maxResults
			if(maxResults > 0) {
				if(subjects.size() >= maxResults) break;
			}
		}
		
		this.close();
		return subjects;
	}
	
	/**
	 * Get XML node and convert all values by "xmlMapping" attribute to Map<String,String>
	 * In map there are "name=value" data.
	 * 
	 * Attribute xmlMapping is from file perun-extSource.xml
	 * 
	 * @param node node for converting
	 * @return Map<String,String> like <name,value>
	 * @throws InternalErrorException 
	 */
	private Map<String, String> convertNodeToMap(Node node) throws InternalErrorException {
		Map<String,String> nodeInMap = new HashMap<String,String>();
		//If node is empty, return null
		if(node == null) return null;
		
		String mapping = getAttributes().get("xmlMapping");
		String[] mappingArray = mapping.split(",\n");
		
		for(int i=0; i<mappingArray.length; i++) {
			String attr = mappingArray[i].trim();
			
			int index = attr.indexOf("=");
			
			if(index <= 0) throw new InternalErrorException("There is no text in xmlMapping attribute or there is no '=' character.");
			String name = attr.substring(0, index);
			String value = attr.substring(index +1);
			
			if(value.startsWith("#")) {
				value = value.substring(1);
				String[] regexAndXpath = value.split("#");
				if(regexAndXpath.length != 2) throw new InternalErrorException("There is not only 2 parts (regex and XpathExpression). There are " + regexAndXpath.length + " parts.");
				value = extractValueByRegex(getValueFromXpath(node, regexAndXpath[1]), regexAndXpath[0]);
			} else {
				value = getValueFromXpath(node, value);
			}
			nodeInMap.put(name.trim(), value.trim());
		}
		
		return nodeInMap;
	}
	
	/**
	 * Get xml Node and xpath expression to get value from node by this xpath.
	 * 
	 * @param node node for getting value from
	 * @param xpathExpression expression for xpath to looking for value in node
	 * @return string extracted from node by xpath
	 * @throws InternalErrorException 
	 */
	private String getValueFromXpath(Node node, String xpathExpression) throws InternalErrorException {
		//Prepare xpath expression
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(xpathExpression);
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when compiling xpath query.", ex);
		}
		
		String text;
		try {
			 text = (String) expr.evaluate(node, XPathConstants.STRING);
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when evaluate xpath query on node.", ex);
		}
		
		return text;
	}
	
	/**
	 * Get regex in format 'regex/replacement' and value to get data from.
	 * Use regex and replacement to get data from value.
	 * 
	 * IMPORTANT: Regex must be always in fromat 'regex/replacement' and must have
	 *						exactly 1 existence of character '/' ex. '[abc](a)[b]/$1'
	 * 
	 * @param value some string
	 * @param regex regex in format 'regex/replacement'
	 * @return extracted string from value by regex
	 * 
	 * @throws InternalErrorException 
	 */
	private String extractValueByRegex(String value, String regex) throws InternalErrorException {
		//trim value to erase newlines and spaces before and after value
		value = value.trim();
		//regex need to be separate to 2 parts (regex) and (replacement) separated by backslash - ex 'regex/replacement'
		Matcher match = pattern.matcher(regex);
		
		//need to separate regex to regexPart and replacementPart
		String regexPart;
		String replacementPart;
		if(match.find()) {
			int i = match.end();
			if(match.find()) throw new InternalErrorException("There is more then one separating forward slash in regex without escaping.");
			while(regex.charAt(i) != '/') {
				i--;
				if(i < 0) throw new InternalErrorException("Index of forward slash not found.");
			}
			regexPart = regex.substring(0, i);
			replacementPart = regex.substring(i+1);
		} else {
			throw new InternalErrorException("There is no replacement in regex.");
		}
		
		//use regex and replacement to get string from value
		value = value.replaceAll(regexPart, replacementPart);
		return value;
	}
	
	/**
	 * Get https uri of xml document and create two way ssl connection using truststore and keystore.
	 * 
	 * @param uri https uri to xml document
	 * @return input stream with xml document
	 * 
	 * @throws IOException if there is some input/output error
	 * @throws InternalErrorException if some variables are not correctly filled
	 */
	private InputStream createTwoWaySSLConnection(String uri) throws IOException, InternalErrorException {
		if(uri == null || uri.isEmpty()) throw new InternalErrorException("Uri must be filled, can't be null or empty.");
		
		//KeyStore data
		String keyStore =  getAttributes().get("keyStore");
		String keyStorePass = getAttributes().get("keyStorePass");
		String keyStoreType = getAttributes().get("keyStoreType");
		if(keyStore == null || keyStorePass == null || keyStoreType == null) {
			throw new InternalErrorException("KeystorePath, KeystorePass and KeystoreType must be filled. Please look into configuration file.");
		}
		
		//TrustStore data
		String trustStore = getAttributes().get("trustStore");
		String trustStorePass = getAttributes().get("trustStorePass");
		if(trustStore == null || trustStorePass == null) {
			throw new InternalErrorException("TrustStorePath and TrustStorePass must be filled. Please look into configuration file.");
		}
		
		//set necessary keystore properties - using a p12 file
		System.setProperty("javax.net.ssl.keyStore", keyStore);
		System.setProperty("javax.net.ssl.keyStorePassword", keyStorePass);
		System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);       

		//set necessary truststore properties - using JKS
		System.setProperty("javax.net.ssl.trustStore", trustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePass);
		// register a https protocol handler  - this may be required for previous JDK versions
		System.setProperty("java.protocol.handler.pkgs","com.sun.net.ssl.internal.www.protocol");

		//prepare sslFactory
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		HttpsURLConnection.setDefaultSSLSocketFactory(factory);
		
		URL myurl = new URL(uri);
		con = (HttpURLConnection) myurl.openConnection();

		//set request header if is required (set in extSource xml)
		String reqHeaderKey = getAttributes().get("requestHeaderKey");
		String reqHeaderValue = getAttributes().get("requestHeaderValue");
		if(reqHeaderKey != null) {
			if(reqHeaderValue == null) reqHeaderValue = "";
			con.setRequestProperty(reqHeaderKey, reqHeaderValue);
		}

		return con.getInputStream();
	}
	
	
	
	public void close() throws InternalErrorException {
		if(con != null) con.disconnect();
	}
}
