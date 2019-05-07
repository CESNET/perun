package cz.metacentrum.perun.taskslib.dao.jdbc;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class TaskResultDaoJdbcTest {

	private TaskResultDaoJdbc taskResultDaoJdbc;

	private static final byte STARTING_BYTE_OF_4_BYTE_CHAR = (byte)0b11110000;
	private static final byte STARTING_BYTE_OF_3_BYTE_CHAR = (byte)0b11100000;
	private static final byte STARTING_BYTE_OF_2_BYTE_CHAR = (byte)0b11000000;
	private static final byte STARTING_BYTE_OF_1_BYTE_CHAR = (byte)0b01000000;
	private static final byte NOT_STARTING_BYTE = (byte)0b10000000;

	@Before
	public void setUp() {
		taskResultDaoJdbc = new TaskResultDaoJdbc();
	}

	@Test
	public void testIsAStartingByteUTF8CharWithStartingByteOf4ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc(
			"isAStartingByteUTF8Char", byte.class);

		assertTrue((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_4_BYTE_CHAR));
	}

	@Test
	public void testIsAStartingByteUTF8CharWithStartingByteOf3ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc(
			"isAStartingByteUTF8Char", byte.class);

		assertTrue((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_3_BYTE_CHAR));
	}

	@Test
	public void testIsAStartingByteUTF8CharWithStartingByteOf2ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc(
			"isAStartingByteUTF8Char", byte.class);

		assertTrue((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_2_BYTE_CHAR));
	}

	@Test
	public void testIsAStartingByteUTF8CharWithStartingByteOf1ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc(
			"isAStartingByteUTF8Char", byte.class);

		assertTrue((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_1_BYTE_CHAR));
	}

	@Test
	public void testIsAStartingByteUTF8CharWithNotStartingByte() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc(
			"isAStartingByteUTF8Char", byte.class);

		assertFalse((boolean)testedMethod.invoke(taskResultDaoJdbc, NOT_STARTING_BYTE));
	}

	@Test
	public void testIsASingleByteUTF8CharWithSingleByte() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc("isASingleByteUTF8Char", byte.class);

		assertFalse((boolean)testedMethod.invoke(taskResultDaoJdbc, NOT_STARTING_BYTE));
	}

	@Test
	public void testIsASingleByteUTF8CharWithStartingByteOf1ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc("isASingleByteUTF8Char", byte.class);

		assertTrue((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_1_BYTE_CHAR));
	}

	@Test
	public void testIsASingleByteUTF8CharWithStartingByteOf2ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc("isASingleByteUTF8Char", byte.class);

		assertFalse((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_2_BYTE_CHAR));
	}

	@Test
	public void testIsASingleByteUTF8CharWithStartingByteOf3ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc("isASingleByteUTF8Char", byte.class);

		assertFalse((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_3_BYTE_CHAR));
	}

	@Test
	public void testIsASingleByteUTF8CharWithStartingByteOf4ByteChar() throws Exception {
		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc("isASingleByteUTF8Char", byte.class);

		assertFalse((boolean)testedMethod.invoke(taskResultDaoJdbc, STARTING_BYTE_OF_4_BYTE_CHAR));
	}

	@Test
	public void testClearZeroBytesFromStringKeepsAllBytesOfMultiByteUTF8Character() throws Exception {

		String testedString = "PERUNřnřPERUN";
		// this String is made that the cut limit is made in half of the first 'ř' character
		byte[] utf8Bytes = testedString.getBytes(StandardCharsets.UTF_8);

		Method testedMethod = getPrivateMethodFromTaskResultDaoJdbc("clearZeroBytesFromString", byte[].class, int.class);

		// call the method with limit 10 so the first 'ř' character cannot fit
		// (The actual limit is 6, the method decreases the specified limit by 4
		//  if the array is larger. It is done this way to make sure the data are in a correct form.)
		//
		byte[] cutBytes = (byte[])testedMethod.invoke(taskResultDaoJdbc, utf8Bytes, 10);

		String cutString = new String(cutBytes, StandardCharsets.UTF_8);

		assertEquals("PERUNř", cutString);
	}

	/**
	 * Method used to gain access to a private method from the TaskResultDaoJdbc.
	 *
	 * @param methodName name of the accessed method
	 * @param argClasses classes of arguments from the accessed method
	 * @return Reference to the accessed method
	 * @throws Exception if cannot find specified method
	 */
	private Method getPrivateMethodFromTaskResultDaoJdbc(String methodName, Class<?>... argClasses) throws Exception {
		Method method = TaskResultDaoJdbc.class.getDeclaredMethod(methodName, argClasses);
		method.setAccessible(true);
		return method;
	}
}
