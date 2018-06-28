package cz.metacentrum.perun.utils.graphs.serializers;

import cz.metacentrum.perun.utils.graphs.Graph;
import cz.metacentrum.perun.utils.graphs.GraphEdge;
import cz.metacentrum.perun.utils.graphs.Node;

import java.util.Set;

/**
 * Graph serializer for DOT format (*.gv)
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class DotGraphSerializer implements GraphSerializer {

	@Override
	public String generateTextFileContent(Graph graph) {
		StringBuilder edgesStringBuilder = new StringBuilder();

		generateHeader(edgesStringBuilder);
		generateBody(edgesStringBuilder, graph);
		generateFooter(edgesStringBuilder);

		return edgesStringBuilder.toString();
	}

	private void generateHeader(StringBuilder builder) {
		builder.append(
				"digraph strongDependencies {\n" +
				"\tgraph [nodesep=0.5, ranksep=0.5];\n" +
				"\trankdir=LR;\n" +
				"\tnode [shape=box]\n"
		);
	}

	private void generateBody(StringBuilder builder, Graph graph) {
		generateNodesData(builder, graph.getNodes().keySet());
		generateEdgesData(builder, graph);
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
			builder.append("\t\"").append(node.getLabel()).append("\" [\n")
					.append("\t\tfillcolor=\"").append(node.getFillColor()).append("\"\n")
					.append("\t\tstyle=\"").append(node.getStyle()).append("\"\n")
					.append("\t]\n");
		}
	}

	private void generateEdgeData(StringBuilder builder, GraphEdge edge) {
		builder.append("\t\"")
				.append(edge.getSourceNode().getLabel())
				.append("\" -> \"")
				.append(edge.getTargetNode().getLabel())
				.append("\" [")
				.append("style=").append(edge.getType().getStyle())
				.append("];\n");
	}

	private void generateFooter(StringBuilder builder) {
		builder.append("}\n");
	}
}
