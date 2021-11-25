package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;

import java.util.List;
import java.util.Objects;

public class RichExtSource extends ExtSource {
	private List<Attribute> attributes;

	public RichExtSource(ExtSource extSource) {
		super(extSource.getId(), extSource.getName(), extSource.getType());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		RichExtSource that = (RichExtSource) o;
		return Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), attributes);
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
}
