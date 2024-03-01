package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.owasp.html.CssSchema;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;


/*
 * Class for sanitizing HTML input using OWASP Java HTML Sanitizer.
 * @author: Matej Hakoš <492968@mail.muni.cz>
 */
public class HTMLParser {
  private static final String[] ALLOWED_TAGS =
    {"a", "article", "aside", "b", "blockquote", "br", "button", "caption", "center", "cite", "decorator", "del",
        "details", "div", "em", "footer", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hr", "i", "img", "kbd",
        "label", "li", "ol", "p", "pre", "section", "select", "span", "strong", "sup", "table", "tbody", "td",
        "textarea", "tfoot", "th", "thead", "tr", "u", "ul"};
  private static final String[] ALLOWED_ATTRIBUTES =
    {"align", "class", "color", "data-lang", "disabled", "height", "hidden", "href", "id", "label", "rel", "size",
        "span", "src", "srcset", "style", "type", "width"};
  private static final String[] ALLOWED_STYLES =
    {"color", "background-color", "font-size", "font-family", "text-align", "margin", "padding", "border", "width",
        "max-height", "height", "display", "position", "top", "bottom", "left", "right", "overflow", "float", "clear",
        "z-index"};
  private static final String[] ALLOWED_URL_PROTOCOLS = {"https", "mailto"};
  private static PolicyFactory policy = null;

  private List<String> escapedTags = new ArrayList<>();
  private List<String> escapedAttributes = new ArrayList<>();
  private List<String> escapedStyles = new ArrayList<>();
  private List<String> escapedLinks = new ArrayList<>();

  private String rawHTML = "";
  private String escapedHTML = "";
  private String[] escapedStrings = new String[] {"", "", "", ""};
  private boolean isInputValid = true;

  public HTMLParser() {
    policy = generatePolicy();
  }

  /**
   * Creates policy defined by the allowedTags, allowedAttributes, allowedStyles and allowedUrlProtocols. Used to
   * sanitize HTML input.
   *
   * @return policy - PolicyFactory object
   */
  private static PolicyFactory generatePolicy() {
    HtmlPolicyBuilder p = new HtmlPolicyBuilder();
    p.allowUrlProtocols(ALLOWED_URL_PROTOCOLS);
    for (String tag : ALLOWED_TAGS) {
      p.allowElements(tag);
    }
    for (String attr : ALLOWED_ATTRIBUTES) {
      p.allowAttributes(attr).globally();
    }
    CssSchema cssWhitelist = CssSchema.withProperties(Arrays.stream(ALLOWED_STYLES).toList());
    p.allowStyling(cssWhitelist);

    // allow 'href' and 'target' attribute on 'a' tags
    p.allowAttributes("href").onElements("a");
    p.allowAttributes("target").onElements("a");
    return p.toFactory();
  }

  /**
   * Returns a string containing the tags and attributes that were removed during the sanitization.
   *
   * @param escaped - array of tags and attributes that were removed
   * @return message - string containing the tags and attributes that were removed
   */
  public static String getMessage(String[] escaped) {
    StringBuilder message = new StringBuilder();
    if (escaped[0].length() > 0) {
      message.append("Following tags are invalid: ").append(escaped[0]).append(". ");
    }
    if (escaped[1].length() > 0) {
      message.append("Following attributes are invalid: ").append(escaped[1]).append(". ");
    }
    if (escaped[2].length() > 0) {
      message.append("Following styles are invalid: ").append(escaped[2]).append(". ");
    }
    if (escaped[3].length() > 0) {
      message.append("'").append(escaped[3]).append("' links contain invalid protocol (allowed: https, mailto).");
    }
    return message.toString();
  }

  public String getMessage() {
    return getMessage(escapedStrings);
  }

  public HTMLParser checkEscapedHTML() {
    return checkEscapedHTML(escapedHTML, rawHTML);
  }

  /**
   * Checks if the given input is sanitized.
   *
   * @param escaped   - sanitized input
   * @param unescaped - unsanitized input
   * @return String[] of tags that are not the same and were removed during the sanitization
   */
  public HTMLParser checkEscapedHTML(String escaped, String unescaped) {
    if (escaped.equals(unescaped) || isInputValid) {
      escapedStrings = new String[] {"", "", "", ""};
      return this;
    }

    escapedStrings[0] = String.join(", ", escapedTags);
    escapedStrings[1] = String.join(", ", escapedAttributes);
    escapedStrings[2] = String.join(", ", escapedStyles);
    escapedStrings[3] = String.join(", ", escapedLinks);
    return this;
  }

  /**
   * Clears the list of escaped tags and attributes. Recomputes the policy and resets the escapedHTML/unescapedHTML and
   * escapedStrings. isInputValid is set to true.
   */
  public HTMLParser clear() {
    escapedTags.clear();
    escapedAttributes.clear();
    escapedStyles.clear();
    escapedLinks.clear();
    escapedHTML = "";
    rawHTML = "";
    escapedStrings = new String[] {"", "", "", ""};
    isInputValid = true;
    policy = generatePolicy();
    return this;
  }

  /**
   * Computes the difference between all style attributes in the escaped and unescaped input.
   *
   * @param input   - unescaped input
   * @param escaped - escaped input
   */
  private void computeEscapedStyles(String input, String escaped) {
    Pattern pattern = Pattern.compile("style=(\"|')(.*?)(\"|')");
    if (input == null || escaped == null) {
      return;
    }
    Matcher matcher = pattern.matcher(input);
    while (matcher.find()) {
      String style = matcher.group(2);
      String[] styles = style.split(";");
      for (String s : styles) {
        String[] split = s.split(":");
        if (split.length == 2) {
          escapedStyles.add(split[0].trim());
        }
      }
    }
    matcher = pattern.matcher(escaped);
    while (matcher.find()) {
      String style = matcher.group(2);
      String[] styles = style.split(";");
      for (String s : styles) {
        String[] split = s.split(":");
        if (split.length == 2) {
          escapedStyles.remove(split[0].trim());
        }
      }
    }
  }

  /**
   * Computes the difference between all links in the escaped and unescaped input.
   *
   * @param input   - unescaped input
   * @param escaped - escaped input
   */
  public void computeInvalidLink(String input, String escaped) {
    Pattern pattern = Pattern.compile("href=(\"|')(.*?)(\"|')");
    if (input == null || escaped == null) {
      return;
    }
    Matcher matcher = pattern.matcher(input);
    while (matcher.find()) {
      // Multiple groups, url is in group 2
      String link = matcher.group(2).trim();
      escapedLinks.add(link);
    }

    for (String protocol : ALLOWED_URL_PROTOCOLS) {
      escapedLinks.removeIf(link -> link.startsWith(protocol));
    }
  }

  /**
   * Returns array of strings containing Error strings. 0 -> tags, 1 -> attributes, 2 -> styles
   *
   * @return escapedStrings - array of error strings
   */
  public String[] getEscaped() {
    return this.escapedStrings;
  }

  /**
   * Returns last input after the HTML sanitization process.
   *
   * @return escapedHTML - sanitized HTML input
   */
  public String getEscapedHTML() {
    if (!BeansUtils.getCoreConfig().getForceHtmlSanitization()) {
      return rawHTML;
    }
    return escapedHTML;
  }

  /**
   * Returns last input used in the sanitizeHTML method.
   *
   * @return escapedTags - list of escaped tags
   */
  public String getRawInput() {
    return rawHTML;
  }

  /**
   * Sanitizes input checkbox label. Only <a> elements with `href` and `target` attributes are allowed.
   *
   * @param input checkbox label to sanitize
   * @return true if safe, false otherwise
   */
  public boolean isCheckboxLabelSafe(String input) {
    if (!BeansUtils.getCoreConfig().getForceHtmlSanitization()) {
      return true;
    }
    // keep track of removed attr/elements, don't care which is which, just to know something was removed.
    List<String> removed = new ArrayList<>();

    HtmlChangeListener<List<String>> listener = new HtmlChangeListener<>() {
      @Override
      public void discardedTag(@Nullable List<String> output, String tag) {
        output.add(tag);
      }

      @Override
      public void discardedAttributes(@Nullable List<String> output, String tag, String... attributes) {
        if (tag.equals("a")) {
          output.addAll(List.of(attributes));
        }
      }
    };
    HtmlPolicyBuilder p = new HtmlPolicyBuilder();
    p.allowUrlProtocols(ALLOWED_URL_PROTOCOLS);
    p.allowElements("a");
    p.allowAttributes("href", "target").onElements("a");
    CssSchema cssWhitelist = CssSchema.withProperties(Arrays.stream(ALLOWED_STYLES).toList());
    p.allowStyling(cssWhitelist);

    String sanitized = p.toFactory().sanitize(input, listener, removed);

    // cannot just simply compare strings, order of attributes can differ
    return removed.isEmpty();
  }

  /*
   * Returns validity of the last input.
   */
  public boolean isInputValid() {
    if (!BeansUtils.getCoreConfig().getForceHtmlSanitization()) {
      return true;
    }
    return isInputValid;
  }

  /**
   * Sanitizes the given input using the predefined policy.
   *
   * @param input - input to sanitize
   * @return sanitized input
   */
  public HTMLParser sanitizeHTML(String input) {
    rawHTML = input;
    if (!BeansUtils.getCoreConfig().getForceHtmlSanitization()) {
      return this;
    }
    HtmlChangeListener<List<List<String>>> listener = new HtmlChangeListener<>() {
      @Override
      public void discardedTag(@Nullable List<List<String>> output, String tag) {
        if (tag.equals("a")) {
          return;
        }
        output.get(0).add(tag);
      }

      @Override
      public void discardedAttributes(@Nullable List<List<String>> output, String tag, String... attributes) {
        // Remove 'href', because it gets computed in computeInvalidLink
        List<String> attrs = Arrays.stream(attributes).filter(attr -> !attr.equals("href")).toList();
        if (attrs.isEmpty()) {
          return;
        }
        output.get(1).add(tag + " " + attrs);
      }
    };

    // 0 -> tags, 1 -> attributes
    List<List<String>> output = new ArrayList<>();
    output.add(new ArrayList<>());
    output.add(new ArrayList<>());
    String escapedOutput = policy.sanitize(input, listener, output);

    escapedTags = output.get(0);
    escapedAttributes = output.get(1);
    computeEscapedStyles(input, escapedOutput);
    computeInvalidLink(input, escapedOutput);

    // Remove whitespaces and filter empty strings
    escapedTags = escapedTags.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    escapedAttributes = escapedAttributes.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    escapedStyles = escapedStyles.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    escapedLinks = escapedLinks.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();

    escapedHTML = escapedOutput;
    isInputValid =
        escapedTags.isEmpty() && escapedAttributes.isEmpty() && escapedStyles.isEmpty() && escapedLinks.isEmpty();
    return this;
  }
}
