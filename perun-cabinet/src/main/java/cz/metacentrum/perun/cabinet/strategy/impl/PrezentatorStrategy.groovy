package cz.metacentrum.perun.cabinet.strategy.impl;

import java.util.List;

import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody

import cz.metacentrum.perun.cabinet.model.Author
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.strategy.AbstractPublicationSystemStrategy;

public class PrezentatorStrategy extends AbstractPublicationSystemStrategy {

	public PrezentatorStrategy(PublicationSystem config) {
		//TODO pouzij tuto tridu misto MUStrategy, predtim
		// vyres problem s vytvarenim instance reflexi a konstruktorem s parametrem
		// ktery se pouziva v CabinetServiceImpl
		super(config);
	}

	@Override
	public List<Publication> parse(String xml) {
		if (xml == null) throw new NullPointerException("cannot parse null");

		List<Publication> result = new ArrayList<Publication>()
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

				//parse authors
				List<Author> authors = new ArrayList<Author>()
				String[] names = it.authors.names[0].text().split("-")
				// assert doesnt work when we don't know author's uco
				//assert names.length == it.authors[0].uco.size() // xml is as expected
				int namesIndex = 0
				it.authors[0].uco.each { uco ->

					Author author = new Author()
					if (uco.text()) { //if element uco is not empty then the author is qualified

						// 3.7.2012 these properties were removed from author - do not use 
						//author.setNamespace(configuration.getLoginNamespace())
						//author.setNamespaceLogin(uco.text())
					}

					String firstName = parseNames(names[namesIndex], 1)
					String lastName = parseNames(names[namesIndex], 0)

					author.setFirstName(firstName)
					author.setLastName(lastName)

					authors.add(author)
					namesIndex++
				}
				publication.setAuthors(authors)
				result.add(publication)
			}
		} catch (org.xml.sax.SAXParseException e) {
			log.error("Failed during parsing XML response. Maybe the response format has changed?")
			throw new CabinetException(ErrorCodes.MALFORMED_XML_RESPONSE, e)
		}
		return result;
	}

	@Override
	public HttpUriRequest getFindPublicationsRequest(int year1, int year2, int authorId) {
		//prepare request body
		MultipartEntity entity = new MultipartEntity();
		try {
		entity.addPart("typ", new StringBody("xml"));
		entity.addPart("kodovani", new StringBody("utf-8"));
		entity.addPart("keyfile", new ByteArrayBody(buildRequestKeyfile(uco.toInteger(), yearSince, yearTill).getBytes(), "template.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		//prepare post request
		HttpPost post = new HttpPost(configuration.getUrl())
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword())
		post.addHeader(BasicScheme.authenticate(credentials, "utf-8", false));//cred, enc, proxy
		post.setEntity(entity)
		return post
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
		for (year in year1..year2) {
			sb.append(criteriaYearBegin).append(year).append(criteriaYearEnd)
		}
		sb.append(criteriaEnd)
		return sb.toString()
	}

	private String parseNames(String s, int indexToReturn) {
		String[] names = s.split(",")
		return capitalize(names[indexToReturn].replace(".", "").trim())
	}

	private final String publicationsRequest = '''<TELO>
<P>
<TFORMA>
	&lt;publication&gt;
		  &lt;authors&gt;&lt;names&gt;<F_PUBL UDAJE="autori" ODDELOVAC="-" VSE="a"/>
	&lt;/names&gt;
	&lt;uco&gt;<F_PUBL_AUTORI FORMAT="uco" ODDELOVAC="&lt;/uco&gt;&lt;uco&gt;" VSE="a" />&lt;/uco&gt;
	&lt;/authors&gt;
	&lt;id&gt;<F_PUBL_ID/>&lt;/id&gt;
	&lt;main&gt;<F_PUBL UDAJE="main" FORMAT="text"/>&lt;/main&gt;
	&lt;isbn&gt;<F_PUBL_ISBN FORMAT="text"/>&lt;/isbn&gt;
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
