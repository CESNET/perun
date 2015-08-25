package cz.metacentrum.perun.cabinet.service.impl;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class PubSysTestStrategy implements IFindPublicationsStrategy {

	public final static GetMethod httpGet = new GetMethod("www.metacentrum.cz");

	public HttpMethod getHttpRequest(String authorId, int yearSince, int yearTill, PublicationSystem ps) {
		//return new GetMethod("www.metacentrum.cz");
		return httpGet;
	}

	public List<Publication> parseHttpResponse(String response) {
		List<Publication> publications = new ArrayList<Publication>();
		Publication p = new Publication();
		Author a = new Author();
		a.setFirstName("Pepa");
		a.setLastName("Becher");
		List<Author> authors = new ArrayList<Author>();
		authors.add(a);
		p.setAuthors(authors);
		publications.add(p); // we have one publication in list..
		return publications;
	}

}
