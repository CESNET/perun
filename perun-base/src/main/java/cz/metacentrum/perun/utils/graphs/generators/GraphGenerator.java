package cz.metacentrum.perun.utils.graphs.generators;

import cz.metacentrum.perun.utils.graphs.Graph;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface GraphGenerator<T> {

	/**
	 * Generates graph from given graph definition and with given node generator.
	 *
	 * @return generated graph
	 */
	Graph generate(NodeGenerator<T> nodeGenerator, GraphDefinition<T> graphDefinition);
}
