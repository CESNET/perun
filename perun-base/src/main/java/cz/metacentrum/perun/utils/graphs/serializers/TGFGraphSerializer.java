package cz.metacentrum.perun.utils.graphs.serializers;

import cz.metacentrum.perun.utils.graphs.Graph;
import cz.metacentrum.perun.utils.graphs.GraphEdge;
import cz.metacentrum.perun.utils.graphs.Node;

import java.util.Set;

/**
 * Graph serializer for TGF format (*.tgf)
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class TGFGraphSerializer implements GraphSerializer {

	@Override
	public String generateTextFileContent(Graph graph) {
		StringBuilder builder = new StringBuilder();

		generateNodesData(builder, graph.getNodes().keySet());
		generateDivider(builder);
		generateEdgesData(builder, graph);

		return builder.toString();
	}

	private void generateEdgesData(StringBuilder builder, Graph graph) {
		for (Node sourceNode : graph.getNodes().keySet()) {
			for (GraphEdge edge : graph.getEdges().get(sourceNode)) {
				generateEdgeData(builder, edge);
			}
		}
	}

	private void generateNodesData(StringBuilder builder, Set<Node> nodes) {
		for (Node node : nodes) {
			generateNodeData(builder, node);
		}
	}

	private void generateDivider(StringBuilder builder) {
		builder.append("#\n");
	}

	private void generateEdgeData(StringBuilder builder, GraphEdge edge) {
		builder.append(edge.getSourceNode().getId())
				.append(" ")
				.append(edge.getTargetNode().getId())
				.append("\n");
	}

	private void generateNodeData(StringBuilder builder, Node node) {
		builder.append(node.getId())
				.append(" ")
				.append(node.getLabel())
				.append("\n");
	}
}
