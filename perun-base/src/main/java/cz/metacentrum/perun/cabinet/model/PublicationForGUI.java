package cz.metacentrum.perun.cabinet.model;

import java.util.Date;
import java.util.List;

/**
 * Extension for single publication which provides more info for GUI
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PublicationForGUI extends Publication{

	private static final long serialVersionUID = 1L;

	private String pubSystemName;
	private String categoryName;
	private List<ThanksForGUI> thanks;

	public PublicationForGUI() {
		super();
	}

	public PublicationForGUI(List<Author> authors, Double rank, Integer id,
			Integer externalId, Integer publicationSystemId, String title,
			Integer year, String main, String isbn, Integer categoryId,
			String createdBy, Date createdDate, String doi, Boolean locked) {
		super(authors, rank, id, externalId, publicationSystemId, title, year, main,
				isbn, categoryId, createdBy, createdDate, doi, locked);
	}

	public PublicationForGUI(List<Author> authors, Double rank, Integer id,
			Integer externalId, Integer publicationSystemId, String title,
			Integer year, String main, String isbn, Integer categoryId,
			String createdBy, Date createdDate, String doi, Boolean locked, Integer createdByUid) {
		super(authors, rank, id, externalId, publicationSystemId, title, year, main,
				isbn, categoryId, createdBy, createdDate, doi, locked);
		setCreatedByUid(createdByUid);
	}

	public PublicationForGUI(Publication pub){
		setId(pub.getId());
		setTitle(pub.getTitle());
		setMain(pub.getMain());
		setYear(pub.getYear());
		setIsbn(pub.getIsbn());
		setCreatedBy(pub.getCreatedBy());
		setCreatedDate(pub.getCreatedDate());
		setCategoryId(pub.getCategoryId());
		setExternalId(pub.getExternalId());
		setPublicationSystemId(pub.getPublicationSystemId());
		setAuthors(pub.getAuthors());
		setRank(pub.getRank());
		setDoi(pub.getDoi());
		setLocked(pub.getLocked());
		setCreatedByUid(pub.getCreatedByUid());
	}

	public List<ThanksForGUI> getThanks() {
		return thanks;
	}

	public void setThanks(List<ThanksForGUI> thanks) {
		this.thanks = thanks;
	}

	public String getPubSystemName() {
		return pubSystemName;
	}

	public void setPubSystemName(String pubSystemName) {
		this.pubSystemName = pubSystemName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[id=").append(this.getId()).append(", externalId=").append(this.getExternalId()).append(", pubSysId=").append(this.getPublicationSystemId()).append(", pubSysName=").append(this.getPubSystemName()).append(", title=").append(this.getTitle()).append(", categoryId=").append(this.getCategoryId()).append(", categoryName=").append(this.getCategoryName()).append(", year=").append(this.getYear()).append(", isbn=").append(this.getIsbn()).append(", doi=").append(this.getDoi()).append(", locked=").append(this.getLocked()).append(", main=").append(this.getMain()).append(", createdBy=").append(this.getCreatedBy()).append(", createdDate=").append(this.getCreatedDate()).append(", rank=").append(this.getRank()).append(", createdByUid=").append(this.getCreatedByUid()).append(", authors=").append(this.getAuthors()).append(", thanks=").append(this.getThanks()).append("]").toString();
	}

}
