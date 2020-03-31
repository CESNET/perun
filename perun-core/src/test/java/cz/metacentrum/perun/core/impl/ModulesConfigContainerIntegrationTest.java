package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ModulesConfigContainer;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ModulesConfigContainerIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = "ModulesConfigContainer.";
	private static final String fileName = "TestConfiguration";
	private static final String emptyProperty = "EmptyProperty";
	private static final String stringProperty = "StringProperty";
	private static final String integerProperty = "IntegerProperty";
	private static final String listOfStringsProperty = "ListOfStringsProperty";
	private static final String listOfIntegersProperty = "ListOfIntegersProperty";
	private static final String stringPropertyValue = "Test string";
	private static final Integer integerPropertyValue = 123;
	private static final List<String> listOfStringsPropertyValue = Arrays.asList("One", "Two", "Three");
	private static final List<Integer> listOfIntegersPropertyValue = Arrays.asList(1, 2, 3);

	@Test(expected= InternalErrorException.class)
	public void fetchNotExistingProperty() {
		System.out.println(CLASS_NAME + "fetchNotExistingProperty");

		ModulesConfigContainer.getInstance().fetchPropertyAsString(fileName, "NotExistingProperty");
	}

	@Test(expected= InternalErrorException.class)
	public void fetchPropertyFromNotExistingFile() {
		System.out.println(CLASS_NAME + "fetchPropertyFromNotExistingFile");

		ModulesConfigContainer.getInstance().fetchPropertyAsString("NotExistingFile", stringProperty);
	}

	@Test(expected= InternalErrorException.class)
	public void fetchEmptyProperty() {
		System.out.println(CLASS_NAME + "fetchPropertyFromNotExistingFile");

		ModulesConfigContainer.getInstance().fetchPropertyAsString(fileName, emptyProperty);
	}

	@Test
	public void fetchPropertyAsString() {
		System.out.println(CLASS_NAME + "fetchPropertyAsString");

		String propertyValue = ModulesConfigContainer.getInstance().fetchPropertyAsString(fileName, stringProperty);

		assertEquals(propertyValue, stringPropertyValue);
	}

	@Test
	public void fetchPropertyAsInteger() {
		System.out.println(CLASS_NAME + "fetchPropertyAsInteger");

		Integer propertyValue = ModulesConfigContainer.getInstance().fetchPropertyAsInteger(fileName, integerProperty);

		assertSame(propertyValue, integerPropertyValue);
	}

	@Test(expected= InternalErrorException.class)
	public void fetchPropertyAsIntegerBadFormat() {
		System.out.println(CLASS_NAME + "fetchPropertyAsIntegerBadFormat");

		ModulesConfigContainer.getInstance().fetchPropertyAsInteger(fileName, stringProperty);
	}

	@Test
	public void fetchPropertyAsListOfStrings() {
		System.out.println(CLASS_NAME + "fetchPropertyAsListOfStrings");

		List<String> propertyValue = ModulesConfigContainer.getInstance().fetchPropertyAsListOfStrings(fileName, listOfStringsProperty);

		assertEquals(propertyValue, listOfStringsPropertyValue);
	}

	@Test
	public void fetchPropertyAsListOfIntegers() {
		System.out.println(CLASS_NAME + "fetchPropertyAsListOfIntegers");

		List<Integer> propertyValue = ModulesConfigContainer.getInstance().fetchPropertyAsListOfIntegers(fileName, listOfIntegersProperty);

		assertEquals(propertyValue, listOfIntegersPropertyValue);
	}

	@Test(expected= InternalErrorException.class)
	public void fetchPropertyAsListOfIntegersBadFormat() {
		System.out.println(CLASS_NAME + "fetchPropertyAsListOfIntegersBadFormat");

		ModulesConfigContainer.getInstance().fetchPropertyAsListOfIntegers(fileName, listOfStringsProperty);
	}
}
