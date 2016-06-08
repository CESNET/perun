package cz.metacentrum.perun.cabinet.strategy.impl

import org.junit.Test

import static org.junit.Assert.*

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;

public class MUStrategyTest {
	
	MUStrategy muStrategy = new MUStrategy();
	
	@Test
	public void getHttpRequest() {
		println 'MUStrategyTest.getHttpRequest'

        PublicationSystem ps = new PublicationSystem();
        ps.setLoginNamespace("mu");
        ps.setUsername("test")
        ps.setPassword("test")
        ps.setUrl("http://www.seznam.cz");

		def result = muStrategy.getHttpRequest("1", 2009, 2010, ps)
		
		assert result != null

	}

	@Test
	public void parseResponse() {
		println 'MUStrategyTest.parseResponse'
		
		List<Publication> publications = muStrategy.parseResponse(muPublicationsResponse);
		
		assertNotNull(publications);
		assertEquals(2, publications.size());
		
		Publication p = publications.get(0);
		final String title = "Network Identity Manager Providers.";
		final int year = 2009;
		
		assertTrue(p.getMain().contains(title));
		assertEquals(title, p.getTitle());
		assertEquals("missing isbn should be default (=empty string)", "", p.getIsbn());
		assertEquals(year, p.getYear());
		assertNotNull(p.getExternalId());
		assertTrue(p.getExternalId() > 0);
		
		List<Author> authors = p.getAuthors();
		assertEquals(5, authors.size());
		assertTrue(authors.get(0) instanceof Author);
		assertTrue(authors.get(1) instanceof Author);
		assertTrue(authors.get(2) instanceof Author);
		assertTrue(authors.get(3) instanceof Author);
		assertTrue(authors.get(4) instanceof Author);
		
		final String firstName = "Michal";
		final String lastName = "Procházka";

		Author a =  authors.get(1);

		assertEquals(firstName, a.getFirstName());
		assertEquals(lastName, a.getLastName());
	}
	
	@Test
	public void parseResponseArrayIndexOutOfBound() {
		System.out.println("MUStrategyTest.parseResponseArrayIndexOutOfBound");
		
		List<Publication> publications = muStrategy.parseResponse(arrayIndexOutOfBoundsResponse2);
		
		assertNotNull(publications);
		assertTrue(publications.size() > 0);
	}
	
	@Test
	public void buildRequestKeyfile() {
		println 'MUStrategyTest.buildRequestKeyfile'
		
		String result = muStrategy.buildRequestKeyfile(39700, 2008, 2010);
		
		assertNotNull(result);
		assert result.contains(39700.toString())
		assert result.contains(2008.toString())
		assert result.contains(2010.toString())
	}
	
	@Test
	public void buildYearsCriteria() {
		println 'MUStrategyTest.buildYearsCriteria'
		
		String keyfile = muStrategy.buildYearsCriteria(2008, 2010);
		String expected = "<PUBL_AUTOR UCO=\"uco\"/>  and (<PUBL_ROK ROK=\"2008\"/> or "+
		"<PUBL_ROK ROK=\"2008\"/> or <PUBL_ROK ROK=\"2009\"/> or <PUBL_ROK ROK=\"2010\"/>)";
		
		assertEquals(expected, keyfile);
	}

	public final static String muPublicationsResponse = '''
	<P>
<UL>


        <publication>
          <authors><names>KOUŘIL, Daniel a Michal PROCHÁZKA a Tomáš KUBINA a Jeffrey ALTMAN a Asanka HERATH.
        </names>
        <uco>1388</uco><uco> 39700</uco><uco> 172593</uco><uco> </uco><uco> </uco>
        </authors>
        <id>843019</id>
        <main>KOUŘIL, Daniel a PROCHÁZKA Michal a KUBINA Tomáš a ALTMAN Jeffrey a HERATH Asanka. Network Identity Manager Providers.
In AFS & Kerberos Best Practices Workshop. 2009.</main>
        <isbn></isbn>
        <year>2009</year>
        <title>Network Identity Manager Providers.</title>
        </publication>

        <publication>
          <authors><names>PROCHÁZKA, Michal a FEIT Josef.
        </names>
        <uco>39700</uco><uco> 1565</uco>
        </authors>
        <id>857582</id>
        <main>PROCHÁZKA, Michal a FEIT Josef. Atlases jako poskytovatel
služby ve federacích. Brno : Masarykova univerzita, 2009. ISBN
978-80-7392-118-7.</main>
        <isbn>978-80-7392-118-7</isbn>
        <year>2009</year>
        <title>Atlases jako poskytovatel služby ve federacích.</title>
        </publication>
        </UL></P>'''
	
		String arrayIndexOutOfBoundsResponse2 = '''<P><UL><publication>
			<authors>
				<names>      DRAŠAR, Martin a Martin JUŘEN.
				</names>
				<uco>      </uco><uco> 98998</uco>
			</authors>
			<id> 949192
			</id>
			<main>
			DRAŠAR, Martin a Martin JUŘEN a Daniel KOUŘIL a Pavel MINAŘÍK a Michal PROCHÁZKA a Jan VYKOPAL. Intelligent logging
				server. Warsaw : SECURE 2010, 2010.
			</main>
			<isbn>
			</isbn>
			<year> 2010
			</year>
			<title> Intelligent logging server.
			</title>
		</publication>
		
	</UL>
</P>''';

}
