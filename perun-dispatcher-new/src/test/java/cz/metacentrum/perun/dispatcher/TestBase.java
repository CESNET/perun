package cz.metacentrum.perun.dispatcher;



import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { // imported "classpath:perun-beans.xml",
									// we need only component scan from there: "classpath:perun-controller-applicationcontext.xml",
									"classpath:perun-tasks-lib-applicationcontext.xml",
									"classpath:perun-dispatcher-applicationcontext-test.xml", 
									"classpath:perun-dispatcher-applicationcontext-jdbc-internal.xml",
									"classpath:perun-dispatcher-test-beans.xml"
									})
@ActiveProfiles("test")
public abstract class TestBase {
	
}
