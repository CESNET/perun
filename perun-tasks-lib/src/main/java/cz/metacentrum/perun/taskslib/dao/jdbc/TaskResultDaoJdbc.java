package cz.metacentrum.perun.taskslib.dao.jdbc;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskResult.TaskResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Transactional
public class TaskResultDaoJdbc extends JdbcDaoSupport implements TaskResultDao {
	private static final Logger log = LoggerFactory.getLogger(TaskResultDaoJdbc.class);
	private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

	public final static String taskResultMappingSelectQuery = " tasks_results.id as tasks_results_id, tasks_results.task_id as tasks_results_task_id," +
		" tasks_results.destination_id as tasks_results_destination_id, tasks_results.status as tasks_results_status, tasks_results.err_message as tasks_results_err_message," +
		" tasks_results.std_message as tasks_results_std_message, tasks_results.return_code as tasks_results_return_code, tasks_results.timestamp as tasks_results_timestamp ";

	public static final RowMapper<TaskResult> TASKRESULT_ROWMAPPER = new RowMapper<TaskResult>() {

		public TaskResult mapRow(ResultSet rs, int i) throws SQLException {

			TaskResult taskResult = new TaskResult();

			taskResult.setId(rs.getInt("tasks_results_id"));
			taskResult.setDestinationId(rs.getInt("tasks_results_destination_id"));
			taskResult.setErrorMessage(rs.getString("tasks_results_err_message"));
			taskResult.setTaskId(rs.getInt("tasks_results_task_id"));
			taskResult.setReturnCode(rs.getInt("tasks_results_return_code"));
			taskResult.setStandardMessage(rs.getString("tasks_results_std_message"));

			if (rs.getTimestamp("tasks_results_timestamp") != null) {
				taskResult.setTimestamp(rs.getTimestamp("tasks_results_timestamp"));
			}

			if (rs.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.DONE.toString())) {
				taskResult.setStatus(TaskResultStatus.DONE);
			} else if (rs.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.ERROR.toString())) {
				taskResult.setStatus(TaskResultStatus.ERROR);
			} else if (rs.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.FATAL_ERROR.toString())) {
				taskResult.setStatus(TaskResultStatus.FATAL_ERROR);
			} else if (rs.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.DENIED.toString())) {
				taskResult.setStatus(TaskResultStatus.DENIED);
			} else {
				throw new IllegalArgumentException("Unknown TaskResult state.");
			}

			taskResult.setDestination(ServicesManagerImpl.DESTINATION_MAPPER.mapRow(rs, i));
			taskResult.setService(ServicesManagerImpl.SERVICE_MAPPER.mapRow(rs, i));

			return taskResult;
		}

	};

	public synchronized NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		if (this.namedParameterJdbcTemplate == null) {
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.getDataSource());
		}
		return this.namedParameterJdbcTemplate;
	}

	@Override
	public int insertNewTaskResult(TaskResult taskResult, int engineID) throws InternalErrorException {
		int newTaskResultId = Utils.getNewId(this.getJdbcTemplate(), "tasks_results_id_seq");

		// There was probably an issue with too long a String for VARCHAR2 datatype http://goo.gl/caVxp.
		// Solution might be to shorten the message according to VARCHAR2: http://goo.gl/WrlYm
		String standardMessage = null;
		String errorMessage = null;
		if(taskResult.getStandardMessage() != null) standardMessage = taskResult.getStandardMessage().length() < 4000 ? taskResult.getStandardMessage() : taskResult.getStandardMessage().substring(0, 3998);
		if(taskResult.getErrorMessage()    != null) errorMessage    = taskResult.getErrorMessage().length() < 4000 ? taskResult.getErrorMessage() : taskResult.getErrorMessage().substring(0, 3998);

		this.getJdbcTemplate()
			.update(
					"insert into tasks_results(" +
					"id, " +
					"task_id, " +
					"destination_id, " +
					"status, " +
					"err_message, " +
					"std_message, " +
					"return_code, " +
					"timestamp, " +
					"engine_id) values (?,?,?,?,?,?,?, " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " ,?)",
					newTaskResultId,
					taskResult.getTaskId(),
					taskResult.getDestinationId(),
					taskResult.getStatus().toString(),
					errorMessage,
					standardMessage,
					taskResult.getReturnCode(),
					TaskDaoJdbc.getDateFormatter().format(taskResult.getTimestamp()),
					engineID);
		return newTaskResultId;
	}

	@Override
	public List<TaskResult> getTaskResults(int engineID) {
		List<TaskResult> taskResults = this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id left join exec_services on exec_services.id = tasks.exec_service_id" +
				" left join services on services.id = exec_services.service_id where tasks_results.engine_id = ?", new Integer[] { engineID },
				TASKRESULT_ROWMAPPER);
		if (taskResults != null) {
			return taskResults;
		} else {
			return new ArrayList<TaskResult>();
		}
	}

	@Override
	public List<TaskResult> getTaskResults() {
		List<TaskResult> taskResults = this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join exec_services on exec_services.id = tasks.exec_service_id" +
				" left join services on services.id = exec_services.service_id ",
				TASKRESULT_ROWMAPPER);
		if (taskResults != null) {
			return taskResults;
		} else {
			return new ArrayList<TaskResult>();
		}
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId, int engineID) {
		return this.getJdbcTemplate().queryForObject(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join exec_services on exec_services.id = tasks.exec_service_id" +
				" left join services on services.id = exec_services.service_id " +
				"where tasks_results.id = ? and tasks_results.engine_id = ?",
				new Object[] { taskResultId, engineID }, TASKRESULT_ROWMAPPER);
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId) {
		return this.getJdbcTemplate().queryForObject("select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join exec_services on exec_services.id = tasks.exec_service_id" +
				" left join services on services.id = exec_services.service_id where tasks_results.id = ?",
				new Object[] { taskResultId }, TASKRESULT_ROWMAPPER);
	}

	@Override
	public int clearByTask(int taskId, int engineID) {
		return this.getJdbcTemplate().update("delete from tasks_results where task_id = ? and engine_id = ?", new Object[] { taskId, engineID });
	}

	@Override
	public int clearByTask(int taskId) {
		return this.getJdbcTemplate().update("delete from tasks_results where task_id = ?", new Object[] { taskId });
	}

	@Override
	public int clearAll(int engineID) {
		return this.getJdbcTemplate().update("delete from tasks_results where engine_id = ?", engineID);
	}

	@Override
	public int clearAll() {
		return this.getJdbcTemplate().update("delete from tasks_results");
	}

	@Override
	public int clearOld(int engineID, int numDays) throws InternalErrorException {

		// create sql toDate() with numDay substracted from now
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_MONTH, -numDays);
		String compareDate = TaskDaoJdbc.getDateFormatter().format(date.getTime());

		return this.getJdbcTemplate().update("delete from tasks_results where engine_id = ? and " +
				"id in (" +
				"select otr.id from tasks_results otr " +
				"         left join ( " +
				"	select tr.destination_id, tr.task_id, max(tr.timestamp) as maxtimestamp " +
				"	from tasks_results tr " +
				"		inner join tasks t on tr.task_id = t.id " +
				"		group by tr.destination_id,tr.task_id " +
				"   )  tmp on otr.task_id = tmp.task_id and otr.destination_id = tmp.destination_id " +
				"where otr.timestamp < maxtimestamp and otr.timestamp < "+Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'")+" )",
				engineID, compareDate);
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId, int engineID) {
		List<TaskResult> taskResults = this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join exec_services on exec_services.id = tasks.exec_service_id" +
				" left join services on services.id = exec_services.service_id " +
				" where tasks_results.task_id = ? and tasks_results.engine_id = ?",
				new Integer[] { taskId, engineID }, TASKRESULT_ROWMAPPER);
		if (taskResults != null) {
			return taskResults;
		} else {
			return new ArrayList<TaskResult>();
		}
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId) {
		List<TaskResult> taskResults = this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join exec_services on exec_services.id = tasks.exec_service_id" +
				" left join services on services.id = exec_services.service_id where tasks_results.task_id = ?", new Integer[] { taskId }, TASKRESULT_ROWMAPPER);
		if (taskResults != null) {
			return taskResults;
		} else {
			return new ArrayList<TaskResult>();
		}
	}

	public List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames) throws InternalErrorException {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("destinations", destinationsNames);

		try {
			return getNamedParameterJdbcTemplate().query("select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
					ServicesManagerImpl.serviceMappingSelectQuery +
					" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
					" left join tasks on tasks.id = tasks_results.task_id " +
					" left join exec_services on exec_services.id = tasks.exec_service_id" +
					" left join services on services.id = exec_services.service_id where destinations.destination in ( :destinations )", parameters, TASKRESULT_ROWMAPPER);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

}
