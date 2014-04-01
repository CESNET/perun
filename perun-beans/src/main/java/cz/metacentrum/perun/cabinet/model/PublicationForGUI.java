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
			return getClass().getSimpleName()+":[id=" + this.getId() + ", externalId=" + this.getExternalId() + ", pubSysId=" + this.getPublicationSystemId() + ", pubSysName=" + this.getPubSystemName() + ", title=" + this.getTitle() + ", categoryId=" + this.getCategoryId() + ", categoryName=" + this.getCategoryName() + ", year=" + this.getYear() + ", isbn=" + this.getIsbn() + ", doi=" + this.getDoi() + ", locked=" + this.getLocked() + ", main=" + this.getMain() + ", createdBy=" + this.getCreatedBy() + ", createdDate=" + this.getCreatedDate() + ", rank=" + this.getRank() + ", createdByUid=" + this.getCreatedByUid() + ", authors=" + this.getAuthors() + ", thanks=" + this.getThanks() + "]";
	}

}