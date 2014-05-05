package cz.metacentrum.perun.notif.entities;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Holds every regex which can be used to recognize type of message
 *
 * @author tomas.tunkl
 *
 * Table pn_regex
 */
public class PerunNotifRegex {

	/**
	 * Unique identifier
	 *
	 * Column id Sequence pn_regex_id_seq
	 */
	private int id;

	/**
	 * Regex used to match against auditer message
	 *
	 * Column regex
	 */
	private String regex;

	/**
	 * Note to describe regex
	 *
	 * Column note
	 */
	private String note;

	/**
	 * Objects which can be recognized from auditer message if passes regex
	 */
	private Set<PerunNotifObject> objects = Collections.synchronizedSet(new HashSet<PerunNotifObject>());

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Set<PerunNotifObject> getObjects() {
		return objects;
	}

	public void setObjects(Set<PerunNotifObject> objects) {
		this.objects = objects;
	}

	public void addObjects(List<PerunNotifObject> objects) {
		if (objects == null) {
			this.objects = Collections.synchronizedSet(new HashSet<PerunNotifObject>());
		}

		this.objects.addAll(objects);
	}

	public void addObject(PerunNotifObject object) {
		if (objects == null) {
			this.objects = Collections.synchronizedSet(new HashSet<PerunNotifObject>());
		}

		this.objects.add(object);
	}

	public static final RowMapper<PerunNotifRegex> PERUN_NOTIF_REGEX = new RowMapper<PerunNotifRegex>() {

		public PerunNotifRegex mapRow(ResultSet rs, int i) throws SQLException {

			PerunNotifRegex result = new PerunNotifRegex();
			result.setId(rs.getInt("id"));
			result.setNote(rs.getString("note"));
			result.setRegex(rs.getString("regex"));

			return result;
		}
	};

	public void update(PerunNotifRegex updatedRegex) {
		this.setNote(updatedRegex.getNote());
		this.setRegex(updatedRegex.getRegex());
		this.setObjects(updatedRegex.getObjects());
	}

	@Override
	public String toString() {
		return "id: " + getId() + " regex: " + getRegex() + " note: " + getNote();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PerunNotifRegex)) {
			return false;
		}

		PerunNotifRegex regex1 = (PerunNotifRegex) o;

		if (id != regex1.id) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (regex != null ? regex.hashCode() : 0);
		result = 31 * result + (note != null ? note.hashCode() : 0);
		result = 31 * result + (objects != null ? objects.hashCode() : 0);
		return result;
	}
}
