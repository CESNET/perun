package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;
import java.util.List;

/**
 * Value transformer, which applies list of regular expresion substitutions ({@link RegexpSubst}) to the each value of
 * attribute.
 */
public class RegexpValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

  /**
   * List of applied regex replace operations. Initialized from the Spring context.
   */
  private List<RegexpSubst> replaceList;

  public List<RegexpSubst> getReplaceList() {
    return replaceList;
  }

  @Override
  public String getValue(String value, Attribute attr) {
    String result = value;
    for (RegexpSubst regexpSubst : replaceList) {
      result = result.replaceAll(regexpSubst.getFind(), regexpSubst.getReplace());
    }
    return result;
  }

  @Override
  public Boolean isMassTransformationPreferred() {
    return false;
  }

  @Override
  public Boolean isReduce() {
    return false;
  }

  public void setReplaceList(List<RegexpSubst> replaceList) {
    this.replaceList = replaceList;
  }

}
