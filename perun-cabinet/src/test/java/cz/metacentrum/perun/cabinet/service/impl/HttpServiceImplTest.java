package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import cz.metacentrum.perun.cabinet.service.IHttpService;

public class HttpServiceImplTest {

	@Test
	public void testSimpleHttpClientServiceImpl() throws Exception {
		System.out.println("HttpServiceImpl.simpleHttpClientServiceImpl");

		IHttpService hs = new HttpServiceImpl();
		HttpGet get = new HttpGet("http://www.seznam.cz");
		HttpResponse response = hs.execute(get);
		assertNotNull(response);
		System.out.println(response);
		assertTrue(EntityUtils.toString(response.getEntity(),"utf-8").contains("Seznam"));
	}

}
