package cz.metacentrum.perun.cabinet.strategy.impl;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for MU PublicationSystem strategy
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MUStrategyUnitTest {

	private MUStrategy muStrategy = new MUStrategy();

	@Test
	public void getHttpRequest() throws Exception {
		System.out.println("MUStrategyUnitTest.getHttpRequest");

		PublicationSystem ps = new PublicationSystem();
		ps.setLoginNamespace("mu");
		ps.setUsername("test");
		ps.setPassword("test");
		ps.setUrl("http://www.seznam.cz");

		HttpUriRequest result = muStrategy.getHttpRequest("1", 2009, 2010, ps);
		assert result != null;

	}

	@Test
	public void parseResponse() throws Exception {
		System.out.println("MUStrategyUnitTest.parseResponse");

		List<Publication> publications = muStrategy.parseResponse(muPublicationsResponse);

		assertNotNull(publications);
		assertEquals(4, publications.size());

		Publication p = publications.get(0);
		final String title = "Network Identity Manager Providers.";
		final Integer year = 2009;

		assertTrue(p.getMain().contains(title));
		assertEquals(title, p.getTitle());
		assertEquals("missing isbn should be default (=empty string)", "", p.getIsbn());
		assertTrue(year == p.getYear());
		assertNotNull(p.getExternalId());
		assertTrue(p.getExternalId() > 0);

		List<Author> authors = p.getAuthors();
		assertEquals(5, authors.size());
		assertTrue(authors.get(0) != null);
		assertTrue(authors.get(1) != null);
		assertTrue(authors.get(2) != null);
		assertTrue(authors.get(3) != null);
		assertTrue(authors.get(4) != null);

		final String firstName = "Michal";
		final String lastName = "Procházka";

		Author a =  authors.get(1);

		assertEquals(firstName, a.getFirstName());
		assertEquals(lastName, a.getLastName());
	}

	@Test
	public void buildRequestKeyfile() throws Exception {
		System.out.println("MUStrategyUnitTest.buildRequestKeyfile");

		String result = muStrategy.buildRequestKeyfile(39700, 2008, 2010);
		assertNotNull(result);
		assert result.contains("39700");
		assert result.contains("2008");
		assert result.contains("2009");
		assert result.contains("2010");

	}

	@Test
	public void buildYearsCriteria() throws Exception {
		System.out.println("MUStrategyUnitTest.buildYearsCriteria");

		String keyfile = MUStrategy.buildYearsCriteria(2008, 2010);
		String expected = "<PUBL_AUTOR UCO=\"uco\"/> and (<PUBL_ROK ROK=\"2008\"/> or "+
				"<PUBL_ROK ROK=\"2009\"/> or <PUBL_ROK ROK=\"2010\"/>)";
		assertEquals(expected, keyfile);
	}

	public final static String muPublicationsResponse = "<P>\n" +
			"<UL>\n" +
			"\n" +
			"<publication>\n" +
			"<authors><names>RNDr. Daniel Kouřil, Ph.D.# RNDr. Michal Procházka, Ph.D.# Mgr. Tomáš Kubina# Jeffrey Altman# Asanka Herath\n" +
			"</names>\n" +
			"<uco>1388</uco><uco> 39700</uco><uco> 172593</uco><uco> </uco><uco> </uco>\n" +
			"</authors>\n" +
			"<id>843019</id>\n" +
			"<main>KOUŘIL, Daniel, Michal PROCHÁZKA, Tomáš KUBINA, Jeffrey ALTMAN\n" +
			"\ta Asanka HERATH. Network Identity Manager Providers. In AFS &\n" +
			"\tKerberos Best Practices Workshop. 2009.</main>\n" +
			"<isbn></isbn>\n" +
			"<doi></doi>\n" +
			"<year>2009</year>\n" +
			"\t<title>Network Identity Manager Providers.</title>\n" +
			"</publication>\n" +
			"\n" +
			"<publication>\n" +
			"  <authors><names>RNDr. David Antoš, Ph.D.# RNDr. Lukáš Hejtmánek, Ph.D.# Jiří Chudoba# RNDr. Daniel Kouřil, Ph.D.# prof. RNDr. Luděk Matyska, CSc.# RNDr. Michal Procházka, Ph.D.\n" +
			"</names>\n" +
			"<uco>3077</uco><uco> 3545</uco><uco> </uco><uco> 1388</uco><uco> 1904</uco><uco> 39700</uco>\n" +
			"</authors>\n" +
			"<id>849978</id>\n" +
			"<main>ANTOŠ, David, Lukáš HEJTMÁNEK, Jiří CHUDOBA, Daniel KOUŘIL,\n" +
			"\tLuděk MATYSKA a Michal PROCHÁZKA. Správa dat v gridových\n" +
			"\tsystémech. In Proceedings of the Annual Database Conference\n" +
			"\tDATAKON 2009. 1. vyd. Praha: Oeconomica, Praha, 2009. s.\n" +
			"251-265, 15 s. ISBN 978-80-245-1568-7.</main>\n" +
			"<isbn>978-80-245-1568-7</isbn>\n" +
			"<doi></doi>\n" +
			"<year>2009</year>\n" +
			"\t<title>Správa dat v gridových systémech.</title>\n" +
			"</publication>\n" +
			"\n" +
			"<publication>\n" +
			"  <authors><names>RNDr. Martin Drašar, Ph.D.# Mgr. Martin Juřen# RNDr. Daniel Kouřil, Ph.D.# RNDr. Pavel Minařík, PhD.# RNDr. Michal Procházka, Ph.D.# RNDr. Jan Vykopal, Ph.D.\n" +
			"</names>\n" +
			"<uco>98998</uco><uco> 324776</uco><uco> 1388</uco><uco> 51832</uco><uco> 39700</uco><uco> 98724</uco>\n" +
			"</authors>\n" +
			"<id>949192</id>\n" +
			"<main>DRAŠAR, Martin, Martin JUŘEN, Daniel KOUŘIL, Pavel MINAŘÍK,\n" +
			"\tMichal PROCHÁZKA a Jan VYKOPAL. Intelligent logging server.\n" +
			"\t\t\tWarsaw: SECURE 2010, 2010.</main>\n" +
			"<isbn></isbn>\n" +
			"<doi></doi>\n" +
			"<year>2010</year>\n" +
			"\t<title>Intelligent logging server.</title>\n" +
			"</publication>\n" +
			"\n" +
			"<publication>\n" +
			"  <authors><names>RNDr. Michal Procházka, Ph.D.# doc. MUDr. Josef Feit, CSc.\n" +
			"</names>\n" +
			"<uco>39700</uco><uco> 1565</uco>\n" +
			"</authors>\n" +
			"<id>857582</id>\n" +
			"<main>PROCHÁZKA, Michal a Josef FEIT. Atlases jako poskytovatel\n" +
			"\tslužby ve federacích. Brno: Masarykova univerzita, 2009. ISBN\n" +
			"978-80-7392-118-7.</main>\n" +
			"<isbn>978-80-7392-118-7</isbn>\n" +
			"<doi></doi>\n" +
			"<year>2009</year>\n" +
			"\t<title>Atlases jako poskytovatel služby ve federacích.</title>\n" +
			"</publication>\n" +
			"</UL>\n" +
			"</P>";

}
