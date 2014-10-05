package cz.metacentrum.perun.engine;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-engine-applicationcontext-jdbc-internal.xml",
									"classpath:perun-engine-applicationcontext.xml", 
									"classpath:perun-engine-test-beans.xml"
									})
public abstract class TestBase {

}
