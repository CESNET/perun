package cz.metacentrum.perun.cabinet.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy;

public class PubSysTestStrategy implements IFindPublicationsStrategy {

	public final static HttpGet httpGet = new HttpGet("www.metacentrum.cz");

	public HttpUriRequest getHttpRequest(String authorId, int yearSince, int yearTill, PublicationSystem ps) {
		//return new HttpGet("www.metacentrum.cz");
		return httpGet;
	}

	public List<Publication> parseHttpResponse(HttpResponse response) {
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
