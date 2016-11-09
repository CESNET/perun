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
		assertEquals(4, publications.size());

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
		"<PUBL_ROK ROK=\"2009\"/> or <PUBL_ROK ROK=\"2010\"/>)";

		assertEquals(expected, keyfile);
	}

	public final static String muPublicationsResponse = '''
<P>
<UL>

<publication>
<authors><names>RNDr. Daniel Kouřil, Ph.D.# RNDr. Michal Procházka, Ph.D.# Mgr. Tomáš Kubina# Jeffrey Altman# Asanka Herath
</names>
<uco>1388</uco><uco> 39700</uco><uco> 172593</uco><uco> </uco><uco> </uco>
</authors>
<id>843019</id>
<main>KOUŘIL, Daniel, Michal PROCHÁZKA, Tomáš KUBINA, Jeffrey ALTMAN
a Asanka HERATH. Network Identity Manager Providers. In AFS &
Kerberos Best Practices Workshop. 2009.</main>
<isbn></isbn>
<doi></doi>
<year>2009</year>
<title>Network Identity Manager Providers.</title>
</publication>

<publication>
  <authors><names>RNDr. David Antoš, Ph.D.# RNDr. Lukáš Hejtmánek, Ph.D.# Jiří Chudoba# RNDr. Daniel Kouřil, Ph.D.# prof. RNDr. Luděk Matyska, CSc.# RNDr. Michal Procházka, Ph.D.
</names>
<uco>3077</uco><uco> 3545</uco><uco> </uco><uco> 1388</uco><uco> 1904</uco><uco> 39700</uco>
</authors>
<id>849978</id>
<main>ANTOŠ, David, Lukáš HEJTMÁNEK, Jiří CHUDOBA, Daniel KOUŘIL,
Luděk MATYSKA a Michal PROCHÁZKA. Správa dat v gridových
systémech. In Proceedings of the Annual Database Conference
DATAKON 2009. 1. vyd. Praha: Oeconomica, Praha, 2009. s.
251-265, 15 s. ISBN 978-80-245-1568-7.</main>
<isbn>978-80-245-1568-7</isbn>
<doi></doi>
<year>2009</year>
<title>Správa dat v gridových systémech.</title>
</publication>

<publication>
  <authors><names>RNDr. Martin Drašar, Ph.D.# Mgr. Martin Juřen# RNDr. Daniel Kouřil, Ph.D.# RNDr. Pavel Minařík, PhD.# RNDr. Michal Procházka, Ph.D.# RNDr. Jan Vykopal, Ph.D.
</names>
<uco>98998</uco><uco> 324776</uco><uco> 1388</uco><uco> 51832</uco><uco> 39700</uco><uco> 98724</uco>
</authors>
<id>949192</id>
<main>DRAŠAR, Martin, Martin JUŘEN, Daniel KOUŘIL, Pavel MINAŘÍK,
Michal PROCHÁZKA a Jan VYKOPAL. Intelligent logging server.
Warsaw: SECURE 2010, 2010.</main>
<isbn></isbn>
<doi></doi>
<year>2010</year>
<title>Intelligent logging server.</title>
</publication>

<publication>
  <authors><names>RNDr. Michal Procházka, Ph.D.# doc. MUDr. Josef Feit, CSc.
</names>
<uco>39700</uco><uco> 1565</uco>
</authors>
<id>857582</id>
<main>PROCHÁZKA, Michal a Josef FEIT. Atlases jako poskytovatel
služby ve federacích. Brno: Masarykova univerzita, 2009. ISBN
978-80-7392-118-7.</main>
<isbn>978-80-7392-118-7</isbn>
<doi></doi>
<year>2009</year>
<title>Atlases jako poskytovatel služby ve federacích.</title>
</publication>
</UL>
</P>
''';

}
