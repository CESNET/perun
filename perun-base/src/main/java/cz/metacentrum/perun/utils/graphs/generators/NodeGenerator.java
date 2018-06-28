package cz.metacentrum.perun.utils.graphs.generators;

import cz.metacentrum.perun.utils.graphs.Node;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public interface NodeGenerator<T> {

	/**
	 * Generates node with given id.
	 *
	 * @return generated node
	 */
	Node generate(T entity, Long id);
}
