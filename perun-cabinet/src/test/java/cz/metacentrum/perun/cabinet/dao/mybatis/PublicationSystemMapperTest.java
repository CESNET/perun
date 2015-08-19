package cz.metacentrum.perun.cabinet.dao.mybatis;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import cz.metacentrum.perun.cabinet.service.impl.BaseIntegrationTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.service.IHttpService;
import cz.metacentrum.perun.cabinet.strategy.IFindPublicationsStrategy;

public class PublicationSystemMapperTest extends BaseIntegrationTest {

	private PublicationSystemMapper publicationSystemMapper;
	private IHttpService httpService;

	@Autowired
	public void setHttpService(IHttpService httpService) {
		this.httpService = httpService;
	}

	@Autowired
	public void setPublicationSystem(PublicationSystemMapper publicationSystem) {
		this.publicationSystemMapper = publicationSystem;
	}

	@Test
	public void findPublicationSystemByNamespace() throws Exception {
		System.out.println("PublicationSystemMapper.findPublicationSystemByNamespace");

		PublicationSystemExample example = new PublicationSystemExample();
		String mu = "mu";
		example.createCriteria().andLoginnamespaceEqualTo(mu);
		List<PublicationSystem> publicationSystems = publicationSystemMapper.selectByExample(example);

		PublicationSystem ps = publicationSystems.get(0);
		assertNotNull(ps);

		IFindPublicationsStrategy prezentator = (IFindPublicationsStrategy) Class.forName(ps.getType()).newInstance();
		assertNotNull(prezentator);
	}

	@Test
	public void findPublicationsMUIntegrationTest() throws Exception {
		System.out.println("PublicationSystemMapper.findPublicationsMUIntegrationTest");

		PublicationSystemExample example = new PublicationSystemExample();
		String mu = "mu";
		example.createCriteria().andLoginnamespaceEqualTo(mu);
		List<PublicationSystem> publicationSystems = publicationSystemMapper.selectByExample(example);

		PublicationSystem ps = publicationSystems.get(0);
		assertNotNull(ps);

		IFindPublicationsStrategy prezentator = (IFindPublicationsStrategy) Class.forName(ps.getType()).newInstance();
		assertNotNull(prezentator);

		String authorId = "39700";
		int yearSince = 2009;
		int yearTill = 2010;
		HttpResponse result = httpService.execute(prezentator.getHttpRequest(authorId, yearSince, yearTill, ps));

		assertNotNull(result);

	}

	@Test
	public void findPublicationsOBDIntegrationTest() throws Exception {
		System.out.println("PublicationSystemMapper.findPublicationsOBDIntegrationTest");

		PublicationSystemExample example = new PublicationSystemExample();
		String mu = "zcu";
		example.createCriteria().andLoginnamespaceEqualTo(mu);
		List<PublicationSystem> publicationSystems = publicationSystemMapper.selectByExample(example);

		PublicationSystem ps = publicationSystems.get(0);
		assertNotNull(ps);

		IFindPublicationsStrategy obd = (IFindPublicationsStrategy) Class.forName(ps.getType()).newInstance();
		assertNotNull(obd);


		String authorId = "Sitera,Jiří";
		int yearSince = 2006;
		int yearTill = 2009;
		HttpUriRequest request = obd.getHttpRequest(authorId, yearSince, yearTill, ps);
		HttpResponse response = httpService.execute(request);

		assertNotNull(response);

	}

}
