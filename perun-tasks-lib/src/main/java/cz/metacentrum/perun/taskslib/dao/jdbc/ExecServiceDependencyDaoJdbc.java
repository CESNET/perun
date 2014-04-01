package cz.metacentrum.perun.taskslib.dao.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.taskslib.dao.ExecServiceDependencyDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

/**
 * @author Michal Karm Babacek
 */
@Transactional
public class ExecServiceDependencyDaoJdbc extends JdbcDaoSupport implements ExecServiceDependencyDao {
	private final static Logger log = LoggerFactory.getLogger(ExecServiceDependencyDaoJdbc.class);

	@Override
	public void createDependency(int dependantExecServiceId, int execServiceId) {
		this.getJdbcTemplate().update("insert into service_dependencies(dependency_id, exec_service_id) values (?,?)", execServiceId, dependantExecServiceId);
	}

	@Override
	public void removeDependency(int dependantExecServiceId, int execServiceId) {
		this.getJdbcTemplate().update("delete from service_dependencies where dependency_id = ? and exec_service_id = ?", new Object[] { execServiceId, dependantExecServiceId });
	}

	@Override
	public boolean isThereDependency(int dependantExecServiceId, int execServiceId) {
		int dependency = 0;
		dependency = this.getJdbcTemplate().queryForInt("select count(*) from service_dependencies where dependency_id = ? and exec_service_id = ?",
				new Object[] { execServiceId, dependantExecServiceId });
		if (dependency > 0) {
			return true;
		}
		return false;
	}

	@Override
	public List<ExecService> listExecServicesDependingOn(int execServiceId) {
		List<ExecService> execServices = getJdbcTemplate()
			.query("" +
					"select " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery +
					" from exec_services left join service_dependencies on exec_services.id = service_dependencies.exec_service_id left join services on " +
					" services.id=exec_services.service_id where service_dependencies.dependency_id = ?",
					new Integer[] { execServiceId }, ExecServiceDaoJdbc.EXEC_SERVICE_ROWMAPPER);
		if (execServices != null) {
			return execServices;
		} else {
			return new ArrayList<ExecService>();
		}
	}

	@Override
	public List<ExecService> listExecServicesThisExecServiceDependsOn(int dependantExecServiceId) {
		log.debug("Gonna listExecServicesThisExecServiceDependsOn...");
		List<ExecService> execServices = getJdbcTemplate().query(
				"select " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery +
				" from exec_services left join service_dependencies on service_dependencies.dependency_id = exec_services.id left join services on " +
				" services.id=exec_services.service_id where service_dependencies.exec_service_id = ?",
				new Integer[] { dependantExecServiceId }, ExecServiceDaoJdbc.EXEC_SERVICE_ROWMAPPER);
		log.debug("For dependant service: "+dependantExecServiceId+", dependencies:"+execServices);
		if (execServices != null) {
			return execServices;
		} else {
			return new ArrayList<ExecService>();
		}
	}

	@Override
	public List<ExecService> listExecServicesThisExecServiceDependsOn(int dependantExecServiceId, ExecServiceType execServiceType) {
		log.debug("Gonna listExecServicesThisExecServiceDependsOn...");
		List<ExecService> execServices = getJdbcTemplate()
			.query("" +
					"select " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery +
					" from exec_services left join service_dependencies on service_dependencies.dependency_id = exec_services.id left join services on " +
					" services.id=exec_services.service_id where service_dependencies.exec_service_id = ? and exec_services.type = ?",
					new Object[] { dependantExecServiceId, execServiceType.toString() }, ExecServiceDaoJdbc.EXEC_SERVICE_ROWMAPPER);
		log.debug("For dependant service: "+dependantExecServiceId+", dependencies:"+execServices);
		if (execServices != null) {
			return execServices;
		} else {
			return new ArrayList<ExecService>();
		}
	}
}
