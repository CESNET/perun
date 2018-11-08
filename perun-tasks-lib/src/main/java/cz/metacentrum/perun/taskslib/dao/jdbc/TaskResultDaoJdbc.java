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

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
		byte[] standardMessage = null;
		byte[] errorMessage = null;
		if(taskResult.getStandardMessage() != null) standardMessage = taskResult.getStandardMessage().getBytes(StandardCharsets.UTF_8);
		if(taskResult.getErrorMessage()    != null) errorMessage    = taskResult.getErrorMessage().getBytes(StandardCharsets.UTF_8);

		// CLEAR UTF-8 0x00 bytes, since PostgreSQL can't store them to varchar column (Oracle can).
		// By java, such byte is displayed as 'empty string' and is not visible in a log.
		standardMessage = clearZeroBytesFromString(standardMessage, 4000);
		errorMessage = clearZeroBytesFromString(errorMessage, 4000);

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
					new String(errorMessage, StandardCharsets.UTF_8),
					new String(standardMessage, StandardCharsets.UTF_8),
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
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.engine_id = ?",
				TASKRESULT_ROWMAPPER,
				engineID);
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
				" left join services on services.id = tasks.service_id",
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
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.id = ? and tasks_results.engine_id = ?",
				TASKRESULT_ROWMAPPER, taskResultId, engineID);
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId) {
		return this.getJdbcTemplate().queryForObject("select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.id = ?",
				TASKRESULT_ROWMAPPER, taskResultId);
	}

	@Override
	public int clearByTask(int taskId, int engineID) {
		return this.getJdbcTemplate().update("delete from tasks_results where task_id = ? and engine_id = ?", taskId, engineID);
	}

	@Override
	public int clearByTask(int taskId) {
		return this.getJdbcTemplate().update("delete from tasks_results where task_id = ?", taskId);
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
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.task_id = ? and tasks_results.engine_id = ?",
				TASKRESULT_ROWMAPPER, taskId, engineID);
		if (taskResults != null) {
			return taskResults;
		} else {
			return new ArrayList<TaskResult>();
		}
	}



	@Override
	public List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId) {
		return this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results" +
				" left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join services on services.id = tasks.service_id" +
				" inner join (" +
				" SELECT destination_id, MAX(modified_at) AS modified_at_max" +
				" FROM tasks_results where task_id=?" +
				" GROUP BY destination_id) tr2" +
				" on tasks_results.destination_id = tr2.destination_id" +
				" and tasks_results.modified_at = tr2.modified_at_max" +
				" inner join (" +
				" SELECT destination_id, modified_at, MAX(id) AS id_max" +
				" FROM tasks_results where task_id=?" +
				" GROUP BY destination_id, modified_at) tr3" +
				" on tasks_results.destination_id = tr3.destination_id" +
				" and tasks_results.modified_at = tr3.modified_at" +
				" and tasks_results.id   = tr3.id_max" +
				" where tasks_results.task_id=?",
				TASKRESULT_ROWMAPPER, taskId, taskId, taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId) {
		List<TaskResult> taskResults = this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.task_id = ?",
				TASKRESULT_ROWMAPPER, taskId);
		if (taskResults != null) {
			return taskResults;
		} else {
			return new ArrayList<TaskResult>();
		}
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskAndDestination(int taskId, int destinationId) {
		List<TaskResult> taskResults = this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
						ServicesManagerImpl.serviceMappingSelectQuery +
						" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
						" left join tasks on tasks.id = tasks_results.task_id " +
						" left join services on services.id = tasks.service_id" +
						" where tasks_results.task_id = ? AND" +
						" tasks_results.destination_id=?",
				TASKRESULT_ROWMAPPER, taskId, destinationId);
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
					" left join services on services.id = tasks.service_id" +
					" where destinations.destination in ( :destinations )", parameters, TASKRESULT_ROWMAPPER);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Clear all Zero bytes (0x00) from UTF-8 String
	 *
	 * @param data to remove zero bytes
	 * @return Original string without zero bytes
	 */
	private static byte[] clearZeroBytesFromString(byte[] data, int maxLength) {
		if (data == null) return null;
		ByteArrayOutputStream dataOut = new ByteArrayOutputStream() ;
		int limit = (maxLength < data.length) ? maxLength - 4 : data.length;
		for (int i = 0; i < limit; i++) {
			if (data[i] != 0x00)
				dataOut.write(data[i]);
		}
		if(maxLength < data.length) {
			// we had to cut the byte array at limit
			// data[limit-1] is the last added byte
			// we have to check, if it starts the non-ASCII char sequence
			if(data[limit-1] >= 0xC0 ) {
				dataOut.write(data[limit]);
			}
			if(data[limit-1] >= 0xE0 ) {
				dataOut.write(data[limit+1]);
			}
			if(data[limit-1] >= 0xF0 ) {
				dataOut.write(data[limit+2]);
			}
		}
		return dataOut.toByteArray();
	}

}
