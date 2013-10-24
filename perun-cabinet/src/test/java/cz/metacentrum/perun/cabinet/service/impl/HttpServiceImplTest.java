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
		System.out.println("simpleHttpClientServiceImpl()");
		
		IHttpService hs = new HttpServiceImpl();
//		hs.setUri("https://is.muni.cz/auth/prezentator/index.pl");
		//hs.setLogin("358470","apebze.chu"); //systemovy ucet :)
		//hs.setLogin("231927","zun-poshli"); //neosobni ucet na jmeno Jiri Harazim
		
//		Map<String,String> params = new HashMap<String,String>();
//		params.put("keyfile", TestTemplates.muRequest);
//		params.put("typ", "xml");
//		params.put("kodovani", "utf-8");
//		
//		String result = hs.execute(params);
//
		HttpGet get = new HttpGet("http://www.seznam.cz");
		HttpResponse response = hs.execute(get);
		assertNotNull(response);
		System.out.println(response);
		assertTrue(EntityUtils.toString(response.getEntity(),"utf-8").contains("Seznam"));
	}
	
}