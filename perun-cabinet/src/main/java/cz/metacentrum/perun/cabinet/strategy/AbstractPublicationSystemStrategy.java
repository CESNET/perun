package cz.metacentrum.perun.cabinet.strategy;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * Shared logic for PublicationSystem strategies.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public abstract class AbstractPublicationSystemStrategy implements PublicationSystemStrategy {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public List<Publication> fetchPublications(String authorId, int yearSince, int yearTill, PublicationSystem ps)
          throws CabinetException {

    HttpUriRequest request = getHttpRequest(authorId, yearSince, yearTill, ps);
    String response = execute(request);
    return parseResponse(response);

  }

  @Override
  public String execute(HttpUriRequest request) throws CabinetException {

    RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(300000)
            .setSocketTimeout(300000)
            .build();

    try (CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build()) {

      log.debug("Attempting to execute HTTP request...");
      try (CloseableHttpResponse response = httpClient.execute(request)) {

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          throw new CabinetException(
                  "Can't contact publication system. HTTP error code: " + response.getStatusLine().getStatusCode(),
                  ErrorCodes.HTTP_IO_EXCEPTION);
        }

        log.debug("HTTP request executed.");
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

      }

    } catch (IOException ioe) {
      log.error("Failed to execute HTTP request.");
      throw new CabinetException(ErrorCodes.HTTP_IO_EXCEPTION, ioe);
    }

  }

  /**
   * Get xml Node and xpath expression to get value from node by this xpath.
   *
   * @param node            node for getting value from
   * @param xpathExpression expression for xpath to looking for value in node
   * @param resultType      type of resulting / expected object (string number node nodelist ...)
   * @return object extracted from node by xpath
   * @throws InternalErrorException
   */
  protected Object getValueFromXpath(Node node, String xpathExpression, QName resultType) {
    //Prepare xpath expression
    XPathFactory xpathfactory = XPathFactory.newInstance();
    XPath xpath = xpathfactory.newXPath();
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
