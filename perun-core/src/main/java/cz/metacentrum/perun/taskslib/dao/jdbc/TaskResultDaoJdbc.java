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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class TaskResultDaoJdbc extends JdbcDaoSupport implements TaskResultDao {

	private static final Logger log = LoggerFactory.getLogger(TaskResultDaoJdbc.class);
	private static final int MAX_NUMBER_OF_UTF8_BYTES = 4;
	private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

	public final static String taskResultMappingSelectQuery = " tasks_results.id as tasks_results_id, tasks_results.task_id as tasks_results_task_id," +
		" tasks_results.destination_id as tasks_results_destination_id, tasks_results.status as tasks_results_status, tasks_results.err_message as tasks_results_err_message," +
		" tasks_results.std_message as tasks_results_std_message, tasks_results.return_code as tasks_results_return_code, tasks_results.timestamp as tasks_results_timestamp ";

	public static final RowMapper<TaskResult> TASKRESULT_ROWMAPPER = (resultSet, i) -> {

		TaskResult taskResult = new TaskResult();

		taskResult.setId(resultSet.getInt("tasks_results_id"));
		taskResult.setDestinationId(resultSet.getInt("tasks_results_destination_id"));
		taskResult.setErrorMessage(resultSet.getString("tasks_results_err_message"));
		taskResult.setTaskId(resultSet.getInt("tasks_results_task_id"));
		taskResult.setReturnCode(resultSet.getInt("tasks_results_return_code"));
		taskResult.setStandardMessage(resultSet.getString("tasks_results_std_message"));

		if (resultSet.getTimestamp("tasks_results_timestamp") != null) {
			taskResult.setTimestamp(resultSet.getTimestamp("tasks_results_timestamp"));
		}

		if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.DONE.toString())) {
			taskResult.setStatus(TaskResultStatus.DONE);
		} else if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.ERROR.toString())) {
			taskResult.setStatus(TaskResultStatus.ERROR);
		} else if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.FATAL_ERROR.toString())) {
			taskResult.setStatus(TaskResultStatus.FATAL_ERROR);
		} else if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResultStatus.DENIED.toString())) {
			taskResult.setStatus(TaskResultStatus.DENIED);
		} else {
			throw new IllegalArgumentException("Unknown TaskResult state.");
		}

		taskResult.setDestination(ServicesManagerImpl.DESTINATION_MAPPER.mapRow(resultSet, i));
		taskResult.setService(ServicesManagerImpl.SERVICE_MAPPER.mapRow(resultSet, i));

		return taskResult;
	};

	public synchronized NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		if (this.namedParameterJdbcTemplate == null && this.getDataSource() != null) {
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.getDataSource());
		}
		return this.namedParameterJdbcTemplate;
	}

	@SuppressWarnings("ConstantConditions")
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

		// jdbc template cannot be null
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
					errorMessage == null ? null : new String(errorMessage, StandardCharsets.UTF_8),
					standardMessage == null ? null : new String(standardMessage, StandardCharsets.UTF_8),
					taskResult.getReturnCode(),
					TaskDaoJdbc.getDateFormatter().format(taskResult.getTimestamp()),
					engineID);
		return newTaskResultId;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<TaskResult> getTaskResults(int engineID) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.engine_id = ?",
				TASKRESULT_ROWMAPPER,
				engineID);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<TaskResult> getTaskResults() {
		// jdbc template cannot be null
		return this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join services on services.id = tasks.service_id",
				TASKRESULT_ROWMAPPER);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public TaskResult getTaskResultById(int taskResultId, int engineID) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().queryForObject(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.id = ? and tasks_results.engine_id = ?",
				TASKRESULT_ROWMAPPER, taskResultId, engineID);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public TaskResult getTaskResultById(int taskResultId) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().queryForObject("select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.id = ?",
				TASKRESULT_ROWMAPPER, taskResultId);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public int clearByTask(int taskId, int engineID) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().update("delete from tasks_results where task_id = ? and engine_id = ?", taskId, engineID);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public int clearByTask(int taskId) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().update("delete from tasks_results where task_id = ?", taskId);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public int clearAll(int engineID) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().update("delete from tasks_results where engine_id = ?", engineID);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public int clearAll() {
		// jdbc template cannot be null
		return this.getJdbcTemplate().update("delete from tasks_results");
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public int clearOld(int engineID, int numDays) {

		String compareDate = LocalDateTime.now().minusDays(numDays).format(TaskDaoJdbc.getDateTimeFormatter());

		// jdbc template cannot be null
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

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId, int engineID) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.task_id = ? and tasks_results.engine_id = ?",
				TASKRESULT_ROWMAPPER, taskId, engineID);
	}


	@SuppressWarnings("ConstantConditions")
	@Override
	public List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId) {
		// jdbc template cannot be null
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

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.task_id = ?",
				TASKRESULT_ROWMAPPER, taskId);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<TaskResult> getTaskResultsByTaskAndDestination(int taskId, int destinationId) {
		// jdbc template cannot be null
		return this.getJdbcTemplate().query(
				"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
						ServicesManagerImpl.serviceMappingSelectQuery +
						" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
						" left join tasks on tasks.id = tasks_results.task_id " +
						" left join services on services.id = tasks.service_id" +
						" where tasks_results.task_id = ? AND" +
						" tasks_results.destination_id=?",
				TASKRESULT_ROWMAPPER, taskId, destinationId);
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
		boolean maxLengthExceeded = maxLength < data.length;
		int limit = maxLengthExceeded ? maxLength - MAX_NUMBER_OF_UTF8_BYTES : data.length;
		for (int i = 0; i < limit; i++) {
			if (data[i] != 0x00)
				dataOut.write(data[i]);
		}
		if(maxLengthExceeded) {
			// check if we have to add some bytes in case we have split an UTF-8 character that is longer than 1 byte.
			if (!isASingleByteUTF8Char(data[limit-1])) {
				int i = 0;
				while (!isAStartingByteUTF8Char(data[limit + i])) {
					if (i == MAX_NUMBER_OF_UTF8_BYTES) {
						log.error("The message data contains invalid UTF-8 character. The byte limit for one character was exceeded.");
						break;
					}
					dataOut.write(data[limit + i]);
					i++;
				}
			}

		}
		return dataOut.toByteArray();
	}

	/**
	 * Checks if the given byte represents an initial UTF-8 character.
	 *
	 * An initial character can be in those formats:
	 *     110XXXXXX
	 *     1110XXXXX
	 *     11110XXXX
	 *     0XXXXXXXX
	 *
	 * If the given value is in range 10000000(inclusive) ... 11000000(exclusive)
	 * it means that the byte is part of a UTF-8 character composed of multiple bytes.
	 *
	 * The value 10000000 for byte in Java is equal to -128 and this value is minimal.
	 * The value 11000000 for byte in Java is equal to -64.
	 *
	 * @param b byte to check
	 * @return true, if the given byte is a starting byte for UTF-8 char, false otherwise.
	 */
	private static boolean isAStartingByteUTF8Char(byte b) {
		return b >= (byte)0b11000000;
	}

	/**
	 * Check if this byte represents a UTF-8 character that is represented by one byte.
	 *
	 * That means, check if the value is in range 00000000 ... 01111111.
	 * If the given byte starts with '1' it means its lower than 0 because bytes in Java
	 * are represented with inversion code.
	 *
	 * @param b byte to check
	 * @return true, if the given byte represents a single byte UTF-8 character, false otherwise.
	 */
	private static boolean isASingleByteUTF8Char(byte b) {
		return b > 0;
	}
}
