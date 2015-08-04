package cz.metacentrum.perun.taskslib.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.FacilitiesManagerImpl;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *         TODO: Remove "" where not necessary...
 */
public class TaskDaoJdbc extends JdbcDaoSupport implements TaskDao {

	private static final Logger log = LoggerFactory.getLogger(TaskDaoJdbc.class);

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

	public final static String taskMappingSelectQuery = " tasks.id as tasks_id, tasks.schedule as tasks_schedule, tasks.recurrence as tasks_recurrence, " +
		"tasks.delay as tasks_delay, tasks.status as tasks_status, tasks.start_time as tasks_start_time, tasks.end_time as tasks_end_time, tasks.engine_id as tasks_engine_id ";

	public static final RowMapper<Task> TASK_ROWMAPPER = new RowMapper<Task>() {

		public Task mapRow(ResultSet rs, int i) throws SQLException {

			Task task = new Task();

			task.setDelay(rs.getInt("tasks_delay"));
			task.setId(rs.getInt("tasks_id"));
			task.setRecurrence(rs.getInt("tasks_recurrence"));

			if (rs.getTimestamp("tasks_start_time") != null) {
				task.setStartTime(rs.getTimestamp("tasks_start_time"));
			}
			if (rs.getTimestamp("tasks_schedule") != null) {
				task.setSchedule(rs.getTimestamp("tasks_schedule"));
			}
			if (rs.getTimestamp("tasks_end_time") != null) {
				task.setEndTime(rs.getTimestamp("tasks_end_time"));
			}

			if (rs.getString("tasks_status").equalsIgnoreCase(TaskStatus.DONE.toString())) {
				task.setStatus(TaskStatus.DONE);
			} else if (rs.getString("tasks_status").equalsIgnoreCase(TaskStatus.ERROR.toString())) {
				task.setStatus(TaskStatus.ERROR);
			} else if (rs.getString("tasks_status").equalsIgnoreCase(TaskStatus.NONE.toString())) {
				task.setStatus(TaskStatus.NONE);
			} else if (rs.getString("tasks_status").equalsIgnoreCase(TaskStatus.PLANNED.toString())) {
				task.setStatus(TaskStatus.PLANNED);
			} else if (rs.getString("tasks_status").equalsIgnoreCase(TaskStatus.PROCESSING.toString())) {
				task.setStatus(TaskStatus.PROCESSING);
			} else {
				throw new IllegalArgumentException("Task status unknown :-(");
			}

			task.setFacility(FacilitiesManagerImpl.FACILITY_MAPPER.mapRow(rs, i));

			task.setExecService(ExecServiceDaoJdbc.EXEC_SERVICE_ROWMAPPER.mapRow(rs, i));

			return task;
		}

	};

	public static final RowMapper<Pair<Task, Integer>> TASK_CLIENT_ROWMAPPER = new RowMapper<Pair<Task, Integer>>() {
	
		public Pair<Task, Integer> mapRow(ResultSet rs, int i) throws SQLException {
		
			Task task = TASK_ROWMAPPER.mapRow(rs, i);
			
			return new Pair<Task, Integer>(task, rs.getInt("tasks_engine_id"));
		}
			
	};	
	
	@Override
	public int scheduleNewTask(Task task, int engineID) throws InternalErrorException {
		int newTaskId = 0;
		try {
			newTaskId = Utils.getNewId(this.getJdbcTemplate(), "tasks_id_seq");
			this.getJdbcTemplate().update(
					"insert into tasks(id, exec_service_id, facility_id, schedule, recurrence, delay, status, engine_id) values (?,?,?, " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ",?,?,?,?)",
					newTaskId, task.getExecServiceId(), task.getFacilityId(), getDateFormatter().format(task.getSchedule()), task.getRecurrence(), task.getDelay(), task.getStatus().toString(), engineID);
			return newTaskId;
		} catch (DataIntegrityViolationException ex) {
			log.error("Data: id, exec_service_id, facility_id, schedule, recurrence, delay, status is: " + newTaskId + ", " + task.getExecServiceId() + ", " + task.getFacilityId() + ", "
					+ getDateFormatter().format(task.getSchedule()) + ", " + task.getRecurrence() + ", " + task.getDelay() + ", " + task.getStatus().toString() + ". Exception:" + ex.toString(), ex);
		}
		return 0;
	}
	
	@Override
	public int insertTask(Task task, int engineID) throws InternalErrorException {
		int newTaskId = 0;
		try {
			newTaskId = task.getId();
			this.getJdbcTemplate().update(
					"insert into tasks(id, exec_service_id, facility_id, schedule, recurrence, delay, status, engine_id) values (?,?,?,to_date(?,'DD-MM-YYYY HH24:MI:SS'),?,?,?,?)",
					newTaskId, task.getExecServiceId(), task.getFacilityId(), getDateFormatter().format(task.getSchedule()), task.getRecurrence(), task.getDelay(), task.getStatus().toString(), engineID);
			return newTaskId;
		} catch (DataIntegrityViolationException ex) {
			log.error("Data: id, exec_service_id, facility_id, schedule, recurrence, delay, status is: " + newTaskId + ", " + task.getExecServiceId() + ", " + task.getFacilityId() + ", "
					+ getDateFormatter().format(task.getSchedule()) + ", " + task.getRecurrence() + ", " + task.getDelay() + ", " + task.getStatus().toString() + ". Exception:" + ex.toString(), ex);
		}
		return 0;
	}

	@Override
	public Task getTask(ExecService execService, Facility facility) {
		return getTask(execService.getId(), facility.getId());
	}

	@Override
	public Task getTask(int execServiceId, int facilityId) {
		try {
			return this
				.getJdbcTemplate()
				.queryForObject(
						"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
						", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
						"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id " +
						"where exec_services.id = ? and tasks.facility_id = ?",
						new Integer[] { execServiceId, facilityId }, TASK_ROWMAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public Task getTask(ExecService execService, Facility facility, int engineID) {
		return getTask(execService.getId(), facility.getId(), engineID);
	}

	@Override
	public Task getTask(int execServiceId, int facilityId, int engineID) {
		try {
			return this
				.getJdbcTemplate()
				.queryForObject(
						"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
						", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
						"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id " +
						"where exec_services.id = ? and tasks.facility_id = ? and tasks.engine_id = ?",
						new Integer[] { execServiceId, facilityId, engineID }, TASK_ROWMAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public List<Task> listAllTasksForFacility(int facilityId) {
		try {
			return this.getJdbcTemplate().query(
					"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
					", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
					"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where facilities.id = ?",
					TASK_ROWMAPPER, facilityId);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public Task getTaskById(int id) {
		try {
			return this.getJdbcTemplate().queryForObject(
					"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
					", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery+ ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
					"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where tasks.id = ?",
					new Integer[] { id }, TASK_ROWMAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public Task getTaskById(int id, int engineID) {
		try {
			return this.getJdbcTemplate().queryForObject(
					"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
					", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery+ ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
					"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where tasks.id = ? and tasks.engine_id = ?",
					new Integer[] { id, engineID }, TASK_ROWMAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public List<Task> listAllTasks() {
		return this.getJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery+ ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id", TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listAllTasks(int engineID) {
		return this.getJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery+ ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where tasks.engine_id = ?",
				new Integer[] { engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Pair<Task, Integer>> listAllTasksAndClients() {
		return this.getJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery+ ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id", TASK_CLIENT_ROWMAPPER);
	}
	
	@Override
	public List<Task> listAllTasksInState(Task.TaskStatus state) {
		String textState = state.toString().toUpperCase();
		return this.getJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery+ ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where tasks.status = ?",
				new Object[] { textState }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listAllTasksInState(Task.TaskStatus state, int engineID) {
		String textState = state.toString().toUpperCase();
		return this.getJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where tasks.status = ? and tasks.engine_id = ?",
				new Object[] { textState, engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listAllTasksNotInState(Task.TaskStatus state, int engineID) {
		String textState = state.toString().toUpperCase();
		return this.getJdbcTemplate().query("select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where tasks.status != ? and tasks.engine_id = ?",
				new Object[] { textState, engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listTasksScheduledBetweenDates(Date olderThen, Date youngerThen) {
		return this.getJdbcTemplate().query(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where "
				+ "tasks.schedule >= " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.schedule < " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'"),
				new Object[] { getDateFormatter().format(olderThen), getDateFormatter().format(youngerThen) }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listTasksScheduledBetweenDates(Date olderThen, Date youngerThen, int engineID) {
		return this.getJdbcTemplate().query(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where "
				+ "tasks.schedule >= " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.schedule < " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.engine_id = ? ",
				new Object[] { getDateFormatter().format(olderThen), getDateFormatter().format(youngerThen), engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listTasksStartedBetweenDates(Date olderThen, Date youngerThen, int engineID) {
		return this.getJdbcTemplate().query(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where "
				+ "tasks.start_time >= " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.start_time < " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.engine_id = ?",
				new Object[] { getDateFormatter().format(olderThen), getDateFormatter().format(youngerThen), engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listTasksStartedBetweenDates(Date olderThen, Date youngerThen) {
		return this.getJdbcTemplate().query(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery  + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where "
				+ "tasks.start_time >= " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.start_time < " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'"),
				new Object[] { getDateFormatter().format(olderThen), getDateFormatter().format(youngerThen) }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listTasksEndedBetweenDates(Date olderThen, Date youngerThen, int engineID) {
		return this.getJdbcTemplate().query(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where "
				+ "tasks.end_time >= " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.end_time < " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.engine_id = ?",
				new Object[] { getDateFormatter().format(olderThen), getDateFormatter().format(youngerThen), engineID }, TASK_ROWMAPPER);
	}

	@Override
	public List<Task> listTasksEndedBetweenDates(Date olderThen, Date youngerThen) {
		return this.getJdbcTemplate().query(
				"select " + taskMappingSelectQuery + ", " + FacilitiesManagerImpl.facilityMappingSelectQuery +
				", " + ExecServiceDaoJdbc.execServiceMappingSelectQuery + ", " + ServicesManagerImpl.serviceMappingSelectQuery + " from tasks left join exec_services on tasks.exec_service_id = exec_services.id " +
				"left join facilities on facilities.id = tasks.facility_id left join services on services.id = exec_services.service_id where "
				+ "tasks.end_time >= " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " and tasks.end_time < " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'"),
				new Object[] { getDateFormatter().format(olderThen), getDateFormatter().format(youngerThen) }, TASK_ROWMAPPER);
	}

	@Override
	public void updateTask(Task task, int engineID) {
		String scheduled = null;
		if (task.getSchedule() != null) {
			scheduled = getDateFormatter().format(task.getSchedule());
		}
		String endTime = null;
		if (task.getEndTime() != null) {
			endTime = getDateFormatter().format(task.getEndTime());
		}
		String startTime = null;
		if (task.getStartTime() != null) {
			startTime = getDateFormatter().format(task.getStartTime());
		}

		this.getJdbcTemplate().update(
				"update tasks set exec_service_id = ?, facility_id = ?, schedule = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ", recurrence = ?, delay = ?, "
				+ "status = ?, start_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ", end_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " where id = ? and engine_id = ?", task.getExecServiceId(),
				task.getFacilityId(), scheduled, task.getRecurrence(), task.getDelay(), task.getStatus().toString(), startTime, endTime, task.getId(),
				engineID);
	}

	@Override
	public void updateTask(Task task) {
		String scheduled = null;
		if (task.getSchedule() != null) {
			scheduled = getDateFormatter().format(task.getSchedule());
		}
		String endTime = null;
		if (task.getEndTime() != null) {
			endTime = getDateFormatter().format(task.getEndTime());
		}
		String startTime = null;
		if (task.getStartTime() != null) {
			startTime = getDateFormatter().format(task.getStartTime());
		}

		this.getJdbcTemplate().update(
				"update tasks set exec_service_id = ?, facility_id = ?, schedule = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ", recurrence = ?, delay = ?, "
				+ "status = ?, start_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + ", end_time = " + Compatibility.toDate("?","'DD-MM-YYYY HH24:MI:SS'") + " where id = ?", task.getExecServiceId(),
				task.getFacilityId(), scheduled, task.getRecurrence(), task.getDelay(), task.getStatus().toString(), startTime, endTime, task.getId());
	}

	@Override
	public boolean isThereSuchTask(ExecService execService, Facility facility, int engineID) {
		this.getJdbcTemplate().update("select id from exec_services where id = ?", execService.getId());

		List<Integer> tasks = new ArrayList<Integer>();
		tasks = this.getJdbcTemplate().queryForList("select id from tasks where exec_service_id = ? and facility_id = ? and engine_id = ?",
				new Integer[] { execService.getId(), facility.getId(), engineID }, Integer.class);
		if (tasks.size() == 0) {
			return false;
		} else if (tasks.size() > 1) {
			throw new IllegalArgumentException("There is a duplicit Task for constraints ExecService[" + execService.getId() + "], Facility[" + facility.getId() + "]");
		}
		return true;
	}

	@Override
	public boolean isThereSuchTask(ExecService execService, Facility facility) {
		//this.getJdbcTemplate().update("select id from exec_services where id = ? for update", execService.getId());

		List<Integer> tasks = new ArrayList<Integer>();
		tasks = this.getJdbcTemplate().queryForList("select id from tasks where exec_service_id = ? and facility_id = ?",
				new Integer[] { execService.getId(), facility.getId() }, Integer.class);
		if (tasks.size() == 0) {
			return false;
		} else if (tasks.size() > 1) {
			throw new IllegalArgumentException("There is a duplicit Task for constraints ExecService[" + execService.getId() + "], Facility[" + facility.getId() + "]");
		}
		return true;
	}

	@Override
	public void removeTask(ExecService execService, Facility facility, int engineID) {
		this.getJdbcTemplate().update("delete from tasks where exec_service_id = ? and facility_id = ? and engine_id = ?",
				new Object[] { execService.getId(), facility.getId(), engineID });
	}

	@Override
	public void removeTask(ExecService execService, Facility facility) {
		this.getJdbcTemplate().update("delete from tasks where exec_service_id = ? and facility_id = ?",
				new Object[] { execService.getId(), facility.getId() });
	}

	@Override
	public void removeTask(int id, int engineID) {
		this.getJdbcTemplate().update("delete from tasks where id = ? and engine_id = ?", id, engineID);
	}

	@Override
	public void removeTask(int id) {
		this.getJdbcTemplate().update("delete from tasks where id = ?", id);
	}

	@Override
	public int countTasks(int engineID) {
		return this.getJdbcTemplate().queryForInt("select count(*) from tasks where engine_id = ?", engineID);

	}

	@Override
	public int countTasks() {
		return this.getJdbcTemplate().queryForInt("select count(*) from tasks");

	}
}
