package cz.metacentrum.perun.dispatcher.unit;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.IfProfileValue;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 *
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskSchedulerTest extends AbstractDispatcherTest {

	private final static Logger log = LoggerFactory.getLogger(TaskSchedulerTest.class);

	Destination destination1 = new Destination(1, "par_dest1", "host", "PARALLEL");

	@IfProfileValue(name = "perun.test.groups", values = ("xxx"))
	@Test
	public void sendToEngineTest() {
		System.out.println("TaskScheduler.sendToEngine()");

		StringBuilder destinations_s = new StringBuilder("");
		destinations_s.append(destination1.serializeToString());
		destinations_s.append("");

		log.debug("Destination list to parse: " + destinations_s.toString());
		List<PerunBean> listOfBeans;
		List<Destination> destinationList = new ArrayList<Destination>();
		try {
			listOfBeans = AuditParser.parseLog(destination1.serializeToString());
			log.debug("Found list of destination beans: " + listOfBeans);
			for (PerunBean bean : listOfBeans) {
				destinationList.add((Destination) bean);
			}
		} catch (InternalErrorException e) {
			log.error("Could not resolve destination from destination list");
		}
	}

}
