package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.TasksManagerImplApi;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * TasksManagerImpl
 */
public class TasksManagerImpl implements TasksManagerImplApi {

	private static final Logger log = LoggerFactory.getLogger(TasksManagerImpl.class);

	private static final int MAX_NUMBER_OF_UTF8_BYTES = 4;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private JdbcPerunTemplate jdbc;

	/**
	 * Create new instance of this class.
	 * Used for the tests only
	 */
	public TasksManagerImpl() {
	}

	/**
	 * Create new instance of this class.
	 *
	 */
	public TasksManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

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

		if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResult.TaskResultStatus.DONE.toString())) {
			taskResult.setStatus(TaskResult.TaskResultStatus.DONE);
		} else if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResult.TaskResultStatus.ERROR.toString())) {
			taskResult.setStatus(TaskResult.TaskResultStatus.ERROR);
		} else if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResult.TaskResultStatus.FATAL_ERROR.toString())) {
			taskResult.setStatus(TaskResult.TaskResultStatus.FATAL_ERROR);
		} else if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResult.TaskResultStatus.DENIED.toString())) {
			taskResult.setStatus(TaskResult.TaskResultStatus.DENIED);
		} else if (resultSet.getString("tasks_results_status").equalsIgnoreCase(TaskResult.TaskResultStatus.WARNING.toString())) {
			taskResult.setStatus(TaskResult.TaskResultStatus.WARNING);
		} else {
			throw new IllegalArgumentException("Unknown TaskResult state.");
		}

		taskResult.setDestination(ServicesManagerImpl.DESTINATION_MAPPER.mapRow(resultSet, i));
		taskResult.setService(ServicesManagerImpl.SERVICE_MAPPER.mapRow(resultSet, i));

		return taskResult;
	};

	public synchronized NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		if (this.namedParameterJdbcTemplate == null && jdbc.getDataSource() != null) {
			this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc.getDataSource());
		}
		this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		return this.namedParameterJdbcTemplate;
	}

	@Override
	public int insertNewTaskResult(TaskResult taskResult) {
		int newTaskResultId = Utils.getNewId(jdbc, "tasks_results_id_seq");

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
		jdbc.update(
				"insert into tasks_results(" +
					"id, " +
					"task_id, " +
					"destination_id, " +
					"status, " +
					"err_message, " +
					"std_message, " +
					"return_code, " +
					"timestamp) values (?,?,?,?,?,?,?," + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ")",
				newTaskResultId,
				taskResult.getTaskId(),
				taskResult.getDestinationId(),
				taskResult.getStatus().toString(),
				errorMessage == null ? null : new String(errorMessage, StandardCharsets.UTF_8),
				standardMessage == null ? null : new String(standardMessage, StandardCharsets.UTF_8),
				taskResult.getReturnCode(),
				getDateFormatter().format(taskResult.getTimestamp()));
		return newTaskResultId;
	}

	@Override
	public List<TaskResult> getTaskResults() {
		// jdbc template cannot be null
		return jdbc.query(
			"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join services on services.id = tasks.service_id",
			TASKRESULT_ROWMAPPER);
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId) {
		// jdbc template cannot be null
		return jdbc.queryForObject("select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id " +
				" left join tasks on tasks.id = tasks_results.task_id" +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.id = ?",
			TASKRESULT_ROWMAPPER, taskResultId);
	}

	@Override
	public void deleteTaskResultById(int taskResultId) {
		try {
			jdbc.update("delete from tasks_results where id = ?", taskResultId);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public int deleteTaskResults(int taskId) {
		try {
			return jdbc.update("delete from tasks_results where task_id = ?", taskId);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public int deleteTaskResults(int taskId, int destinationId) {
		try {
			return jdbc.update("delete from tasks_results where task_id = ? and destination_id = ?", taskId, destinationId);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public int deleteOldTaskResults(int numDays) {
		try {
			String compareDate = LocalDateTime.now().minusDays(numDays).format(getDateTimeFormatter());

			return jdbc.update("delete from tasks_results where " +
							"id in (" +
							"select otr.id from tasks_results otr " +
							"         left join ( " +
							"	select tr.destination_id, tr.task_id, max(tr.timestamp) as maxtimestamp " +
							"	from tasks_results tr " +
							"		inner join tasks t on tr.task_id = t.id " +
							"		group by tr.destination_id,tr.task_id " +
							"   )  tmp on otr.task_id = tmp.task_id and otr.destination_id = tmp.destination_id " +
							"where otr.timestamp < maxtimestamp and otr.timestamp < "+Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'")+" )",
					compareDate);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public int deleteAllTaskResults() {
		try {
			return jdbc.update("delete from tasks_results");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId) {
		// jdbc template cannot be null
		return jdbc.query(
			"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.task_id = ? ",
			TASKRESULT_ROWMAPPER, taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId) {
		// jdbc template cannot be null
		return jdbc.query(
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
	public List<TaskResult> getTaskResultsByTaskAndDestination(int taskId, int destinationId) {
		// jdbc template cannot be null
		return jdbc.query(
			"select " + taskResultMappingSelectQuery + ", " + ServicesManagerImpl.destinationMappingSelectQuery + ", " +
				ServicesManagerImpl.serviceMappingSelectQuery +
				" from tasks_results left join destinations on tasks_results.destination_id = destinations.id" +
				" left join tasks on tasks.id = tasks_results.task_id " +
				" left join services on services.id = tasks.service_id" +
				" where tasks_results.task_id = ? AND" +
				" tasks_results.destination_id=?",
			TASKRESULT_ROWMAPPER, taskId, destinationId);
	}

	public List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames) {

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

	private JdbcTemplate getMyJdbcTemplate() {
		// jdbc template cannot be null
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		return jdbc;
	}

	/**
	 * Method create formatter with default settings for perun timestamps and set lenient on false
	 * Timestamp format:  "dd-MM-yyyy HH:mm:ss" - "ex. 01-01-2014 10:10:10"
	 *
	 * Lenient on false means that formatter will be more strict to creating timestamp from string
	 *
	 * IMPORTANT: SimpleDateFormat is not thread safe !!!
	 *
	 * @return date formatter
	 */
	public static SimpleDateFormat getDateFormatter() {
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		df.setLenient(false);
		return df;
	}

	/**
	 * Method create formatter with default settings for perun timestamps and set ResolverStyle to STRICT
	 * Timestamp format:  "dd-MM-yyyy HH:mm:ss" - "ex. 01-01-2014 10:10:10"
	 *
	 * ResolverStyle.STRICT means that formatter will be more strict to creating timestamp from string
	 *
	 * @return date formatter
	 */
	public static DateTimeFormatter getDateTimeFormatter() {
		return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
	}

	public final static String taskMappingSelectQuery = " tasks.id as tasks_id, tasks.schedule as tasks_schedule, tasks.recurrence as tasks_recurrence, " +
		"tasks.delay as tasks_delay, tasks.status as tasks_status, tasks.start_time as tasks_start_time, tasks.end_time as tasks_end_time ";

	public static final RowMapper<Task> TASK_ROWMAPPER = (resultSet, i) -> {

		Task task = new Task();

		task.setDelay(resultSet.getInt("tasks_delay"));
		task.setId(resultSet.getInt("tasks_id"));
		task.setRecurrence(resultSet.getInt("tasks_recurrence"));

		if (resultSet.getTimestamp("tasks_start_time") != null) {
			task.setStartTime(resultSet.getTimestamp("tasks_start_time").toLocalDateTime());
		}
		if (resultSet.getTimestamp("tasks_schedule") != null) {
			task.setSchedule(resultSet.getTimestamp("tasks_schedule").toLocalDateTime());
		}
		if (resultSet.getTimestamp("tasks_end_time") != null) {
			task.setEndTime(resultSet.getTimestamp("tasks_end_time").toLocalDateTime());
		}

		if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.WAITING.toString())) {
			task.setStatus(Task.TaskStatus.WAITING);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.PLANNED.toString())) {
			task.setStatus(Task.TaskStatus.PLANNED);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.SENDERROR.toString())) {
			task.setStatus(Task.TaskStatus.SENDERROR);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.GENERROR.toString())) {
			task.setStatus(Task.TaskStatus.GENERROR);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.GENERATING.toString())) {
			task.setStatus(Task.TaskStatus.GENERATING);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.GENERATED.toString())) {
			task.setStatus(Task.TaskStatus.GENERATED);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.SENDING.toString())) {
			task.setStatus(Task.TaskStatus.SENDING);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.DONE.toString())) {
			task.setStatus(Task.TaskStatus.DONE);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.ERROR.toString())) {
			task.setStatus(Task.TaskStatus.ERROR);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(Task.TaskStatus.WARNING.toString())) {
			task.setStatus(Task.TaskStatus.WARNING);
		} else {
			throw new IllegalArgumentException("Task status [" + resultSet.getString("tasks_status") + "] unknown");
		}

		task.setFacility(FacilitiesManagerImpl.FACILITY_MAPPER.mapRow(resultSet, i));

		task.setService(ServicesManagerImpl.SERVICE_MAPPER.mapRow(resultSet, i));

		return task;
	};

	@Override
	public int insertTask(Task task) {
		int newTaskId = 0;
		try {
			newTaskId = Utils.getNewId(getMyJdbcTemplate(), "tasks_id_seq");
			// jdbc template cannot be null
			getMyJdbcTemplate().update(
				"insert into tasks(id, service_id, facility_id, schedule, recurrence, delay, status) values (?,?,?, " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ",?,?,?)",
				newTaskId, task.getServiceId(), task.getFacilityId(), task.getSchedule().format(getDateTimeFormatter()), task.getRecurrence(), task.getDelay(), task.getStatus().toString());
			log.debug("Added task with ID {}", newTaskId);
			return newTaskId;
		} catch (DataIntegrityViolationException ex) {
			log.error("Data: id, service_id, facility_id, schedule, recurrence, delay, status is: " + newTaskId + ", " + task.getServiceId() + ", " + task.getFacilityId() + ", "
				+ task.getSchedule().format(getDateTimeFormatter()) + ", " + task.getRecurrence() + ", " + task.getDelay() + ", " + task.getStatus().toString() + ". Exception:" + ex.toString(), ex);
		} catch (Exception ex) {
			log.error("Failed to insert new Task.", ex);
		}
		return 0;
	}

	@Override
	public Task getTask(Service service, Facility facility) {
		return getTask(service.getId(), facility.getId());
	}

	@Override
	public Task getTask(int serviceId, int facilityId) {
		try {
			// jdbc template cannot be null
			return getMyJdbcTemplate().queryForObject(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
					", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join services on tasks.service_id = services.id and tasks.service_id=?" +
					"left join facilities on facilities.id = tasks.facility_id and tasks.facility_id = ?",
				TASK_ROWMAPPER, serviceId, facilityId);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public List<Task> listAllTasksForFacility(int facilityId) {
		try {
			// jdbc template cannot be null
			return getMyJdbcTemplate().query(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
					", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join services on tasks.service_id = services.id " +
					"left join facilities on facilities.id = tasks.facility_id where facilities.id = ?",
				TASK_ROWMAPPER, facilityId);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public Task getTaskById(int id) {
		try {
			// jdbc template cannot be null
			return getMyJdbcTemplate().queryForObject(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
					", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
					"left join facilities on facilities.id = tasks.facility_id where tasks.id = ?",
				TASK_ROWMAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public List<Task> listAllTasks() {
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
			", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
			"left join facilities on facilities.id = tasks.facility_id", TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listAllTasksInState(Task.TaskStatus state) {
		String textState = state.toString().toUpperCase();
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
				"left join facilities on facilities.id = tasks.facility_id where tasks.status = ?",
			TASK_ROWMAPPER, textState);
	}

	@Override
	public List<Task> listAllTasksNotInState(Task.TaskStatus state) {
		String textState = state.toString().toUpperCase();
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
				"left join facilities on facilities.id = tasks.facility_id where tasks.status != ? ",
			 new Object[] { textState }, TASK_ROWMAPPER);
	}

	@Override
	public void updateTask(Task task) {
		String scheduled = null;
		if (task.getSchedule() != null) {
			scheduled = task.getSchedule().format(getDateTimeFormatter());
		}
		String endTime = null;
		if (task.getEndTime() != null) {
			endTime = task.getEndTime().format(getDateTimeFormatter());
		}
		String startTime = null;
		if (task.getStartTime() != null) {
			startTime = task.getStartTime().format(getDateTimeFormatter());
		}

		// jdbc template cannot be null
		getMyJdbcTemplate().update(
			"update tasks set service_id = ?, facility_id = ?, schedule = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ", recurrence = ?, delay = ?, "
				+ "status = ?, start_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ", end_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " where id = ?", task.getServiceId(),
			task.getFacilityId(), scheduled, task.getRecurrence(), task.getDelay(), task.getStatus().toString(), startTime, endTime, task.getId());
	}

	@Override
	public boolean isThereSuchTask(Service service, Facility facility) {
		//this.getJdbcTemplate().update("select id from services where id = ? for update", service.getId());

		// jdbc template cannot be null
		List<Integer> tasks = getMyJdbcTemplate().queryForList("select id from tasks where service_id = ? and facility_id = ?",
			new Integer[] { service.getId(), facility.getId() }, Integer.class);
		if (tasks.size() == 0) {
			return false;
		} else if (tasks.size() > 1) {
			throw new IllegalArgumentException("There is a duplicate Task for constraints Service[" + service.getId() + "], Facility[" + facility.getId() + "]");
		}
		return true;
	}

	@Override
	public void removeTask(Service service, Facility facility) {
		// jdbc template cannot be null
		getMyJdbcTemplate().update("delete from tasks where service_id = ? and facility_id = ?", service.getId(), facility.getId());
	}

	@Override
	public void removeTask(int id) {
		// jdbc template cannot be null
		getMyJdbcTemplate().update("delete from tasks where id = ?", id);
	}

	private int queryForInt(String sql, Object... args) {
		// jdbc template cannot be null
		Integer i = getMyJdbcTemplate().queryForObject(sql, args, Integer.class);
		return (i != null ? i : 0);
	}

	@Override
	public int countTasks() {
		return queryForInt("select count(*) from tasks");
	}

}
