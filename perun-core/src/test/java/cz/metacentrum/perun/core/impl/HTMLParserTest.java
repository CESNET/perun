package cz.metacentrum.perun.core.impl;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * @author: Matej Hakoš <492968@mail.muni.cz>
 */
public class HTMLParserTest {

	@Test
	public void checkParserAPI() {
		System.out.println("HTMLParserTest.checkParserAPI");

		HTMLParser parser = new HTMLParser();

		assertTrue(parser.isInputValid());
		assertEquals(parser.getMessage(), "");
		assertEquals(parser.getRawInput(), "");
		assertEquals(parser.getEscapedHTML(), "");

		parser.sanitizeHTML("<p>Hello</p>").checkEscapedHTML();
		assertTrue(parser.isInputValid());
		assertEquals(parser.getMessage(), "");
		assertEquals(parser.getRawInput(), "<p>Hello</p>");
		assertEquals(parser.getEscapedHTML(), "<p>Hello</p>");

		parser.sanitizeHTML("<p>Hello</p><script>alert('I AM UNSAFE!');</script>").checkEscapedHTML();
		assertFalse(parser.isInputValid());
		assertNotEquals(parser.getMessage(), "");
		assertEquals(parser.getRawInput(), "<p>Hello</p><script>alert('I AM UNSAFE!');</script>");
		assertEquals(parser.getEscapedHTML(), "<p>Hello</p>");
	}

	@Test
	public void checkUnsafeHtmlInput() {
		System.out.println("HTMLParserTest.checkUnsafeHtmlInput");

		String unsafeInput = """
			<style>
			.helo {
			  background-color:blue;
			  color:red;
			}
			</style>
			<script>
			alert("I AM UNSAFE!");
			</script>
			<p class="helo">HELLO !</p>
			<p non-existing-attr style="background-color:blue; background: url(''); color:red;">Hello 2!</p>""";

		HTMLParser parser = new HTMLParser()
			.sanitizeHTML(unsafeInput)
			.checkEscapedHTML();

		assertFalse(parser.isInputValid());
		String[] unsafeTags = parser.getEscaped();
		assertEquals(unsafeTags[0], "style, script");
		assertEquals(unsafeTags[1], "[non-existing-attr] in p");
		assertEquals(unsafeTags[2], "background");
	}

	@Test
	public void checkSafeHtmlInput() {
		System.out.println("HTMLParserTest.checkSafeHtmlInput");

		String input = """
			<hr>
			<p class="helo">HELLO !</p>
			<p style="background-color:blue; color:red;">Hello 2!</p>
			<a href="https://www.example.eu">https://www.example.eu</a>
			<a href="mailto:contact@email.eu">contact@email.eu</a>
			""";

		HTMLParser parser = new HTMLParser()
            .sanitizeHTML(input)
            .checkEscapedHTML();
		assertTrue(parser.isInputValid());
		assertEquals(parser.getMessage(), "");
	}
}
