package cz.metacentrum.perun.cabinet.strategy;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;

/**
 * This interface represents the general strategy used for searching publications
 * in concrete PublicationSystem (i.e. MU prezentator or OBD). The passed PubSys. is just
 * data transfer object containing i.e. url.
 * Creating the http request and parsing the response according to the appropriate PubSys
 * is the main purpose of classes implementing this interface.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public interface IFindPublicationsStrategy {

	/**
	 * Create HTTP GET/POST request based on PublicationSystem strategy.
	 *
	 * @param authorId Author's identification in external PS, should be unique
	 * @param yearSince Since which year should publications be (filter results)
	 * @param yearTill Until which year should publications be (filter results)
	 * @param ps PublicationSystem to be "asked" for publications
	 * @return HTTP request
	 */
	HttpUriRequest getHttpRequest(String authorId, int yearSince, int yearTill, PublicationSystem ps);

	/**
	 * Parse XML response from external PS into list of user's publications.
	 *
	 * @param response HTTP response returned from external PS
	 * @return List of user's publications. Empty list if nothing found.
	 */
	List<Publication> parseHttpResponse(HttpResponse response);

}
