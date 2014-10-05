package cz.metacentrum.perun.dispatcher.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.TestBase;

public class TaskSchedulerTest extends TestBase {
	    private final static Logger log = LoggerFactory.getLogger(TaskSchedulerTest.class);

	    @Autowired
	    Destination destination1;		
	    	
	    @IfProfileValue(name="perun.test.groups", values=("xxx"))
	    @Test 
	    public void sendToEngineTest() {
		    StringBuilder destinations_s = new StringBuilder("");
		    destinations_s.append(destination1.serializeToString());
		    destinations_s.append("");
		    
		    log.debug("Destination list to parse: " + destinations_s.toString());
		    List<PerunBean> listOfBeans;
		    List<Destination> destinationList = new ArrayList<Destination>();
		    try {
			    listOfBeans = AuditParser.parseLog(destination1.serializeToString());
			    log.debug("Found list of destination beans: " + listOfBeans);
			    for(PerunBean bean : listOfBeans) {
				    destinationList.add((Destination)bean);
			    }
		    } catch (InternalErrorException e) {
			    log.error("Could not resolve destination from destination list");
		    }
	    }
}
