package cz.metacentrum.perun.utils.graphs;

import cz.metacentrum.perun.utils.graphs.serializers.DotGraphSerializer;
import cz.metacentrum.perun.utils.graphs.serializers.GraphSerializer;
import cz.metacentrum.perun.utils.graphs.serializers.TgfGraphSerializer;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public enum GraphTextFormat {
  DOT(DotGraphSerializer::new), TGF(TgfGraphSerializer::new);

  private GetSerializerAction getSerializerAction;

  GraphTextFormat(GetSerializerAction getSerializerAction) {
    this.getSerializerAction = getSerializerAction;
  }

  public GraphSerializer getSerializer() {
    return getSerializerAction.get();
  }

  @FunctionalInterface
  private interface GetSerializerAction {
    GraphSerializer get();
  }
}
