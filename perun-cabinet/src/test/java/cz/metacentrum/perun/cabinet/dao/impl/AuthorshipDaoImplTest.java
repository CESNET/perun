package cz.metacentrum.perun.cabinet.dao.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.service.SortParam;
import cz.metacentrum.perun.cabinet.service.impl.BaseIntegrationTest;

public class AuthorshipDaoImplTest extends BaseIntegrationTest {


	@Test
	public void exampleSortParam() {
		System.out.println("AuthorshipDaoImpl.exampleSortParam");

		Authorship authorship = new Authorship();
		authorship.setUserId(USER_ID);
		authorship.setPublicationId(publicationOne.getId());

		SortParam sp = null;
		sp = new SortParam(0,8,"createdDate",false);
		//get ordered list
		List<Authorship> result = authorshipDao.findByFilter(authorship, sp);

		assertNotNull(result);
		assertTrue(result.size() > 0);
		if (result.size() > 1) {
			assertTrue(result.get(0).getCreatedDate().after(result.get(1).getCreatedDate()));
		}

		//get reversed list
		sp = new SortParam(0,8,"createdDate", true);
		List<Authorship> result2 = authorshipDao.findByFilter(authorship, sp);

		assertNotNull(result2);
		assertTrue(result2.size() > 0);
		if (result2.size() > 1) {
			assertTrue(result2.get(0).getCreatedDate().before(result2.get(1).getCreatedDate()));
		}
	}

}
