package cz.metacentrum.perun.cabinet.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Extension for single publication which provides more info for GUI
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PublicationForGUI extends Publication {

	private String pubSystemName;
	private String categoryName;
	private List<ThanksForGUI> thanks = new ArrayList<>();

	public PublicationForGUI(List<Author> authors, double rank, int id,
			int externalId, int publicationSystemId, String title,
			int year, String main, String isbn, int categoryId,
			String createdBy, Date createdDate, String doi, boolean locked) {
		super(authors, rank, id, externalId, publicationSystemId, title, year, main,
				isbn, categoryId, createdBy, createdDate, doi, locked);
	}

	public PublicationForGUI(List<Author> authors, double rank, int id,
			int externalId, int publicationSystemId, String title,
			int year, String main, String isbn, int categoryId,
			String createdBy, Date createdDate, String doi, boolean locked, int createdByUid) {
		super(authors, rank, id, externalId, publicationSystemId, title, year, main,
				isbn, categoryId, createdBy, createdDate, doi, locked);
		setCreatedByUid(createdByUid);
	}

	public PublicationForGUI(Publication pub){
		this(pub.getAuthors(), pub.getRank(), pub.getId(), pub.getExternalId(), pub.getPublicationSystemId(),
				pub.getTitle(), pub.getYear(), pub.getMain(), pub.getIsbn(), pub.getCategoryId(), pub.getCreatedBy(),
				pub.getCreatedDate(), pub.getDoi(), pub.getLocked(), pub.getCreatedByUid());
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
