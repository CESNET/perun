package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests of methods from Utils class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class UtilsIntegrationTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void parseCommonNameTest() throws Exception {
		System.out.println("Utils.parseCommonName");

		String titleBeforeString = "titleBefore";
		String titleAfterString = "titleAfter";
		String firstNameString = "firstName";
		String lastNameString = "lastName";

		String text1 = "RNDr. David Kurka Ph.D.";
		String text2 = "RNDr. David Kurka Ph. D.";
		String text3 = "Mgr. David Něco Kurka CSc. Ph.D.";
		String text4 = "David Kurka CSc. Ph.D.";
		String text5 = "Mgr. et Mgr. David Kurka Ph.D.";
		String text6 = "David Kurka CSc.";
		String text7 = "David Kurka";
		String text8 = "Dr. David Kurka";
		String text9 = "Ali H. Reshak Al-jaary";
		String text10 = "Ali H. Reshak Al-jaary CSc.";
		String text11 = "prof. Ali H. Reshak Al-jaary";
		String text12 = "RNDr. Petr Kulhánek, PhD.";
		String text13 = "prof. RNDr. David Kurka, CSc.";
		String text14 = "prof.,RNDr. David  Kurka_mladsi, CSc.";

		Map<String,String> parsedRawName = new HashMap<>();

		parsedRawName = Utils.parseCommonName(text1);
		assertEquals("RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text2);
		assertEquals("RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("Ph. D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text3);
		assertEquals("Mgr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Něco Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc. Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text4);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc. Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text5);
		assertEquals("Mgr. et Mgr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("Ph.D.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text6);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text7);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text8);
		assertEquals("Dr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text9);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("Ali", parsedRawName.get(firstNameString));
		assertEquals("H. Reshak Al-jaary", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text10);
		assertEquals(null, parsedRawName.get(titleBeforeString));
		assertEquals("Ali", parsedRawName.get(firstNameString));
		assertEquals("H. Reshak Al-jaary", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text11);
		assertEquals("prof.", parsedRawName.get(titleBeforeString));
		assertEquals("Ali", parsedRawName.get(firstNameString));
		assertEquals("H. Reshak Al-jaary", parsedRawName.get(lastNameString));
		assertEquals(null, parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text12);
		assertEquals("RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("Petr", parsedRawName.get(firstNameString));
		assertEquals("Kulhánek", parsedRawName.get(lastNameString));
		assertEquals("PhD.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text13);
		assertEquals("prof. RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));

		parsedRawName = Utils.parseCommonName(text14);
		assertEquals("prof. RNDr.", parsedRawName.get(titleBeforeString));
		assertEquals("David", parsedRawName.get(firstNameString));
		assertEquals("Kurka mladsi", parsedRawName.get(lastNameString));
		assertEquals("CSc.", parsedRawName.get(titleAfterString));
	}

}
