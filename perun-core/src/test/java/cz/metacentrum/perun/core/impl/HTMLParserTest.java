package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.BeansUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * @author: Matej Hakoš <492968@mail.muni.cz>
 */
public class HTMLParserTest extends AbstractPerunIntegrationTest {
  private boolean originalForce;

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

    parser = new HTMLParser();
    parser.sanitizeHTML("<p>Hello</p><script>alert('I AM UNSAFE!');</script>").checkEscapedHTML();
    assertFalse(parser.isInputValid());
    assertNotEquals(parser.getMessage(), "");
    assertEquals(parser.getRawInput(), "<p>Hello</p><script>alert('I AM UNSAFE!');</script>");
    assertEquals(parser.getEscapedHTML(), "<p>Hello</p>");
  }

  @Test
  public void checkSafeCheckboxLabel() {
    System.out.println("HTMLParserTest.checkSafeCheckboxLabel");

    // this is the format in which options are stored, potentially parse before sanitizing if this causes issues
    String input = "testVal#I have read <a href=\"https://www.example.eu\">these terms</a>|f#b";

    assertTrue(new HTMLParser().isCheckboxLabelSafe(input));
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

    HTMLParser parser = new HTMLParser().sanitizeHTML(input).checkEscapedHTML();
    assertTrue(parser.isInputValid());
    assertEquals(parser.getMessage(), "");
  }

  @Test
  public void checkUnsafeCheckboxLabel() {
    System.out.println("HTMLParserTest.checkUnsafeCheckboxLabel");

    // this is the format in which options are stored, potentially parse before sanitizing if this causes issues
    String input = "testVal#I have read <a>these terms</a>|f#<script>alert(\"I AM UNSAFE!\")</script>";

    assertFalse(new HTMLParser().isCheckboxLabelSafe(input));
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

    HTMLParser parser = new HTMLParser().sanitizeHTML(unsafeInput).checkEscapedHTML();

    assertFalse(parser.isInputValid());
    String[] unsafeTags = parser.getEscaped();
    assertEquals(unsafeTags[0], "style, script");
    assertEquals(unsafeTags[1], "p [non-existing-attr]");
    assertEquals(unsafeTags[2], "background");
  }

  @After
  public void revertForce() {
    BeansUtils.getCoreConfig().setForceHtmlSanitization(originalForce);
  }

  @Before
  public void setForce() {
    originalForce = BeansUtils.getCoreConfig().getForceHtmlSanitization();
    BeansUtils.getCoreConfig().setForceHtmlSanitization(true);
  }
}
