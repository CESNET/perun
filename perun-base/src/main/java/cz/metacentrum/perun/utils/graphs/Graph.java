package cz.metacentrum.perun.utils.graphs;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class Graph {
	private final Map<Node, Node> nodes = new HashMap<>();
	private final Map<Node, Set<GraphEdge>> edges = new HashMap<>();

	/**
	 * Find nodes that belong to component for given node.
	 *
	 * @param node node
	 * @return Set of nodes that belong to the same component as given Node.
	 */
	public Set<Node> getComponentNodes(Node node) {
		Node graphDefinitionNode = nodes.get(node);

		if (graphDefinitionNode == null) {
			return Collections.singleton(node);
		}

		return findComponentNodes(graphDefinitionNode);
	}

	/**
	 * Finds nodes that belong to the same graph component as given node.
	 *
	 * @param node node
	 * @return Nodes that belong to the same graph component as given node.
	 */
	private static Set<Node> findComponentNodes(Node node) {
		Set<Node> componentNodes = new HashSet<>();
		Set<Node> newlyDiscoveredNodes = new HashSet<>();

		newlyDiscoveredNodes.add(node);
		componentNodes.add(node);

		do {
			newlyDiscoveredNodes = findOutgoingAndIncomingNodes(newlyDiscoveredNodes);
		} while (componentNodes.addAll(newlyDiscoveredNodes));

		return componentNodes;
	}

	/**
	 * For given nodes finds all of theirs incoming and outgoing nodes.
	 *
	 * @param nodesToBeSearched nodes that will be searched.
	 * @return discovered nodes
	 */
	private static Set<Node> findOutgoingAndIncomingNodes(Set<Node> nodesToBeSearched) {
		Set<Node> newNodesToSearch = new HashSet<>();

		for (Node node : nodesToBeSearched) {
			for (GraphEdge edge : node.getOutComingEdges()) {
				newNodesToSearch.add(edge.getTargetNode());
			}
			for (GraphEdge edge : node.getInComingEdges()) {
				newNodesToSearch.add(edge.getSourceNode());
			}
		}

		return newNodesToSearch;
	}

	public void addNodes(Set<Node> nodes) {
		nodes.forEach(this::addNode);
	}

	public void addNode(Node node) {
		if (nodes.keySet().contains(node)) {
			return;
		}
		nodes.put(node, node);
		edges.put(node, new HashSet<>());
	}

	public void removeNodes(Set<Node> nodes) {
		nodes.forEach(this::removeNode);
	}

	public void removeNode(Node node) {
		node.getAllEdges().forEach(this::removeEdge);

		nodes.remove(node);
		edges.remove(node);

		for (GraphEdge edge : node.getInComingEdges()) {
			edge.getSourceNode().removeOutComingEdge(edge);
		}
		for (GraphEdge edge : node.getOutComingEdges()) {
			edge.getSourceNode().removeInComingEdge(edge);
		}
	}

	public void removeEdge(GraphEdge edge) {
		edges.get(edge.getSourceNode()).remove(edge);
		edge.getSourceNode().removeOutComingEdge(edge);
		edge.getTargetNode().removeInComingEdge(edge);
	}

	public void createEdge(Node source, Node target, GraphEdge.Type type) {
		source = nodes.get(source);
		target = nodes.get(target);

		if (source == null) {
			throw new IllegalArgumentException("Graph does not contain given source node.");
		}
		if (target == null) {
			throw new IllegalArgumentException("Graph does not contain given target node.");
		}

		GraphEdge edge = new GraphEdge(source, target, type);

		source.addOutComingEdge(edge);
		target.addInComingEdge(edge);

		edges.get(source).add(edge);
	}

	public Map<Node, Node> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}

	public Map<Node, Set<GraphEdge>> getEdges() {
		return Collections.unmodifiableMap(edges);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Graph graph = (Graph) o;
		return Objects.equals(nodes, graph.nodes) &&
				Objects.equals(edges, graph.edges);
	}

	@Override
	public int hashCode() {

		return Objects.hash(nodes, edges);
	}
}
