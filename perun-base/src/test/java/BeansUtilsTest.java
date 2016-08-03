import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import static org.hamcrest.CoreMatchers.isA;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Michal Krajčovič <mkrajcovic@mail.muni.cz>
 */
public class BeansUtilsTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private String identifier = "table.identifier";

	@Test
	public void prepareInSQLClauseForValuesTestWithValidParameters() {
		String expected = " ( table.identifier in ('8','1','2','3','4','5') ) ";
		List<String> values = Arrays.asList("8", "1", "2", "3", "4", "5");

		try {
			Assert.assertEquals(expected, BeansUtils.prepareInSQLClauseForValues(values, identifier));
		} catch (InternalErrorException e) {
			fail();
		}
	}

	@Test
	public void prepareInSQLClauseForValuesTestWithALotOfValidParameters() {
		List<String> values = new ArrayList<>(2653);
		String expected = getExpectedResultForALotOfValues(values, 2653);
		try {
			Assert.assertEquals(expected, BeansUtils.prepareInSQLClauseForValues(values, identifier));
		} catch (InternalErrorException e) {
			fail();
		}
	}


	@Test
	public void prepareInSQLClauseForValuesTestWithEmptyValues() {
		String expected = " ( table.identifier in () ) ";
		List<String> values = Collections.emptyList();

		try {
			Assert.assertEquals(expected, BeansUtils.prepareInSQLClauseForValues(values, identifier));
		} catch (InternalErrorException e) {
			fail();
		}
	}

	@Test
	public void prepareInSQLClauseForValuesTestWithEmptyIdentifier() throws InternalErrorException {
		expectedException.expectCause(isA(IllegalArgumentException.class));

		String identifier = "";
		List<String> values = Arrays.asList("8", "1", "2", "3", "4", "5");

		BeansUtils.prepareInSQLClauseForValues(values, identifier);
	}

	@Test
	public void prepareInSQLClauseForValuesTestWithNullValues() throws InternalErrorException {
		expectedException.expectCause(isA(NullPointerException.class));

		List<String> values = null;

		BeansUtils.prepareInSQLClauseForValues(values, identifier);
	}

	@Test
	public void prepareInSQLClauseForValuesTestWithNullIdentifier() throws InternalErrorException {
		expectedException.expectCause(isA(NullPointerException.class));

		String identifier = null;
		List<String> values = Arrays.asList("8", "1", "2", "3", "4", "5");

		BeansUtils.prepareInSQLClauseForValues(values, identifier);
	}

	@Test
	public void prepareInSQLClauseForValuesTestWithSomeNullValues() {
		String expected = " ( table.identifier in ('1','2','4','5') ) ";
		List<String> values = Arrays.asList(null, "1", "2", null, "4", "5");

		try {
			Assert.assertEquals(expected, BeansUtils.prepareInSQLClauseForValues(values, identifier));
		} catch (InternalErrorException e) {
			fail();
		}
	}

	@Test
	public void beanValuesToStringTestWithValidValues() {
		String expected = "'8','1','2','3','4','5'";
		List<String> values = Arrays.asList("8", "1", "2", "3", "4", "5");

		try {
			Assert.assertEquals(expected, BeansUtils.beanValuesToString(values));
		} catch (InternalErrorException e) {
			fail();
		}
	}

	@Test
	public void beanValuesToStringTestWithSomeNullValues() {
		String expected = "'8','1','3','5'";
		List<String> values = Arrays.asList("8", "1", null, "3", null, "5");

		try {
			Assert.assertEquals(expected, BeansUtils.beanValuesToString(values));
		} catch (InternalErrorException e) {
			fail();
		}
	}

	@Test
	public void beanValuesToStringTestWithNullValues() throws InternalErrorException {
		expectedException.expectCause(isA(NullPointerException.class));
		List<String> values = null;

		BeansUtils.beanValuesToString(values);
	}

	@Test
	public void beanValuesToStringTestWithSomeEmptyValues() {
		List<String> values = Collections.emptyList();

		try {
			Assert.assertEquals("", BeansUtils.beanValuesToString(values));
		} catch (InternalErrorException e) {
			fail();
		}
	}

	private String getExpectedResultForALotOfValues(List<String> values, int max) {
		int loops = max / 1000;
		String expected = " ( table.identifier in ('0'";
		values.add("0");
		int j = 1;
		for (int i = 0; i < loops; i++) {
			for (; j < (i + 1) * 1000; j++) {
				String current = Integer.toString(j);
				values.add(current);
				expected += ",'" + current + "'";
			}
			expected += ") or table.identifier in ('" + j + "'";
			values.add(Integer.toString(j));
			j++;
		}
		for (; j < max; j++) {
			String current = Integer.toString(j);
			values.add(current);
			expected += ",'" + current + "'";
		}
		expected += ") ) ";
		return expected;
	}
}
