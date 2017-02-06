package cz.metacentrum.perun.cabinet.model;

import cz.metacentrum.perun.core.api.PerunBean;

import java.util.Objects;

/**
 * This class represents a category (of publication). I.e. patent, article in journal etc.
 * Each category is supposed to have some rank, which expresses the importance of given category.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Category extends PerunBean {

	private String name;
	private Double rank;

	public Category() {}

	public Category(int id, String name, Double rank) {
		super(id);
		this.name = name;
		this.rank = rank;
	}

	/**
	 * Get Category name
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set Category name.
	 *
	 * @param name the value for CATEGORY.name
	 */
	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	/**
	 * Get Category rank.
	 *
	 * @return Rank of Category
	 */
	public Double getRank() {
		return rank;
	}

	/**
	 * Set Category rank.
	 *
	 * @param rank the value for CATEGORY.rank
	 */
	public void setRank(Double rank) {
		this.rank = rank;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, rank);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Category)) return false;
		if (!super.equals(o)) return false;
		Category category = (Category) o;
		return Objects.equals(name, category.name) &&
				Objects.equals(rank, category.rank);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(getClass().getSimpleName()).append(":[id=").append(getId()).append(", name=").append(name).append(", rank=").append(rank).append( "]").toString();
	}

}
