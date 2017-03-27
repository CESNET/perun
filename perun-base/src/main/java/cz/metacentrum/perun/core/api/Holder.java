package cz.metacentrum.perun.core.api;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;

import java.io.Serializable;

/**
 * Holder of an attribute. Represents who does the attribute belong to. Holder is identified by id and type (member, group..)
 *
 * @author Simona Kruppova
 */
@Indexed
public class Holder implements Serializable {
	@Field
	@NumericField
	private int id;

	@Field(analyze = Analyze.NO)
	private HolderType type;

	public enum HolderType {
		FACILITY, MEMBER, VO, GROUP, HOST, RESOURCE, USER, UES
	}

	public Holder(int id, HolderType type) {
		this.id = id;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HolderType getType() {
		return type;
	}

	public void setType(HolderType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Holder{");
		sb.append("id=").append(id);
		sb.append(", type=").append(type);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Holder holder = (Holder) o;

		if (id != holder.id) return false;
		if (type != holder.type) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + type.hashCode();
		return result;
	}
}
