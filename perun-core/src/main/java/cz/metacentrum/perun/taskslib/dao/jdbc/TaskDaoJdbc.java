package cz.metacentrum.perun.taskslib.dao.jdbc;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.FacilitiesManagerImpl;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.text.SimpleDateFormat;
;import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *         TODO: Remove "" where not necessary...
 */
public class TaskDaoJdbc extends JdbcDaoSupport implements TaskDao {

	private static final Logger log = LoggerFactory.getLogger(TaskDaoJdbc.class);

	private JdbcTemplate getMyJdbcTemplate() {
		// jdbc template cannot be null
		this.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		return this.getJdbcTemplate();
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
		"tasks.delay as tasks_delay, tasks.status as tasks_status, tasks.start_time as tasks_start_time, tasks.end_time as tasks_end_time, tasks.engine_id as tasks_engine_id ";

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

		if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.WAITING.toString())) {
			task.setStatus(TaskStatus.WAITING);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.PLANNED.toString())) {
			task.setStatus(TaskStatus.PLANNED);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.SENDERROR.toString())) {
			task.setStatus(TaskStatus.SENDERROR);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.GENERROR.toString())) {
			task.setStatus(TaskStatus.GENERROR);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.GENERATING.toString())) {
			task.setStatus(TaskStatus.GENERATING);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.GENERATED.toString())) {
			task.setStatus(TaskStatus.GENERATED);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.SENDING.toString())) {
			task.setStatus(TaskStatus.SENDING);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.DONE.toString())) {
			task.setStatus(TaskStatus.DONE);
		} else if (resultSet.getString("tasks_status").equalsIgnoreCase(TaskStatus.ERROR.toString())) {
			task.setStatus(TaskStatus.ERROR);
		} else {
			throw new IllegalArgumentException("Task status [" + resultSet.getString("tasks_status") + "] unknown");
		}

		task.setFacility(FacilitiesManagerImpl.FACILITY_MAPPER.mapRow(resultSet, i));

		task.setService(ServicesManagerImpl.SERVICE_MAPPER.mapRow(resultSet, i));

		return task;
	};

	public static final RowMapper<Pair<Task, Integer>> TASK_CLIENT_ROWMAPPER = (resultSet, i) -> {

		Task task = TASK_ROWMAPPER.mapRow(resultSet, i);

		int engineID = resultSet.getInt("tasks_engine_id");
		if(resultSet.wasNull()) {
			engineID = -1;
		}
		return new Pair<>(task, engineID);
	};

	@Override
	public int scheduleNewTask(Task task, int engineID) {
		int newTaskId = 0;
		try {
			newTaskId = Utils.getNewId(getMyJdbcTemplate(), "tasks_id_seq");
			// jdbc template cannot be null
			getMyJdbcTemplate().update(
						"insert into tasks(id, service_id, facility_id, schedule, recurrence, delay, status, engine_id) values (?,?,?, " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ",?,?,?,?)",
						newTaskId, task.getServiceId(), task.getFacilityId(), task.getSchedule().format(getDateTimeFormatter()), task.getRecurrence(), task.getDelay(), task.getStatus().toString(), engineID < 0 ? null : engineID);
			log.debug("Added task with ID {}", newTaskId);
			return newTaskId;
		} catch (DataIntegrityViolationException ex) {
			log.error("Data: id, service_id, facility_id, schedule, recurrence, delay, status is: " + newTaskId + ", " + task.getServiceId() + ", " + task.getFacilityId() + ", "
					+ task.getSchedule().format(getDateTimeFormatter()) + ", " + task.getRecurrence() + ", " + task.getDelay() + ", " + task.getStatus().toString() + ". Exception:" + ex.toString(), ex);
		} catch (Exception ex) {
			log.error("ERRORSTR: {}", ex);
		}
		return 0;
	}

	@Override
	public int insertTask(Task task, int engineID) {
		int newTaskId = 0;
		try {
			newTaskId = task.getId();
			// jdbc template cannot be null
			getMyJdbcTemplate().update(
					"insert into tasks(id, service_id, facility_id, schedule, recurrence, delay, status, engine_id) values (?,?,?,to_date(?,'DD-MM-YYYY HH24:MI:SS'),?,?,?,?)",
					newTaskId, task.getServiceId(), task.getFacilityId(), task.getSchedule().format(getDateTimeFormatter()), task.getRecurrence(), task.getDelay(), task.getStatus().toString(), engineID < 0 ? null : engineID);
			return newTaskId;
		} catch (DataIntegrityViolationException ex) {
			log.error("Data: id, service_id, facility_id, schedule, recurrence, delay, status is: " + newTaskId + ", " + task.getServiceId() + ", " + task.getFacilityId() + ", "
					+ task.getSchedule().format(getDateTimeFormatter()) + ", " + task.getRecurrence() + ", " + task.getDelay() + ", " + task.getStatus().toString() + ". Exception:" + ex.toString(), ex);
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
	public Task getTask(Service service, Facility facility, int engineID) {
		return getTask(service.getId(), facility.getId(), engineID);
	}

	@Override
	public Task getTask(int serviceId, int facilityId, int engineID) {
		try {
			// jdbc template cannot be null
			return getMyJdbcTemplate().queryForObject(
						"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
						", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join services on tasks.service_id = services.id " +
						"left join facilities on facilities.id = tasks.facility_id " +
						"where tasks.service_id = ? and tasks.facility_id = ? and tasks.engine_id " + (engineID < 0 ? "is null" : "= ?"),
						engineID < 0 ? new Integer[] { serviceId, facilityId } : new Integer[] { serviceId, facilityId, engineID},
						TASK_ROWMAPPER);
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
	public Task getTaskById(int id, int engineID) {
		try {
			// jdbc template cannot be null
			return getMyJdbcTemplate().queryForObject(
					"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
					", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
					"left join facilities on facilities.id = tasks.facility_id where tasks.id = ? and tasks.engine_id " + (engineID < 0 ? "is null" : "= ?"),
					engineID < 0 ? new Integer[] { id } : new Integer[] { id, engineID }, TASK_ROWMAPPER);
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
	public List<Task> listAllTasks(int engineID) {
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
				"left join facilities on facilities.id = tasks.facility_id left where tasks.engine_id " + (engineID < 0 ? "is null" : "= ?"),
				engineID < 0 ? new Integer[] { } : new Integer[] { engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Pair<Task, Integer>> listAllTasksAndClients() {
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
				"left join facilities on facilities.id = tasks.facility_id", TASK_CLIENT_ROWMAPPER);
	}

	@Override
	public List<Task> listAllTasksInState(Task.TaskStatus state) {
		String textState = state.toString().toUpperCase();
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
				"left join facilities on facilities.id = tasks.facility_id left where tasks.status = ?",
				TASK_ROWMAPPER, textState);
	}

	@Override
	public List<Task> listAllTasksInState(Task.TaskStatus state, int engineID) {
		String textState = state.toString().toUpperCase();
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
				"left join facilities on facilities.id = tasks.facility_id where tasks.status = ? and tasks.engine_id " + (engineID < 0 ? "is null" : "= ?"),
				engineID < 0 ? new Object[] { textState } : new Object[] { textState, engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listAllTasksNotInState(Task.TaskStatus state, int engineID) {
		String textState = state.toString().toUpperCase();
		// jdbc template cannot be null
		return getMyJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join services on tasks.service_id = services.id " +
				"left join facilities on facilities.id = tasks.facility_id where tasks.status != ? and tasks.engine_id " + (engineID < 0 ? "is null" : "= ?"),
				engineID < 0 ? new Object[] { textState } : new Object[] { textState, engineID }, TASK_ROWMAPPER);
	}

	@Override
	public void updateTask(Task task, int engineID) {
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
				+ "status = ?, start_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ", end_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " where id = ? and engine_id " + (engineID < 0 ? "is null" : "= ?"), task.getServiceId(),
				task.getFacilityId(), scheduled, task.getRecurrence(), task.getDelay(), task.getStatus().toString(), startTime, endTime, task.getId(),
				engineID < 0 ? null : engineID);
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
	public void updateTaskEngine(Task task, int engineID) throws InternalErrorException {
		try {
			// jdbc template cannot be null
			getMyJdbcTemplate().update(
				"update tasks set engine_id = ? where id = ?", engineID < 0 ? null : engineID, task.getId());
		} catch(Exception e) {
			throw new InternalErrorException("Error updating engine id", e);
		}
	}

	@Override
	public boolean isThereSuchTask(Service service, Facility facility, int engineID) {
		// jdbc template cannot be null
		getMyJdbcTemplate().update("select id from services where id = ?", service.getId());

		List<Integer> tasks = getMyJdbcTemplate().queryForList("select id from tasks where service_id = ? and facility_id = ? and engine_id " + (engineID < 0 ? "is null" : "= ?"),
				engineID < 0 ? new Integer[] { service.getId(), facility.getId() }
					: new Integer[] { service.getId(), facility.getId(),  engineID }, Integer.class);
		if (tasks.size() == 0) {
			return false;
		} else if (tasks.size() > 1) {
			throw new IllegalArgumentException("There is a duplicate Task for constraints Service[" + service.getId() + "], Facility[" + facility.getId() + "]");
		}
		return true;
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
	public void removeTask(Service service, Facility facility, int engineID) {
		// jdbc template cannot be null
		getMyJdbcTemplate().update("delete from tasks where service_id = ? and facility_id = ? and engine_id " + (engineID < 0 ? "is null" : "= ?"),
				engineID < 0 ? new Object[] { service.getId(), facility.getId() }
					: new Object[] { service.getId(), facility.getId(), engineID });
	}

	@Override
	public void removeTask(Service service, Facility facility) {
		// jdbc template cannot be null
		getMyJdbcTemplate().update("delete from tasks where service_id = ? and facility_id = ?", service.getId(), facility.getId());
	}

	@Override
	public void removeTask(int id, int engineID) {
		if(engineID < 0) {
			// jdbc template cannot be null
			getMyJdbcTemplate().update("delete from tasks where id = ? and engine_id is null", id);
		} else {
			// jdbc template cannot be null
			getMyJdbcTemplate().update("delete from tasks where id = ? and engine_id = ?", id, engineID);
		}
	}

	@Override
	public void removeTask(int id) {
		// jdbc template cannot be null
		getMyJdbcTemplate().update("delete from tasks where id = ?", id);
	}

	private int queryForInt(String sql, Object... args) throws DataAccessException {
		// jdbc template cannot be null
		Integer i = getMyJdbcTemplate().queryForObject(sql, args, Integer.class);
		return (i != null ? i : 0);
	}

	@Override
	public int countTasks(int engineID) {
		if(engineID < 0) {
			return queryForInt("select count(*) from tasks where engine_id is null");
		} else {
			return queryForInt("select count(*) from tasks where engine_id = ?", engineID );
		}
	}

	@Override
	public int countTasks() {
		return queryForInt("select count(*) from tasks");

	}
}
