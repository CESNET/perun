package cz.metacentrum.perun.engine.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.dao.EngineDao;
import cz.metacentrum.perun.engine.exceptions.DispatcherNotConfiguredException;
import cz.metacentrum.perun.engine.exceptions.EngineNotConfiguredException;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.rpclib.api.RpcCaller;
import cz.metacentrum.perun.rpclib.impl.RpcCallerImpl;
import cz.metacentrum.perun.taskslib.model.ExecService;

import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "engineManager")
public class EngineManagerImpl implements EngineManager {

	private final static Logger log = LoggerFactory.getLogger(EngineManagerImpl.class);

	@Autowired
	private EngineDao engineDao;
	@Autowired
	private JMSQueueManager jmsQueueManager;
	@Autowired
	private Properties propertiesBean;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskManager taskManager;
	private RpcCaller rpcCaller;

	@Override
	public void registerEngine() throws EngineNotConfiguredException, DispatcherNotConfiguredException {
		engineDao.registerEngine();
		engineDao.loadDispatcherAddress();
	}

	@Override
	public void checkIn() {
		engineDao.checkIn();
	}

	@Override
	public void startMessaging() {
		jmsQueueManager.initiateConnection();
		jmsQueueManager.registerForReceivingMessages();
	}

	@Override
	public RpcCaller getRpcCaller() {
		if (this.rpcCaller != null) {
			return rpcCaller;
		}
		try {
			String perunPrincipal = propertiesBean.getProperty("perun.principal.name");
			String extSourceName = propertiesBean.getProperty("perun.principal.extSourceName");
			String extSourceType = propertiesBean.getProperty("perun.principal.extSourceType");
			PerunPrincipal pp = new PerunPrincipal(perunPrincipal, extSourceName, extSourceType);
			this.rpcCaller = new RpcCallerImpl(pp);
		} catch (InternalErrorException e) {
			log.error(e.toString());
		}
		return this.rpcCaller;
	}

	public void setEngineDao(EngineDao engineDao) {
		this.engineDao = engineDao;
	}

	public EngineDao getEngineDao() {
		return engineDao;
	}

	public void setJmsQueueManager(JMSQueueManager jmsQueueManager) {
		this.jmsQueueManager = jmsQueueManager;
	}

	public JMSQueueManager getJmsQueueManager() {
		return jmsQueueManager;
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
	}

	public Properties getPropertiesBean() {
		return propertiesBean;
	}

	@Override
	public void loadSchedulingPool() {
		log.info("I am going to load ExecService:Facility pairs from SchedulingPool.txt");
		try {
			BufferedReader input = new BufferedReader(new FileReader("SchedulingPool.txt"));
			String line = null;
			while ((line = input.readLine()) != null) {
				//timestamp execserviceID facilityID
				String[] data = line.split(" ");
				ExecService execService = Rpc.GeneralServiceManager.getExecService(getRpcCaller(), Integer.parseInt(data[1]));
				Facility facility = Rpc.FacilitiesManager.getFacilityById(getRpcCaller(), Integer.parseInt(data[2]));
				if(facility.getName().equals("alcor.ics.muni.cz") ||
						facility.getName().equals("aldor.ics.muni.cz") ||
						facility.getName().equals("torque.ics.muni.cz") ||
						facility.getName().equals("nympha-cloud.zcu.cz") ||
						facility.getName().equals("ascor.ics.muni.cz")) {
					log.info("IGNORE facility " + facility.getName());
					continue;
				}	
				schedulingPool.addToPool(new Pair<ExecService, Facility>(execService, facility));
			}
		} catch (IOException e) {
			log.error(e.toString(), "loadSchedulingPool from file has failed. You might have lost some ExecService:facility pairs.");
		} catch (FacilityNotExistsException e) {
			log.error(e.toString());
		} catch (NumberFormatException e) {
			log.error(e.toString());
		} catch (InternalErrorException e) {
			log.error(e.toString());
		} catch (PrivilegeException e) {
			log.error(e.toString());
		} catch (ServiceNotExistsException e) {
			log.error(e.toString());
		}
		log.info("Loading ExecService:Facility pairs from SchedulingPool.txt has completed.");
	}

	@Override
	public void switchUnfinishedTasksToERROR() {
		log.info("I am going to switched all unfinished tasks to ERROR and finished GEN tasks which data wasn't send to NONE");
		for (Task task : taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id")))) {
			if(task.getStatus().equals(TaskStatus.DONE)) {
				ExecService execService = task.getExecService();

				if(execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
					task.setStatus(TaskStatus.NONE);
					task.setEndTime(new Date(System.currentTimeMillis()));
					taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
				}
			} else {
				if (!task.getStatus().equals(TaskStatus.ERROR) && !task.getStatus().equals(TaskStatus.NONE)) {
					task.setStatus(TaskStatus.ERROR);
					task.setEndTime(new Date(System.currentTimeMillis()));
					taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
				}
			}
		}
		log.info("I'm done with it.");
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

}
