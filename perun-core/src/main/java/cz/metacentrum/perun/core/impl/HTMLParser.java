package cz.metacentrum.perun.core.impl;

import org.owasp.html.CssSchema;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Class for sanitizing HTML input using OWASP Java HTML Sanitizer.
 * @author: Matej Hakoš <492968@mail.muni.cz>
 */
public class HTMLParser {
	private static final String[] allowedTags = {"a", "article", "aside", "b", "blockquote", "br", "button", "caption", "center", "cite", "decorator", "del", "details", "div", "em", "footer", "h1", "h2", "h3", "h4", "h5", "h6", "header", "i", "img", "kbd", "label", "li", "ol", "p", "pre", "section", "select", "span", "strong", "sup", "table", "tbody", "td", "textarea", "tfoot", "th", "thead", "tr", "ul"};
	private static final String[] allowedAttributes = {"align", "class", "color", "disabled", "height", "hidden", "href", "id", "label", "size", "span", "src", "srcset", "style", "width"};
	private static final String[] allowedStyles = {"color", "background-color", "font-size", "font-family", "text-align", "margin", "padding", "border", "width", "height", "display", "position", "top", "bottom", "left", "right", "overflow", "float", "clear", "z-index"};
	private static final String[] allowedUrlProtocols = {"https", "mailto"};
	private static PolicyFactory policy = null;

	private List<String> escapedTags = new ArrayList<>();
	private List<String> escapedAttributes = new ArrayList<>();
	private List<String> escapedStyles = new ArrayList<>();

	private String rawHTML = "";
	private String escapedHTML = "";
	private String[] escapedStrings = new String[]{"", "", ""};
	private boolean isInputValid = true;

	public HTMLParser() {
		policy = generatePolicy();
	}

	/**
	 * Creates policy defined by the allowedTags, allowedAttributes, allowedStyles and allowedUrlProtocols.
	 * Used to sanitize HTML input.
	 *
	 * @return policy - PolicyFactory object
	 */
	private static PolicyFactory generatePolicy() {
		HtmlPolicyBuilder p = new HtmlPolicyBuilder();
		p.allowUrlProtocols(allowedUrlProtocols);
		for (String tag : allowedTags) {
			p.allowElements(tag);
		}
		for (String attr : allowedAttributes) {
			p.allowAttributes(attr).globally();
		}
		CssSchema cssWhitelist = CssSchema.withProperties(Arrays.stream(allowedStyles).toList());
		p.allowStyling(cssWhitelist);
		return p.toFactory();
	}

	/**
	 * Computes the difference between all style attributes in the escaped and unescaped input.
	 *
	 * @param input   - unescaped input
	 * @param escaped - escaped input
	 */
	private void computeEscapedStyles(String input, String escaped) {
		Pattern pattern = Pattern.compile("style=\"(.*?)\"");
		if (input == null || escaped == null) return;
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			String style = matcher.group(1);
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
			String style = matcher.group(1);
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
	 * Clears the list of escaped tags and attributes.
	 * Recomputes the policy and resets the escapedHTML/unescapedHTML and escapedStrings.
	 * isInputValid is set to true.
	 */
	public HTMLParser clear() {
		escapedTags.clear();
		escapedAttributes.clear();
		escapedStyles.clear();
		escapedHTML = "";
		rawHTML = "";
		escapedStrings = new String[]{"", "", ""};
		isInputValid = true;
		policy = generatePolicy();
		return this;
	}

	/**
	 * Returns last input after the HTML sanitization process.
	 * @return escapedHTML - sanitized HTML input
	 */
	public String getEscapedHTML() {
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

	/*
	* Returns validity of the last input.
	*/
	public boolean isInputValid() {
        return isInputValid;
    }

	/**
	 * Returns array of strings containing Error strings.
	 * 0 -> tags, 1 -> attributes, 2 -> styles
	 * @return escapedStrings - array of error strings
	 */
	public String[] getEscaped() {
		return this.escapedStrings;
	}

	/**
	 * Sanitizes the given input using the predefined policy.
	 *
	 * @param input - input to sanitize
	 * @return sanitized input
	 */
	public HTMLParser sanitizeHTML(String input) {
		rawHTML = input;
		HtmlChangeListener<List<List<String>>> listener = new HtmlChangeListener<>() {
			@Override
			public void discardedTag(@Nullable List<List<String>> output, String tag) {
				output.get(0).add(tag);
			}
			
			@Override
			public void discardedAttributes(@Nullable List<List<String>> output, String tag, String... attributes) {
				output.get(1).add(Arrays.toString(attributes) + " in " + tag);
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

		escapedHTML = escapedOutput;
		isInputValid = escapedTags.isEmpty() && escapedAttributes.isEmpty() && escapedStyles.isEmpty();
		return this;
	}

	/**
	 * Checks if the given input is sanitized.
	 *
	 * @param escaped   - sanitized input
	 * @param unescaped - unsanitized input
	 * @return String[] of tags that are not the same and were removed during the sanitization
	 */
	public HTMLParser checkEscapedHTML(String escaped, String unescaped) {
		if (escaped.equals(unescaped) || isInputValid){
			escapedStrings = new String[]{"", "", ""};
			return this;
		}

		escapedStrings[0] = String.join(", ", escapedTags);
		escapedStrings[1] = String.join(", ", escapedAttributes);
		escapedStrings[2] = String.join(", ", escapedStyles);
		return this;
	}

	public HTMLParser checkEscapedHTML() {
		return checkEscapedHTML(escapedHTML, rawHTML);
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
			message.append("The following tags are not allowed: ").append(escaped[0]).append(". ");
		}
		if (escaped[1].length() > 0) {
			message.append("The following attributes are not allowed: ").append(escaped[1]).append(". ");
		}
		if (escaped[2].length() > 0) {
            message.append("The following styles are not allowed: ").append(escaped[2]).append(". ");
        }
        return message.toString();
    }

	public String getMessage() {
		return getMessage(escapedStrings);
	}
}
