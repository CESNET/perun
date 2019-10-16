package cz.metacentrum.perun.core.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class representing an extended version of {@link UserExtSource}.
 *
 * This class is used for providing additional information about an UserExtSource
 * via its attributes.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class RichUserExtSource {

	private UserExtSource userExtSource;

	private List<Attribute> attributes;

	@SuppressWarnings("unused") // used by mapper
	public RichUserExtSource() {
	}

	public RichUserExtSource(UserExtSource userExtSource, List<Attribute> attributes) {
		this.userExtSource = userExtSource;
		this.attributes = Collections.unmodifiableList(attributes);
	}

	/**
	 * View method used for accessing the original {@link UserExtSource} object
	 * of this composition.
	 *
	 * @return original {@link UserExtSource}
	 */
	@JsonProperty("userExtSource")
	public UserExtSource asUserExtSource() {
		return userExtSource;
	}

	public void setUserExtSource(UserExtSource userExtSource) {
		this.userExtSource = userExtSource;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = Collections.unmodifiableList(attributes);
	}

	public String getBeanName() {
		return getClass().getSimpleName();
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RichUserExtSource that = (RichUserExtSource) o;
		return Objects.equals(userExtSource, that.userExtSource) &&
			Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userExtSource, attributes);
	}

	@Override
	public String toString() {
		return "RichUserExtSource:[" +
			"userExtSource=" + userExtSource +
			", attributes=" + attributes +
			']';
	}
}
