package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GenMemberDataNode {

	private final List<String> hashes;

	public GenMemberDataNode(List<String> hashes) {
		this.hashes = hashes;
	}

	public void addHashes(Collection<String> hashes) {
		this.hashes.addAll(hashes);
	}

	public List<String> getH() {
		return Collections.unmodifiableList(hashes);
	}

}
