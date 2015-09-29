package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

/**
 * Integration tests of ExtSourcesManager
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ExtSourcesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String EXT_SOURCE_NAME = "ExtSourcesManagerEntryIntegrationTest";
	private static final String CLASS_NAME = "ExtSourcesManager.";
	private ExtSourcesManager extSourcesManagerEntry;
	private static ExtSource extSource;

	@Before
	public void setUp() throws Exception {
		ExtSource newExtSource = new ExtSource(EXT_SOURCE_NAME, ExtSourcesManager.EXTSOURCE_INTERNAL);
		extSource = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);
		this.extSourcesManagerEntry = perun.getExtSourcesManager();
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#createExtSource(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.ExtSource)}.
	 */
	@Test
	public void testCreateExtSource() throws Exception {
		System.out.println(CLASS_NAME + "createExtSource");

		final ExtSource es = newInstanceExtSource();

		final ExtSource createdExtSource = extSourcesManagerEntry.createExtSource(sess, es, null);

		assertNotNull(createdExtSource);
		assertTrue(createdExtSource.getId() > 0);
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#deleteExtSource(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.ExtSource)}.
	 */
	@Test(expected=ExtSourceNotExistsException.class)
	public void testDeleteExtSource() throws Exception {
		System.out.println(CLASS_NAME + "deleteExtSource");

		final ExtSource es = newInstanceExtSource();
		final ExtSource createdExtSource = extSourcesManagerEntry.createExtSource(sess, es, null);
		assertTrue(createdExtSource.getId() > 0);

		extSourcesManagerEntry.deleteExtSource(sess, createdExtSource);

		extSourcesManagerEntry.getExtSourceById(sess, createdExtSource.getId());
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#getExtSourceById(cz.metacentrum.perun.core.api.PerunSession, int)}.
	 */
	@Test
	public void testGetExtSourceById() throws Exception {
		System.out.println(CLASS_NAME + "getExtSourceById");

		final ExtSource es = newInstanceExtSource();
		final ExtSource createdExtSource = extSourcesManagerEntry.createExtSource(sess, es, null);
		assertTrue(createdExtSource.getId() > 0);

		assertEquals(createdExtSource, extSourcesManagerEntry.getExtSourceById(sess, createdExtSource.getId()));
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#getExtSourceByName(cz.metacentrum.perun.core.api.PerunSession, java.lang.String)}.
	 */
	@Test
	public void testGetExtSourceByName() throws Exception {
		System.out.println(CLASS_NAME + "getExtSourceByName");

		final ExtSource es = extSourcesManagerEntry.getExtSourceByName(sess, EXT_SOURCE_NAME);

		assertNotNull(es);
		assertEquals(EXT_SOURCE_NAME, es.getName());
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#getVoExtSources(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo)}.
	 */
	@Test
	public void testGetVoExtSources() throws Exception {
		System.out.println(CLASS_NAME + "getVoExtSources");

		final VosManagerEntry vosManagerEntry = new VosManagerEntry(perun);
		final Vo createdVo = vosManagerEntry.createVo(sess, new Vo(0,"sjk","kljlk"));

		final ExtSource extSource = newInstanceExtSource();
		extSourcesManagerEntry.createExtSource(sess, extSource, null);
		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);

		final List<ExtSource> extSources = extSourcesManagerEntry.getVoExtSources(sess, createdVo);

		assertNotNull(extSources);
		assertTrue(extSources.contains(extSource));
	}

	@Test
	public void testGetGroupExtSources() throws Exception {
		System.out.println(CLASS_NAME + "getGroupExtSources");

		final VosManagerEntry vosManagerEntry = new VosManagerEntry(perun);
		final Vo createdVo = vosManagerEntry.createVo(sess, new Vo(0,"sjk","kljlk"));
		final GroupsManagerEntry groupsManagerEntry = new GroupsManagerEntry(perun);
		final Group createdGroup = groupsManagerEntry.createGroup(sess, createdVo, new Group("sjk", "kljlk"));

		final ExtSource extSource = newInstanceExtSource();
		extSourcesManagerEntry.createExtSource(sess, extSource, null);
		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);
		extSourcesManagerEntry.addExtSource(sess, createdGroup, extSource);

		final List<ExtSource> extSources = extSourcesManagerEntry.getGroupExtSources(sess, createdGroup);

		assertNotNull(extSources);
		assertTrue(extSources.contains(extSource));
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#getExtSources(cz.metacentrum.perun.core.api.PerunSession)}.
	 */
	@Test
	public void testGetExtSources() throws Exception {
		System.out.println(CLASS_NAME + "testGetExtSources");

		final List<ExtSource> extSources;
		extSources = extSourcesManagerEntry.getExtSources(sess);

		assertTrue(extSources.size() > 0);
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#addExtSource(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo, cz.metacentrum.perun.core.api.ExtSource)}.
	 */
	@Test
	public void testAddExtSourceToVo() throws Exception {
		System.out.println(CLASS_NAME + "addExtSource");

		final VosManagerEntry vosManagerEntry = new VosManagerEntry(perun);
		final Vo createdVo = vosManagerEntry.createVo(sess, new Vo(0,"sjk","kljlk"));

		final ExtSource extSource = newInstanceExtSource();
		extSourcesManagerEntry.createExtSource(sess, extSource, null);
		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);

		final List<ExtSource> extSources = extSourcesManagerEntry.getVoExtSources(sess, createdVo);

		assertNotNull(extSources);
		assertTrue(extSources.contains(extSource));
	}

	@Test
	public void testAddExtSourceToGroup() throws Exception {
		System.out.println(CLASS_NAME + "addExtSource");

		final VosManagerEntry vosManagerEntry = new VosManagerEntry(perun);
		final Vo createdVo = vosManagerEntry.createVo(sess, new Vo(0,"sjk","kljlk"));
		final GroupsManagerEntry groupsManagerEntry = new GroupsManagerEntry(perun);
		final Group createdGroup = groupsManagerEntry.createGroup(sess, createdVo, new Group("sjk", "kljlk"));

		final ExtSource extSource = newInstanceExtSource();
		extSourcesManagerEntry.createExtSource(sess, extSource, null);
		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);
		extSourcesManagerEntry.addExtSource(sess, createdGroup, extSource);

		final List<ExtSource> extSources = extSourcesManagerEntry.getGroupExtSources(sess, createdGroup);

		assertNotNull(extSources);
		assertTrue(extSources.contains(extSource));
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#checkOrCreateExtSource(cz.metacentrum.perun.core.api.PerunSession, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCheckOrCreateExtSource() throws Exception {
		System.out.println(CLASS_NAME + "getVoExtSources");

		final ExtSource extSource = extSourcesManagerEntry.checkOrCreateExtSource(sess, EXT_SOURCE_NAME, "cz.metacentrum.perun.core.impl.ExtSourceSql");
		final ExtSource extSource2 = extSourcesManagerEntry.checkOrCreateExtSource(sess, EXT_SOURCE_NAME, "cz.metacentrum.perun.core.impl.ExtSourceSql");

		assertEquals(extSource, extSource2);
	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#removeExtSource(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo, cz.metacentrum.perun.core.api.ExtSource)}.
	 */
	@Test
	public void testRemoveExtSourceFromVo() throws Exception {
		System.out.println(CLASS_NAME + "removeExtSource");

		final VosManagerEntry vosManagerEntry = new VosManagerEntry(perun);
		final Vo createdVo = vosManagerEntry.createVo(sess, new Vo(0,"sjk","kljlk"));

		final ExtSource extSource = newInstanceExtSource();
		extSourcesManagerEntry.createExtSource(sess, extSource, null);
		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);

		final List<ExtSource> extSources = extSourcesManagerEntry.getVoExtSources(sess, createdVo);
		assertTrue(extSources.size() > 0);

		extSourcesManagerEntry.removeExtSource(sess, createdVo, extSource);

		final List<ExtSource> extSourcesResult = extSourcesManagerEntry.getVoExtSources(sess, createdVo);
		assertTrue( ! extSourcesResult.contains(extSource));

	}

	@Test
	public void testRemoveExtSourceFromGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeExtSource");

		final VosManagerEntry vosManagerEntry = new VosManagerEntry(perun);
		final Vo createdVo = vosManagerEntry.createVo(sess, new Vo(0,"sjk","kljlk"));
		final GroupsManagerEntry groupsManagerEntry = new GroupsManagerEntry(perun);
		final Group createdGroup = groupsManagerEntry.createGroup(sess, createdVo, new Group("sjk", "kljlk"));

		final ExtSource extSource = newInstanceExtSource();
		extSourcesManagerEntry.createExtSource(sess, extSource, null);
		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);
		extSourcesManagerEntry.addExtSource(sess, createdGroup, extSource);

		final List<ExtSource> extSources = extSourcesManagerEntry.getGroupExtSources(sess, createdGroup);
		assertTrue(extSources.size() > 0);

		extSourcesManagerEntry.removeExtSource(sess, createdGroup, extSource);

		final List<ExtSource> extSourcesResult = extSourcesManagerEntry.getGroupExtSources(sess, createdGroup);
		assertTrue( ! extSourcesResult.contains(extSource));

	}

	/**
	 * Test method for {@link cz.metacentrum.perun.core.blImpl.ExtSourcesManagerBlImpl#getInvalidUsers(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.ExtSource)}.
	 */
	@Ignore
	@Test
	public void testGetInvalidUsers() throws Exception {
		fail("not implemented");
	}

	@Ignore
	@Test
	public void getCandidate() throws Exception {
		System.out.println(CLASS_NAME + "getCandidate");

		// TODO create searchable ext source (mock ?)
		// search for specific Candidate
		fail("not implemented");

	}

	@Ignore
	@Test (expected=CandidateNotExistsException.class)
	public void getCandidateWhenCandidateNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getCandidateWhenCandidateNotExists");

		// TODO create searchable ExtSource (mock ?)
		// search for specific Candidate which doesn't exists in ExtSource.
		fail("not implemented");

	}

	@Test (expected=ExtSourceNotExistsException.class)
	public void getExtSourceByNameWhenExtSourceNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getExtSourceByNameWhenExtSourceNotExists");

		extSourcesManagerEntry.getExtSourceByName(sess, "nonsense");
		// shouldn't find ext source

	}

	@Test (expected=ExtSourceNotExistsException.class)
	public void getExtSourceByIdWhenExtSourceNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getExtSourceByIdWhenExtSourceNotExists");

		extSourcesManagerEntry.getExtSourceById(sess, 0);
		// shouldn't find ext source

	}

	@Test (expected=VoNotExistsException.class)
	public void addExtSourceWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addExtSourceWhenVoNotExists");

		extSourcesManagerEntry.addExtSource(sess, new Vo(), extSource);
		// shouldn't find VO

	}

	@Test (expected=ExtSourceNotExistsException.class)
	public void addExtSourceWhenExtSourceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addExtSourceWhenExtSourceNotExists");

		ExtSource source = new ExtSource(0, "Fake", ExtSourcesManager.EXTSOURCE_INTERNAL);
		Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "sjk", "kljlk"));
		extSourcesManagerEntry.addExtSource(sess, createdVo, source);
		// shouldn't find Ext Source

	}

	@Test (expected=ExtSourceAlreadyAssignedException.class)
	public void addExtSourceWhenExtSourceAlreadyAssigned() throws Exception {
		System.out.println(CLASS_NAME + "addExtSourceWhenExtSourceAlreadyAssigned");

		VosManager vosManager = perun.getVosManager();
		Vo createdVo = vosManager.createVo(sess, new Vo(0,"sjk","kljlk"));

		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);
		extSourcesManagerEntry.addExtSource(sess, createdVo, extSource);
		// shouldn't add same ext source twice

	}

	@Test (expected=ExtSourceNotAssignedException.class)
	public void removeExtSourceWhenExtSourceNotAssigned() throws Exception {
		System.out.println(CLASS_NAME + "removeExtSourceWhenExtSourceNotAssigned");

		VosManager vosManager = perun.getVosManager();
		Vo createdVo = vosManager.createVo(sess, new Vo(0,"sjk","kljlk"));

		extSourcesManagerEntry.removeExtSource(sess, createdVo, extSource);
		// shouldn't find not assigned ext source

	}

	@Test (expected=ExtSourceNotExistsException.class)
	public void removeExtSourceWhenExtSourceNotExist() throws Exception {
		System.out.println(CLASS_NAME + "removeExtSourceWhenExtSourceNotExist");

		VosManager vosManager = perun.getVosManager();
		Vo createdVo = vosManager.createVo(sess, new Vo(0, "sjk", "kljlk"));

		ExtSource source = new ExtSource(0, "Fake", ExtSourcesManager.EXTSOURCE_INTERNAL);
		extSourcesManagerEntry.removeExtSource(sess, createdVo, source);
		// shouldn't find invalid ext source

	}

	@Test (expected=VoNotExistsException.class)
	public void getVoExtSourcesWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getVoExtSourcesWhenVoNotExists");

		extSourcesManagerEntry.getVoExtSources(sess, new Vo());

	}


	// private methods --------------------------------------------------------

	private ExtSource newInstanceExtSource() {
		final ExtSource es;
		es = new ExtSource();
		es.setName("SomeExtSource");
		es.setType(ExtSourcesManager.EXTSOURCE_SQL);
		return es;
	}

}
