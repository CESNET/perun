package cz.metacentrum.perun;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utils class for tests.
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class TestUtils {

	/**
	 * Interface used to define a function that can throw a checked exception.
	 * The exception is parsed in a RuntimeException and thrown.
	 *
	 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
	 */
	@FunctionalInterface
	public interface TestFunction<T, R> extends Function<T, R> {

		@Override
		default R apply(T t) {
			try {
				return applyThrows(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		R applyThrows(final T t) throws Exception;
	}

	/**
	 * Interface used to define a supplier that can throw a checked exception.
	 * The exception is parsed in a RuntimeException and thrown.
	 *
	 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
	 */
	@FunctionalInterface
	public interface TestSupplier<T> extends Supplier<T> {

		@Override
		default T get() {
			try {
				return getThrows();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		T getThrows() throws Exception;
	}

	/**
	 * Interface used to define a consumer that can throw a checked exception.
	 * The exception is parsed in a RuntimeException and thrown.
	 * @param <T>
	 */
	@FunctionalInterface
	public interface TestConsumer<T> extends Consumer<T> {

		@Override
		default void accept(final T elem) {
			try {
				acceptThrows(elem);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}

		void acceptThrows(T elem) throws Exception;
	}

}
