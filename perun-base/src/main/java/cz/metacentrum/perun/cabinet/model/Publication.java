package cz.metacentrum.perun.cabinet.model;

import cz.metacentrum.perun.core.api.PerunBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Class represents single Publication.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Publication extends PerunBean {

	private List<Author> authors = new ArrayList<Author>();

	private int externalId;
	private int publicationSystemId;
	private String title;
	private int year;
	private String main;
	private String isbn;
	private String doi;
	private int categoryId;
	private double rank;
	private boolean locked;

	private String createdBy;
	private int createdByUid;
	private Date createdDate;

	public Publication(){}

	public Publication(int id) {
		super(id);
	}

	public Publication(int id, int externalId, int publicationSystemId, String title, int year,
	                   String main, String isbn, int categoryId, String doi, double rank, boolean locked,
	                   String createdBy, Date createdDate) {
		super(id);
		this.rank = rank;
		this.externalId = externalId;
		this.publicationSystemId = publicationSystemId;
		this.title = title;
		this.year = year;
		this.main = main;
		this.isbn = isbn;
		this.categoryId = categoryId;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.doi = doi;
		this.locked = locked;
	}

	public Publication(List<Author> authors, double rank, int id,
			int externalId, int publicationSystemId, String title,
			int year, String main, String isbn, int categoryId,
			String createdBy, Date createdDate, String doi, boolean locked) {
		super(id);
		this.authors = authors;
		this.rank = rank;
		this.externalId = externalId;
		this.publicationSystemId = publicationSystemId;
		this.title = title;
		this.year = year;
		this.main = main;
		this.isbn = isbn;
		this.categoryId = categoryId;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.doi = doi;
		this.locked = locked;
	}

	public Publication(List<Author> authors, double rank, int id,
			int externalId, int publicationSystemId, String title,
			int year, String main, String isbn, int categoryId,
			String createdBy, Date createdDate, String doi, boolean locked, int createdByUid) {
		this(authors, rank, id, externalId, publicationSystemId, title,
				year, main, isbn, categoryId, createdBy, createdDate, doi, locked);
		this.createdByUid = createdByUid;
	}

	/**
	 * Returns the value of the database column PUBLICATION.externalId
	 *
	 * @return the value of PUBLICATION.externalId
	 */
	public int getExternalId() {
		return externalId;
	}

	/**
	 * Sets the value of the database column PUBLICATION.externalId
	 *
	 * @param externalId the value for PUBLICATION.externalId
	 */
	public void setExternalId(int externalId) {
		this.externalId = externalId;
	}

	/**
	 * Returns the value of the database column PUBLICATION.publicationSystemId
	 *
	 * @return the value of PUBLICATION.publicationSystemId
	 */
	public int getPublicationSystemId() {
		return publicationSystemId;
	}

	/**
	 * Sets the value of the database column PUBLICATION.publicationSystemId
	 *
	 * @param publicationSystemId the value for PUBLICATION.publicationSystemId
	 */
	public void setPublicationSystemId(int publicationSystemId) {
		this.publicationSystemId = publicationSystemId;
	}

	/**
	 * Returns the value of the database column PUBLICATION.title
	 *
	 * @return the value of PUBLICATION.title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the value of the database column PUBLICATION.title
	 *
	 * @param title the value for PUBLICATION.title
	 */
	public void setTitle(String title) {
		this.title = title == null ? null : title.trim();
	}

	/**
	 * This method returns the value of the database column PUBLICATION.year
	 *
	 * @return the value of PUBLICATION.year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Sets the value of the database column PUBLICATION.year
	 *
	 * @param year the value for PUBLICATION.year
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * Returns the value of the database column PUBLICATION.main
	 *
	 * @return the value of PUBLICATION.main
	 */
	public String getMain() {
		return main;
	}

	/**
	 * Sets the value of the database column PUBLICATION.main
	 *
	 * @param main the value for PUBLICATION.main
	 */
	public void setMain(String main) {
		this.main = main == null ? null : main.trim();
	}

	/**
	 * Returns the value of the database column PUBLICATION.isbn
	 *
	 * @return the value of PUBLICATION.isbn
	 */
	public String getIsbn() {
		return isbn;
	}

	/**
	 * Sets the value of the database column PUBLICATION.isbn
	 *
	 * @param isbn the value for PUBLICATION.isbn
	 */
	public void setIsbn(String isbn) {
		this.isbn = isbn == null ? null : isbn.trim();
	}

	/**
	 * Returns the value of the database column PUBLICATION.categoryId
	 *
	 * @return the value of PUBLICATION.categoryId
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * Sets the value of the database column PUBLICATION.categoryId
	 *
	 * @param categoryId the value for PUBLICATION.categoryId
	 */
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * Return rank for publication
	 *
	 * @return rank of publication (value of PUBLICATION.rank)
	 */
	public double getRank() {
		return rank;
	}

	/**
	 * Sets Publications rank
	 *
	 * @param rank value for PUBLICATION.rank
	 */
	public void setRank(double rank) {
		this.rank = rank;
	}

	/**
	 * Returns the value of the database column PUBLICATION.createdBy
	 *
	 * @return the value of PUBLICATION.createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Sets the value of the database column PUBLICATION.createdBy
	 *
	 * @param createdBy the value for PUBLICATION.createdBy
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * Returns the value of the database column PUBLICATION.createdDate
	 *
	 * @return the value of PUBLICATION.createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Sets the value of the database column PUBLICATION.createdDate
	 *
	 * @param createdDate the value for PUBLICATION.createdDate
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public int getCreatedByUid() {
		return createdByUid;
	}

	public void setCreatedByUid(int createdByUid) {
		this.createdByUid = createdByUid;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public boolean getLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Publication)) return false;
		if (!super.equals(o)) return false;
		Publication that = (Publication) o;
		return externalId == that.externalId &&
				publicationSystemId == that.publicationSystemId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), externalId, publicationSystemId);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[id=").append(getId())
				.append(", externalId=").append(externalId)
				.append(", pubSysId=").append(publicationSystemId)
				.append(", title=").append(title)
				.append(", categoryId=").append(categoryId)
				.append(", year=").append(year)
				.append(", isbn=").append(isbn)
				.append(", doi=").append(doi)
				.append(", locked=").append(locked)
				.append(", main=").append(main)
				.append(", createdBy=").append(createdBy)
				.append(", createdByUid=").append(createdByUid)
				.append(", createdDate=").append(createdDate)
				.append(", rank=").append(rank)
				.append(", authors=").append(authors)
				.append("]").toString();
	}

	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[" ).append(
			"id=<").append(getId()).append(">" ).append(
			", externalIdId=<").append(getExternalId()).append(">" ).append(
			", pubSysId=<").append(getPublicationSystemId()).append(">" ).append(
			", title=<").append(getTitle()).append(">" ).append(
			", categoryId=<").append(getCategoryId()).append(">" ).append(
			", year=<").append(getYear()).append(">" ).append(
			", isbn=<").append(getIsbn()).append(">" ).append(
			", doi=<").append(getDoi()).append(">" ).append(
			", locked=<").append(getLocked()).append(">" ).append(
			", main=<").append(getMain()).append(">" ).append(
			", rank=<").append(getRank()).append(">" ).append(
			", authors=<").append(getAuthors()).append(">" ).append(
			", createdBy=<").append(getCreatedBy()).append(">" ).append(
			", createdByUid=<").append(getCreatedByUid()).append(">" ).append(
			", createdDate=<").append(getCreatedDate()).append(">" ).append(
			']').toString();
	}

}
