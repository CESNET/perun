package cz.metacentrum.perun.utils.graphs;

import java.util.Objects;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GraphEdge {
	private Node sourceNode;
	private Node targetNode;
	private Type type;

	public GraphEdge(Node sourceNode, Node targetNode, Type type) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.type = type;
	}

	public Node getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(Node sourceNode) {
		this.sourceNode = sourceNode;
	}

	public Node getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(Node targetNode) {
		this.targetNode = targetNode;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public enum Type {
		DASHED("dashed"),
		BOLD("bold");

		private String style;

		public String getStyle() {
			return style;
		}

		Type(String style) {
			this.style = style;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GraphEdge edge = (GraphEdge) o;
		return Objects.equals(sourceNode, edge.sourceNode) &&
				Objects.equals(targetNode, edge.targetNode) &&
				type == edge.type;
	}

	@Override
	public int hashCode() {

		return Objects.hash(sourceNode, targetNode, type);
	}
}
