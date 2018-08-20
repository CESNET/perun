package cz.metacentrum.perun.utils.graphs.serializers;

import cz.metacentrum.perun.utils.graphs.Graph;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface GraphSerializer {

	/**
	 * Generates content of a text file describing graph.
	 *
	 * @return content of a text file describing relations of given attributes.
	 */
	String generateTextFileContent(Graph graph);
}
