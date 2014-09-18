package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Resource Tag entity.
 *
 */
public class ResourceTag extends Auditable {
	private String tagName;
	private int voId;

	public ResourceTag() {
	}

	public ResourceTag(int id, String tagName, int voId) {
		super(id);
		this.tagName = tagName;
		this.voId = voId;
	}

	public ResourceTag(int id, String tagName, int voId, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.tagName = tagName;
		this.voId = voId;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public int getVoId() {
		return voId;
	}

	public void setVoId(int voId) {
		this.voId = voId;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", tagName=<").append(getTagName() == null ? "\\0" : BeansUtils.createEscaping(getTagName())).append(">").append(
			", voId=<").append(getVoId()).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id='").append(getId()).append('\'').append(
			", tagName='").append(tagName).append('\'').append(
			", voId='").append(voId).append('\'').append(
			']').toString();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + this.getId();
		hash = 53 * hash + (this.getTagName() != null ? this.tagName.hashCode() : 0);
		hash = 53 * hash + this.voId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ResourceTag other = (ResourceTag) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if ((this.tagName == null) ? (other.tagName != null) : !this.tagName.equals(other.tagName)) {
			return false;
		}
		if ((this.getVoId() != other.getVoId())) {
			return false;
		}
		return true;
	}
}
