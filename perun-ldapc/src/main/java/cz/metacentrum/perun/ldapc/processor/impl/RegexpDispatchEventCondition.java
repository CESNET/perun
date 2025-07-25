package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the message pattern matching part of dispatch conditions.
 * The primary condition used in all the processors, defined in `perun-ldapc.xml`.
 */
public class RegexpDispatchEventCondition extends SimpleDispatchEventCondition {

  private Pattern pattern;

  @Override
  public boolean isApplicable(MessageBeans beans, String msg) {
    Matcher matcher = pattern.matcher(msg);
    return super.isApplicable(beans, msg) && matcher.find();
  }

  public void setPattern(String regexp) {
    this.pattern = Pattern.compile(regexp, Pattern.DOTALL);
  }
}
