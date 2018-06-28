package cz.metacentrum.perun.utils.graphs.generators;

import cz.metacentrum.perun.utils.graphs.Graph;
import cz.metacentrum.perun.utils.graphs.GraphEdge;
import cz.metacentrum.perun.utils.graphs.Node;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link GraphGenerator }. Creates graph from given definition
 * without duplicated edges and without nodes that has no edges.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class NoDuplicatedEdgesGraphGenerator<T> implements GraphGenerator<T> {

	@Override
	public Graph generate(NodeGenerator<T> nodeGenerator, GraphDefinition<T> graphDefinition) {
		Graph graph = new Graph();
		Long identifier = 0L;

		for (GraphEdge.Type edgeType : graphDefinition.getEdgeTypes()) {
			addDataFromDependencies(graphDefinition.getEdgeData(edgeType), graph, edgeType, identifier, nodeGenerator);
		}

		return graph;
	}

	/**
	 * Adds data to given graph with specified edge type.
	 *
	 * @param data data that are transformed to nodes
	 * @param graph graph where new data is added
	 * @param edgeType used edge type
	 * @param identifier identifier used to generate ids for nodes
	 */
	private void addDataFromDependencies(Map<T, Set<T>> data, Graph graph, GraphEdge.Type edgeType, Long identifier,
										 NodeGenerator<T> nodeGenerator) {
		for (T source : data.keySet()) {
			Node sourceNode = nodeGenerator.generate(source, identifier);

			if (data.get(source).isEmpty()) {
				continue;
			}

			identifier++;

			if (!graph.getNodes().containsKey(sourceNode)) {
				graph.addNode(sourceNode);
			}

			for(T destination : data.get(source)) {
				Node destinationNode = nodeGenerator.generate(destination, identifier);

				if (!graph.getNodes().containsKey(destinationNode)) {
					graph.addNode(destinationNode);
					identifier++;
				}

				graph.createEdge(sourceNode, destinationNode, edgeType);
			}
		}
	}
}
