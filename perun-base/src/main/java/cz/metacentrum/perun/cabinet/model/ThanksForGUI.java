package cz.metacentrum.perun.cabinet.model;

import java.util.Date;

/**
 * Extension for single Thanks to provide GUI more info
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ThanksForGUI extends Thanks {

	private String ownerName;

	public ThanksForGUI() {
		super();
	}

	public ThanksForGUI(int id, int publicationId, int ownerId,
			String createdBy, Date createdDate) {
		super(id, publicationId, ownerId, createdBy, createdDate);
	}

	public ThanksForGUI(int id, int publicationId, int ownerId,
			String createdBy, Date createdDate, int createByUid) {
		super(id, publicationId, ownerId, createdBy, createdDate);
		setCreatedByUid(createByUid);
	}

	public ThanksForGUI(Thanks thanks) {
		setId(thanks.getId());
		setPublicationId(thanks.getPublicationId());
		setCreatedBy(thanks.getCreatedBy());
		setCreatedDate(thanks.getCreatedDate());
		setOwnerId(thanks.getOwnerId());
		setCreatedByUid(thanks.getCreatedByUid());
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String owner) {
		this.ownerName = owner;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[id=").append(this.getId()).append(", pubId=").append(this.getPublicationId()).append(", ownerId=").append(this.getOwnerId()).append(", ownerName=").append(ownerName).append(", createdBy=").append(this.getCreatedBy()).append(", createdByUid=").append(this.getCreatedByUid()).append(", createdDate=").append(this.getCreatedDate()).append("]").toString();
	}

}
