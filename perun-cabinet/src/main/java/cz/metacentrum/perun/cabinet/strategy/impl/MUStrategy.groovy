package cz.metacentrum.perun.cabinet.strategy.impl

import groovy.xml.XmlUtil
import org.apache.http.Consts
import org.apache.http.HttpResponse
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import cz.metacentrum.perun.cabinet.model.Author
import cz.metacentrum.perun.cabinet.model.Publication
import cz.metacentrum.perun.cabinet.model.PublicationSystem
import cz.metacentrum.perun.cabinet.service.CabinetException
import cz.metacentrum.perun.cabinet.service.ErrorCodes
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy
import cz.metacentrum.perun.core.impl.Utils

/**
 * Groovy class for retrieving publications from external source
 * Prezentator MU
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: MUStrategy.groovy 6260 2013-02-25 17:16:46Z 256627 $
 */
class MUStrategy implements IFindPublicationsStrategy {

	private static final String MU = "MU";

	private Logger log = LoggerFactory.getLogger(MUStrategy.class);

	public List<Publication> parseHttpResponse(HttpResponse response) {
		return parseResponse(EntityUtils.toString(response.getEntity(), "utf-8"))
	}

	public HttpUriRequest getHttpRequest(String uco, int yearSince, int yearTill, PublicationSystem ps) {

		//prepare request body
		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		try {
			entityBuilder.addPart("typ", new StringBody("xml", ContentType.create("text/plain", Consts.UTF_8)));
			entityBuilder.addPart("kodovani", new StringBody("utf-8", ContentType.create("text/plain", Consts.UTF_8)));
			entityBuilder.addPart("keyfile", new ByteArrayBody(buildRequestKeyfile(uco.toInteger(), yearSince, yearTill).getBytes(), "template.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

        //prepare post request
		HttpPost post = new HttpPost(ps.getUrl())
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(ps.getUsername(), ps.getPassword())
		post.addHeader(BasicScheme.authenticate(credentials, "utf-8", false));//cred, enc, proxy
		post.setEntity(entityBuilder.build())
		return post

	}

	public List<Publication> parseResponse(String xml) {

		// log response into /var/log/perun/perun-cabinet.log
		// log.debug(xml)

		assert xml != null
		List<Publication> result = new ArrayList<Publication>()
		//hook for titles with &
		xml= xml.replace("&", "&amp;")

		try {
			def rootNode = new XmlParser().parseText(xml)
			rootNode.UL.publication.findAll {

				Publication publication = new Publication()
				//required properties
				publication.setExternalId(it.id.text().toInteger())
				publication.setMain(it.main.text())

				//optional properties
				publication.setIsbn((it.isbn.text()) ? it.isbn.text() : "")
				publication.setYear((it.year.text()) ? it.year.text().toInteger() : 0)
				publication.setTitle((it.title.text()) ? it.title.text() : "")
				publication.setDoi((it.doi.text()) ? it.doi.text() : "")

				//parse authors
				List<Author> authors = new ArrayList<Author>()
				String[] names = it.authors.names[0].text().split("#")
				int namesIndex = 0
				for (String name : names) {
					try {

						Author author = new Author()
						Map<String,String> commonName = Utils.parseCommonName(name)

						author.setFirstName(commonName.get("firstName"))
						author.setLastName(capitalize(commonName.get("lastName")))

						authors.add(author)
						namesIndex++

					} catch (Exception ex) {
						log.error("Exception [{}] caught while processing authors of response: [{}]", ex, xml)
					}
				}

				publication.setAuthors(authors)
				result.add(publication)
			}
		} catch (org.xml.sax.SAXParseException e) {
			log.error("Failed during parsing XML response. Maybe the response format has changed?")
            throw new CabinetException(ErrorCodes.MALFORMED_HTTP_RESPONSE, e)
		}
		return result;
	}

	public String buildRequestKeyfile(int uco, int year1, int year2) {
		assert uco > 0

		String criteria = buildYearsCriteria(year1, year2)
		String keyfile = publicationsRequest.replace("REPLACE_BY_CRITERIA", criteria)
		Node rootNode = new XmlParser().parseText(keyfile) //check validity?
		rootNode.P.UL.DATA_PUBL.PUBL_AUTOR.@UCO=uco
		return XmlUtil.serialize(rootNode)
	}

	public String buildYearsCriteria(int year1, int year2) {
		assert year1 <= year2

		StringBuffer sb = new StringBuffer()
		sb.append(criteriaBegin)
		sb.append(year1).append(criteriaYearEnd)
		for (year in year1+1..year2) {
			sb.append(criteriaYearBegin).append(year).append(criteriaYearEnd)
		}
		sb.append(criteriaEnd)
		return sb.toString()
	}

	private String capitalize(String name) {
		return name.toLowerCase().capitalize()
	}

	private final String publicationsRequest = '''<TELO>
<P>
<TFORMA>
	&lt;publication&gt;
		  &lt;authors&gt;&lt;names&gt;<F_PUBL_AUTORI FORMAT="plne_jmeno" ODDELOVAC="#" VSE="1"/>
	&lt;/names&gt;
	&lt;uco&gt;<F_PUBL_AUTORI FORMAT="uco" ODDELOVAC="&lt;/uco&gt;&lt;uco&gt;" VSE="1" />&lt;/uco&gt;
	&lt;/authors&gt;
	&lt;id&gt;<F_PUBL_ID/>&lt;/id&gt;
	&lt;main&gt;<F_PUBL UDAJE="main" FORMAT="text"/>&lt;/main&gt;
	&lt;isbn&gt;<F_PUBL_ISBN FORMAT="text"/>&lt;/isbn&gt;
	&lt;doi&gt;<F_PUBL_DOI/>&lt;/doi&gt;
	&lt;year&gt;<F_PUBL_ROK/>&lt;/year&gt;
	&lt;title&gt;<F_PUBL UDAJE="nazev" FORMAT="text"/>&lt;/title&gt;
	&lt;/publication&gt;
</TFORMA><UL>

<DATA_PUBL>
	REPLACE_BY_CRITERIA
</DATA_PUBL>

</UL>

</P>
</TELO>'''

	String criteriaBegin = '''<PUBL_AUTOR UCO="uco"/>  and (<PUBL_ROK ROK="'''
	String criteriaYearBegin = ''' or <PUBL_ROK ROK="'''
	String criteriaYearEnd = '''"/>'''
	String criteriaEnd = ''')'''



}
