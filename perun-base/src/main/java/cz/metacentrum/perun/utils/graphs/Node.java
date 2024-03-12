package cz.metacentrum.perun.utils.graphs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class representing node in {@link Graph}.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class Node {
  private long id;
  private String label;
  private String fillColor;
  private String style;
  private Set<GraphEdge> outComingEdges = new HashSet<>();
  private Set<GraphEdge> inComingEdges = new HashSet<>();

  public void addInComingEdge(GraphEdge edge) {
    if (edge.getTargetNode() != this) {
      throw new IllegalArgumentException("Invalid edge given. Target node is not equal to this node.");
    }
    inComingEdges.add(edge);
  }

  public void addOutComingEdge(GraphEdge edge) {
    if (edge.getSourceNode() != this) {
      throw new IllegalArgumentException("Invalid edge given. Source node is not equal to this node.");
    }
    outComingEdges.add(edge);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Node node = (Node) o;
    return Objects.equals(label, node.label);
  }

  public Set<GraphEdge> getAllEdges() {
    Set<GraphEdge> allEdges = new HashSet<>(outComingEdges);
    allEdges.addAll(inComingEdges);
    return allEdges;
  }

  public String getFillColor() {
    return fillColor;
  }

  public void setFillColor(String fillColor) {
    this.fillColor = fillColor;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Set<GraphEdge> getInComingEdges() {
    return Collections.unmodifiableSet(inComingEdges);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Set<GraphEdge> getOutComingEdges() {
    return Collections.unmodifiableSet(outComingEdges);
  }

  public String getStyle() {
    return style;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  @Override
  public int hashCode() {

    return Objects.hash(label);
  }

  public void removeInComingEdge(GraphEdge edge) {
    inComingEdges.remove(edge);
  }

  public void removeOutComingEdge(GraphEdge edge) {
    outComingEdges.remove(edge);
  }
}
