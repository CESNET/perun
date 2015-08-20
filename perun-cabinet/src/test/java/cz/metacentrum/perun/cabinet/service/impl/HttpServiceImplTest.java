package cz.metacentrum.perun.cabinet.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cz.metacentrum.perun.cabinet.service.IHttpService;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpServiceImplTest {

	@Test
	public void testSimpleHttpClientServiceImpl() throws Exception {
		System.out.println("HttpServiceImpl.simpleHttpClientServiceImpl");

		IHttpService hs = new HttpServiceImpl();
		GetMethod get = new GetMethod("http://www.seznam.cz");
		String response = hs.execute(get, null);
		assertNotNull(response);
		System.out.println(response);
		assertTrue(response.contains("Seznam"));
	}

}
