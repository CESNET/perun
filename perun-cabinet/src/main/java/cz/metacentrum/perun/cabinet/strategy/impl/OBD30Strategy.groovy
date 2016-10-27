package cz.metacentrum.perun.cabinet.strategy.impl

import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import cz.metacentrum.perun.cabinet.model.Author
import cz.metacentrum.perun.cabinet.model.Publication
import cz.metacentrum.perun.cabinet.model.PublicationSystem
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy

/**
 * Groovy class for retrieving publications from external source
 * OBD ZČU v 3.0
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: $
 */
class OBD30Strategy implements IFindPublicationsStrategy {

	private Logger log = LoggerFactory.getLogger(OBD30Strategy.class);

	// http://obd.zcu.cz:6443/fcgi/verso.fpl?fname=obd_exportt_xml&_a_prijmeni=Habernal&_diakritika=0&_a_jmeno=Ivan&_diakritika=0

	public List<Publication> parseHttpResponse(HttpResponse response) {
		return parseResponse(EntityUtils.toString(response.getEntity(), "utf-8"))
	}

	public HttpUriRequest getHttpRequest(String kodOsoby, int yearSince, int yearTill, PublicationSystem ps) {

		// set params
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("fname", "obd_exportt_xml"));
		formparams.add(new BasicNameValuePair("_v_vlo", yearSince.toString()));
		formparams.add(new BasicNameValuePair("_v_vld", yearTill.toString()));

		// search base on lastName + firstName of person
		String[] names = kodOsoby.toString().split(",");
		formparams.add(new BasicNameValuePair("_a_prijmeni", names[0]));
		formparams.add(new BasicNameValuePair("_diakritika", '0'));
		formparams.add(new BasicNameValuePair("_a_jmeno", names[1]));
		formparams.add(new BasicNameValuePair("_diakritika", '0'));

		// prepare valid uri
		URI uri = new URI(ps.getUrl() + URLEncodedUtils.format(formparams, "UTF-8"));

		// log response into /var/log/perun/perun-cabinet.log
		// log.debug(uri)

		return new HttpGet(uri)
	}

	public List<Publication> parseResponse(String xmlResponse) {
		assert xmlResponse != null

		// log response into /var/log/perun/perun-cabinet.log
		// log.debug(xmlResponse)

		List<Publication> result = new ArrayList<Publication>()
		try {
			def rootNode = new XmlParser().parseText(xmlResponse.trim())

			// parse every publication
			rootNode.zaznam.findAll {

				//required properties
				Publication publication = new Publication()
                publication.setExternalId(it.'@id'.toInteger())
                publication.setYear((it.rok.text()) ? it.rok.text().toInteger() : 0)

                // use TITLE in original language
                it.titul_list.titul.each {
                    if (!it.original.text().equalsIgnoreCase("Ne")) {
                        publication.setTitle((it.nazev.text()) ? it.nazev.text() : "")
                    }
                }

				String source = (it.zdroj_nazev.text()) ? it.zdroj_nazev.text() : ""
				String source_year = (it.rocnik.text()) ? it.rocnik.text() : ""
				String issue = (it.cislo.text()) ? it.cislo.text() : ""
				String pages = (it.strany.text()) ? it.strany.text() : ""
				String isbn = (it.isbn.text()) ? it.isbn.text() : ""
				String issn = (it.issn.text()) ? it.issn.text() : ""

				List<Author> authors = new ArrayList<Author>()

				// authors
				it.autor_list.autor.each {
					Author author = new Author()
					author.setTitleBefore(it.titul_pred.text())
					author.setTitleAfter(it.titul_za.text())
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
				main += publication.getTitle() + ". "
				main += (publication.getYear()) ? publication.getYear()+". " : ""

				main += (source) ? source+"," : "";
				main += (source_year) ? " roč. "+source_year+"," : "";
				main += (issue) ? " č. "+issue+"," : "";
				main += (pages) ? " s. "+pages+"," : "";
				main += (isbn) ? " ISBN: "+isbn+"." : ""
				main += (issn) ? " ISSN: "+issn+"." : ""
				publication.setMain(main)

				publication.setIsbn((isbn && issn) ? (isbn + " / " + issn) : ((isbn) ? isbn : ((issn) ? issn : "")))

				result.add(publication)

			}
		} catch (org.xml.sax.SAXParseException e) {
			//throw new MalformedResponseException("Unexpected OBD xml response. Response or request format changed?",e)
			log.error("Failed during parsing XML response. Maybe the response format has changed?")
			throw new CabinetException(ErrorCodes.MALFORMED_HTTP_RESPONSE, e)
		} catch (Exception ex) {
			log.error("{}", ex);
			throw ex;
		}
		return result;
	}

}
