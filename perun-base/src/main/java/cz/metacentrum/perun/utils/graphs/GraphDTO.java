package cz.metacentrum.perun.utils.graphs;

/**
 * Class used for serializing graph data.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GraphDTO {

	private String graph;
	private String format;

	public GraphDTO() {
	}

	public GraphDTO(String graph, String format) {
		this.graph = graph;
		this.format = format;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
