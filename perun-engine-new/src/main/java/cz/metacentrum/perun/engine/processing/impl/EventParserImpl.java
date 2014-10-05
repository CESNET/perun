package cz.metacentrum.perun.engine.processing.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.engine.processing.EventParser;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "eventParser")
public class EventParserImpl implements EventParser {
    private static final Logger log = LoggerFactory.getLogger(EventParserImpl.class);

    @Autowired
    private DependenciesResolver dependenciesResolver;
    @Autowired
    private ExecServiceDao execServiceDao;
    @Autowired
    private Properties propertiesBean;

    @Override
    public Task parseEvent(String event) throws InvalidEventMessageException, ServiceNotExistsException, InternalErrorException, PrivilegeException {

    	log.info("I am going to process event:" + event);

        /**
         * Expected string format as on: https://projekty.ics.muni.cz/perunv3/trac/wiki/PerunEngineDispatcherController event|x|[timestamp][Event header][Event data]
		 * New format: "task|[engine_id]|[task_id][exec_service_id][facility]|[destination_list]|[dependency_list]"
		 * 
         */
        //String eventParsingPattern = "^event\\|([0-9]{1,6})\\|\\[([a-zA-Z0-9: ]+)\\]\\[([^\\]]+)\\]\\[(.*)\\]$";
        String eventParsingPattern = "^task\\|([0-9]{1,6})\\|\\[([0-9]+)\\]\\[([0-9]+)\\]\\[([^\\|]+)\\]\\|\\[([^\\|]+)\\]|\\[(.*)\\]$";
        Pattern pattern = Pattern.compile(eventParsingPattern);
        Matcher matcher = pattern.matcher(event);
        boolean matchFound = matcher.find();

        if (matchFound) {
            log.debug("Message format matched ok...");
            String thisEngineID = matcher.group(1);
            // This should indeed match the current Engine instance ID, so let's compare it...
            if (Integer.parseInt(thisEngineID) != Integer.parseInt((String) propertiesBean.get("engine.unique.id"))) {
                throw new InvalidEventMessageException("Wrong Engine ID. Was:" + thisEngineID + ", Expected:" + propertiesBean.get("engine.unique.id"));
            }
            // Data should provide information regarding the target ExecService (Processing rule).
            String eventTaskId = matcher.group(2);
            String eventExecService = matcher.group(3);
            String eventFacility = matcher.group(4);
            String eventDestinationList = matcher.group(5);
            String eventDependencyList = matcher.group(6);
            
            log.debug("Event data to be parsed: task id " + eventTaskId 
            			+ ", facility " + eventFacility + ", exec service " + eventExecService
            			+ ", destination list " + eventDestinationList
            			+ ", dependency list " + eventDependencyList 
            			);
            
            //Prepare variables
            Facility facility;
            ExecService execService;
            List<Destination> destinationList = new ArrayList<Destination>();
            
            // resolve facility
            // deserialize event data
            List<PerunBean> listOfBeans = AuditParser.parseLog(eventFacility);
            try {
            	facility = (Facility)listOfBeans.get(0);
            } catch (Exception e) {
            	throw new InvalidEventMessageException("Could not resolve facility from event [" + eventFacility + "]", e);
            }
            
            // fetch execService from DB by id
            try {
            	int execServiceId = Integer.parseInt(eventExecService);
            	execService = execServiceDao.getExecService(execServiceId);
            } catch (Exception e) {
            	throw new InvalidEventMessageException("Could not resolve execService from event [" + eventExecService + "]", e);
            }
            if(execService == null) {
            	throw new InvalidEventMessageException("Could not resolve execService from event [" + eventExecService + "]");
            }

            // resolve list of destinations 
            listOfBeans = AuditParser.parseLog(eventDestinationList);
            log.debug("Found list of destination beans: " + listOfBeans);
            //return new Pair<ExecService, Facility>(execService, facility);
            try {
            	for(PerunBean bean : listOfBeans) {
            		destinationList.add((Destination)bean);
            	}
            } catch (Exception e) {
            	throw new InvalidEventMessageException("Could not resolve list of destinations from event.", e);
            }
            
            Task task = new Task();
            task.setId(Integer.parseInt(eventTaskId));
            task.setFacility(facility);
            task.setExecService(execService);
            task.setDestinations(destinationList);
            task.setDelay(execService.getDefaultDelay());
            task.setRecurrence(execService.getDefaultRecurrence());

            // resolve list of dependencies
            if(eventDependencyList != null) {
            	for(String token : eventDependencyList.split("[\t ]*,[\t ]*")) {
            		dependenciesResolver.addDependency(task, Integer.parseInt(token));
            	}
            }
            
            return task;

        } else {
            throw new InvalidEventMessageException("Invalid message format: Message[" + event + "]");
        }
    }


    public Properties getPropertiesBean() {
        return propertiesBean;
    }

    public void setPropertiesBean(Properties propertiesBean) {
        this.propertiesBean = propertiesBean;
    }

	public ExecServiceDao getExecServiceDao() {
		return execServiceDao;
	}

	public void setExecServiceDao(ExecServiceDao execServiceDao) {
		this.execServiceDao = execServiceDao;
	}
}
