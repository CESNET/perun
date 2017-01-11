package cz.metacentrum.perun.cabinet.strategy;

import java.io.IOException;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Shared logic for PublicationSystem strategies.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public abstract class AbstractPublicationSystemStrategy implements PublicationSystemStrategy {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
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

	/**
	 * Get xml Node and xpath expression to get value from node by this xpath.
	 *
	 * @param node node for getting value from
	 * @param xpathExpression expression for xpath to looking for value in node
	 * @param resultType type of resulting / expected object (string number node nodelist ...)
	 * @return object extracted from node by xpath
	 * @throws InternalErrorException
	 */
	protected Object getValueFromXpath(Node node, String xpathExpression, QName resultType) throws InternalErrorException {
		//Prepare xpath expression
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(xpathExpression);
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when compiling xpath query.", ex);
		}

		Object result;
		try {
			result = expr.evaluate(node, resultType);
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when evaluate xpath query on node.", ex);
		}

		return result;
	}

}
