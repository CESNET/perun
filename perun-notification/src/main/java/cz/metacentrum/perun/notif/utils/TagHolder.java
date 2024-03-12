package cz.metacentrum.perun.notif.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information about tag which we already gone through User: tomastunkl Date: 29.09.12 Time: 23:46 To change this
 * template use File | Settings | File Templates.
 */
public class TagHolder {

  private static final Logger LOGGER = LoggerFactory.getLogger(TagHolder.class);
  private int startTagPosition;
  private int endTagPosition;
  private TagType type;
  private String expression;
  private boolean startTag;

  public static TagHolder resolveTagHolder(String tagString, int start, int end) {

    if (tagString == null || tagString.isEmpty()) {
      return null;
    }

    TagHolder result = new TagHolder();
    String trimmedTag = tagString.substring(1, tagString.length() - 1);
    //Removed < and >
    char startChar = trimmedTag.charAt(0);
    if (startChar == '/') {
      result.setStartTag(false);
      trimmedTag = trimmedTag.substring(2);
    } else {
      result.setStartTag(true);
      trimmedTag = trimmedTag.substring(1);
    }

    int spaceLocation = trimmedTag.indexOf(" ");
    if (spaceLocation < 0) {
      spaceLocation = trimmedTag.length();
    }
    String name = trimmedTag.substring(0, spaceLocation);
    TagType newType = TagType.resolve(name);
    if (newType == null) {
      LOGGER.warn("Tag type not recognized: " + name);
      return null;
    }

    result.setType(newType);

    result.setStartTagPosition(start);
    result.setEndTagPosition(end);

    if (result.isStartTag()) {
      int indexOfVar = trimmedTag.indexOf("var=");
      String newExpression = trimmedTag.substring(indexOfVar + 5);
      newExpression = newExpression.substring(0, newExpression.lastIndexOf('"'));
      result.setExpression(newExpression);
    }
    return result;
  }

  public int getEndTagPosition() {
    return endTagPosition;
  }

  public String getExpression() {
    return expression;
  }

  public int getStartTagPosition() {
    return startTagPosition;
  }

  public TagType getType() {
    return type;
  }

  public boolean isStartTag() {
    return startTag;
  }

  public void setEndTagPosition(int endTagPosition) {
    this.endTagPosition = endTagPosition;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public void setStartTag(boolean startTag) {
    this.startTag = startTag;
  }

  public void setStartTagPosition(int startTagPosition) {
    this.startTagPosition = startTagPosition;
  }

  public void setType(TagType type) {
    this.type = type;
  }
}
