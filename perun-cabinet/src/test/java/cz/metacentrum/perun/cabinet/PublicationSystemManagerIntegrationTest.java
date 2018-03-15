package cz.metacentrum.perun.cabinet;

import org.junit.Test;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.strategy.PublicationSystemStrategy;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * Integration tests of AuthorshipManager
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PublicationSystemManagerIntegrationTest extends CabinetBaseIntegrationTest {

	@Test
	public void createPublicationSystem() throws Exception {
		System.out.println("PublicationSystemManagerIntegrationTest.createPublicationSystem");

		PublicationSystem ps = new PublicationSystem();
		ps.setFriendlyName("PS for tests");
		ps.setLoginNamespace("some");
		ps.setUrl("http://seznam.cz");
		ps.setUsername("");
		ps.setPassword("");
		ps.setType("cz.metacentrum.perun.cabinet.strategy.impl.MUStrategy");

		ps = getCabinetManager().createPublicationSystem(sess, ps);
		PublicationSystem ps2 = getCabinetManager().getPublicationSystemById(ps.getId());
		assertEquals(ps, ps2);

	}


	@Test
	public void updatePublicationSystem() throws Exception {
		System.out.println("PublicationSystemManagerIntegrationTest.updatePublicationSystem");

		PublicationSystem ps = new PublicationSystem();
		ps.setFriendlyName("PS for tests");
		ps.setLoginNamespace("some");
		ps.setUrl("http://seznam.cz");
		ps.setUsername("");
		ps.setPassword("");
		ps.setType("cz.metacentrum.perun.cabinet.strategy.impl.MUStrategy");

		ps = getCabinetManager().createPublicationSystem(sess, ps);

		PublicationSystem ps2 = getCabinetManager().getPublicationSystemById(ps.getId());
		assertEquals(ps, ps2);

		ps.setUrl("http://www.seznam.cz");
		ps = getCabinetManager().updatePublicationSystem(sess, ps);

		PublicationSystem ps3 = getCabinetManager().getPublicationSystemById(ps.getId());
		assertEquals(ps.getUrl(), ps3.getUrl());
		assertFalse(Objects.equals(ps2.getUrl(), ps3.getUrl()));

	}

	@Test
	public void deletePublicationSystem() throws Exception {
		System.out.println("PublicationSystemManagerIntegrationTest.deletePublicationSystem");

		List<PublicationSystem> systems = getCabinetManager().getPublicationSystems(sess);

		assertNotNull(systems);
		assertTrue(!systems.isEmpty());

		PublicationSystem ps = systems.get(0);

		getCabinetManager().deletePublicationSystem(sess, ps);

		List<PublicationSystem> systems2 = getCabinetManager().getPublicationSystems(sess);
		assertNotNull(systems2);
		assertTrue(!systems2.isEmpty());
		assertTrue(!systems2.contains(ps));

	}

	@Test
	public void getPublicationSystems() throws Exception {
		System.out.println("PublicationSystemManagerIntegrationTest.getPublicationSystems");
		List<PublicationSystem> systems = getCabinetManager().getPublicationSystems(sess);

		assertNotNull(systems);
		assertTrue(!systems.isEmpty());
		assertTrue(systems.contains(pubSysMu));
		assertTrue(systems.contains(pubSysZcu));
		assertTrue(systems.contains(pubSysEuropePMC));
		assertTrue(systems.size() == 4);

	}

	@Test
	public void getPublicationSystemByNamespace() throws Exception {
		System.out.println("PublicationSystemManagerIntegrationTest.getPublicationSystemByNamespace");

		PublicationSystem publicationSystem = getCabinetManager().getPublicationSystemByNamespace("mu");
		assertNotNull(publicationSystem);
		assertTrue(Objects.equals(publicationSystem, pubSysMu));

		PublicationSystemStrategy ps = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(ps);
	}

	@Test
	public void getPublicationSystemById() throws Exception {
		System.out.println("PublicationSystemManagerIntegrationTest.getPublicationSystemById");

		PublicationSystem publicationSystem = getCabinetManager().getPublicationSystemById(pubSysZcu.getId());
		assertNotNull(publicationSystem);
		assertTrue(Objects.equals(publicationSystem, pubSysZcu));

		PublicationSystemStrategy ps = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(ps);

	}

	@Test
	public void getPublicationSystemByName() throws Exception {
		System.out.println("PublicationSystemManagerIntegrationTest.getPublicationSystemByName");

		PublicationSystem publicationSystem = getCabinetManager().getPublicationSystemByName(pubSysZcu.getFriendlyName());
		assertNotNull(publicationSystem);
		assertTrue(Objects.equals(publicationSystem, pubSysZcu));

		PublicationSystemStrategy ps = (PublicationSystemStrategy) Class.forName(publicationSystem.getType()).newInstance();
		assertNotNull(ps);

	}

}
