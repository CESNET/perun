package cz.metacentrum.perun.cabinet.strategy.impl

import java.util.List
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.methods.GetMethod

import groovy.xml.XmlUtil
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import cz.metacentrum.perun.cabinet.model.Author
import cz.metacentrum.perun.cabinet.model.Publication
import cz.metacentrum.perun.cabinet.model.PublicationSystem
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy

/**
 * Groovy class for retrieving publications from external source
 * OBD ZČU v 2.5
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: OBD25Strategy.groovy 4404 2012-08-06 09:04:55Z 256627 $
 */
class OBD25Strategy implements IFindPublicationsStrategy {

	private Logger log = LoggerFactory.getLogger(OBD25Strategy.class);


	public List<Publication> parseHttpResponse(String response) {
		return parseResponse(response)
	}

	public HttpMethod getHttpRequest(String kodOsoby, int yearSince, int yearTill, PublicationSystem ps) {

		// set params
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("fname", "obd_2_0_export_xml"));
		formparams.add(new BasicNameValuePair("_rokod", yearSince.toString()));
		formparams.add(new BasicNameValuePair("_rokdo", yearTill.toString()));

		// search base on lastName + firstName of person
		//String[] names = kodOsoby.toString().split(",");
		//formparams.add(new BasicNameValuePair("_aup", names[0]));
		//formparams.add(new BasicNameValuePair("_auj", names[1]));

		// search based on lastName
		formparams.add(new BasicNameValuePair("_aup", kodOsoby));

		// log uri into alcor.ics.muni.cz:  /home/perun/.perunv3/logs/perun-cabinet.log
		// log.debug(uri)

		return new GetMethod(ps.getUrl() + URLEncodedUtils.format(formparams, "UTF-8"))
	}

	public List<Publication> parseResponse(String xmlResponse) {
		assert xmlResponse != null

		// log response into alcor.ics.muni.cz:  /home/perun/.perunv3/logs/perun-cabinet.log
		// log.debug(xmlResponse)

		List<Publication> result = new ArrayList<Publication>()
		try {
			def rootNode = new XmlParser().parseText(xmlResponse.trim())

			// parse every publication
			rootNode.zaznam.findAll {

				//required properties
				Publication publication = new Publication()
				publication.setExternalId(it.ID.text().toInteger())

				// optional properties
				publication.setIsbn((it.ISSN_ISBN.text()) ? it.ISSN_ISBN.text() : "")
				publication.setTitle((it.TITUL_BEZ_CLENU.text()) ? it.TITUL_BEZ_CLENU.text() : "")
				publication.setYear((it.ROK.text()) ? it.ROK.text().toInteger() : 0) //TODO externalize default values for obd and mu

				List<Author> authors = new ArrayList<Author>()

				// authors
				it.autori[0].autor.each {

					Author author = new Author()
					if (it.kodautora.text()) {
						//prepare delete QualAuth..
						//QualifiedAuthor a = new QualifiedAuthor()
						//a.setExtPubSourceAliasId(it.kodautora.text().toLong())

						// 3.7.2012 these properties were removed from author - do not use
						//author.setNamespace('ZCU');
						//author.setNamespaceLogin(it.kodautora.text())

					}

					author.setFirstName(it.jmeno.text())
					author.setLastName(it.prijmeni.text())
					authors.add(author)

				}

				publication.setAuthors(authors)

				// create main entry
				String main = ""
				for (int i=0; i<authors.size(); i++){
					if (i == 0) {
						main += authors.get(i).getLastName().toUpperCase() + " " + authors.get(i).getFirstName()
					} else {
						main += authors.get(i).getFirstName() + " " + authors.get(i).getLastName().toUpperCase()
					}
					main += " a "
				}
				if (main.length() > 3) {
					main = main.substring(0, main.length()-3)+ ". "
				}
				main += it.TITUL_BEZ_CLENU.text() + ". "
				main += (it.ROK.text()) ? it.ROK.text()+". " : ""
				main += (it.ISSN_ISBN.text()) ? "ISBN: "+it.ISSN_ISBN.text()+"." : ""
				publication.setMain(main)

				result.add(publication)

			}
		} catch (org.xml.sax.SAXParseException e) {
			//throw new MalformedResponseException("Unexpected OBD xml response. Response or request format changed?",e)
			log.error("Failed during parsing XML response. Maybe the response format has changed?")
			throw new CabinetException(ErrorCodes.MALFORMED_HTTP_RESPONSE, e)

		}
		return result;
	}

}