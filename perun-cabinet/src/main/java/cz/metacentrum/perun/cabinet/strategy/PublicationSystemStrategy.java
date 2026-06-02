package cz.metacentrum.perun.cabinet.strategy;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import java.util.List;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * This interface represents the general strategy used for searching publications in concrete PublicationSystem (i.e. MU
 * prezentator or OBD). The passed PubSys. is just data transfer object containing i.e. url. Creating the http request
 * and parsing the response according to the appropriate PubSys is the main purpose of classes implementing this
 * interface.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public interface PublicationSystemStrategy {

  /**
   * Searches for publications in external system (build and execute request and parse response)
   *
   * @param authorId  Author's identification in external PS, should be unique
   * @param yearSince Since which year should publications be (filter results)
   * @param yearTill  Until which year should publications be (filter results)
   * @param ps        PublicationSystem to be "asked" for publications
   * @return List of publications found by the input data
   * @throws CabinetException When any operation fails.
   */
  List<Publication> fetchPublications(String authorId, int yearSince, int yearTill, PublicationSystem ps)
          throws CabinetException;

  /**
   * Executes a HTTP request (i.e. HttpGet or HttpPost) and returns obtained string response.
   *
   * @param request HTTP request which we get from PSStrategy files (required)
   * @return String response we pass back to PSStrategy files to be parsed as publications
   * @throws CabinetException
   */
  public String execute(HttpUriRequest request) throws CabinetException;

  /**
   * Create HTTP GET/POST request based on PublicationSystem strategy.
   *
   * @param authorId  Author's identification in external PS, should be unique
   * @param yearSince Since which year should publications be (filter results)
   * @param yearTill  Until which year should publications be (filter results)
   * @param ps        PublicationSystem to be "asked" for publications
   * @return HTTP request
   */
  HttpUriRequest getHttpRequest(String authorId, int yearSince, int yearTill, PublicationSystem ps);

  /**
   * Parse String (usually XML) response from external PS into list of user's publications.
   *
   * @param response String response returned from external PS
   * @return List of user's publications. Empty list if nothing found.
   */
  List<Publication> parseResponse(String response) throws CabinetException;

}
