package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Tests of methods from Utils class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class UtilsIntegrationTest {

	@Before
	public void setUp() {
	}

	@Test
	public void parseCommonNameTest() {
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

		Map<String,String> parsedRawName;

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

	@Test
	public void extendDateByPeriod() throws InternalErrorException {
		System.out.println("Utils.extendDateByPeriod");
		LocalDate localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1d");
		assertEquals(LocalDate.of(2019, 2, 9), localDate);

		localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1m");
		assertEquals(LocalDate.of(2019, 3, 8), localDate);

		localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1y");
		assertEquals(LocalDate.of(2020, 2, 8), localDate);
	}

	@Test(expected = InternalErrorException.class)
	public void extendDateByPeriodInBadFormat() throws InternalErrorException {
		System.out.println("Utils.extendDateByPeriodInBadFormat");
		LocalDate localDate = Utils.extendDateByPeriod(LocalDate.of(2019,2,8), "+1day");
	}

	@Test
	public void extendDateByStaticDate() {
		System.out.println("Utils.extendDateByStaticDate");
		String period = "1.1.";
		Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
		Matcher m = p.matcher(period);
		m.matches();
		LocalDate localDate = Utils.extendDateByStaticDate(LocalDate.of(2019, 2, 9), m);
		assertEquals(LocalDate.of(2020, 1, 1), localDate);
	}

	@Test
	public void prepareGracePeriodDate() throws InternalErrorException {
		System.out.println("Utils.prepareGracePeriodDate");
		String gracePeriod = "5d";
		Pattern p = Pattern.compile("([0-9]+)([dmy]?)");
		Matcher m = p.matcher(gracePeriod);
		Pair<Integer, TemporalUnit> fieldAmount = Utils.prepareGracePeriodDate(m);
		assertEquals(new Integer(5), fieldAmount.getLeft());
		assertEquals(ChronoUnit.DAYS, fieldAmount.getRight());
	}
}
