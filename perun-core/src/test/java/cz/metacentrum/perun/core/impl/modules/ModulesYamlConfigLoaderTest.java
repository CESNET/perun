package cz.metacentrum.perun.core.impl.modules;

import cz.metacentrum.perun.core.api.exceptions.rt.ModulePropertyNotFoundException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ModulesYamlConfigLoaderTest {

  private static final String CONFIG_FILE = "test-module-config";
  private static final String NULL_PROPERTY = "null";
  private static final String NOT_EXISTING_PROPERTY = "not-existing";

  private final ModulesYamlConfigLoader loader = new ModulesYamlConfigLoader("src/test/resources/");

  @Test
  public void testLoadStringProperty() {
    String value = loader.loadString(CONFIG_FILE, "string");
    assertThat(value).isEqualTo("John Doe");
  }

  @Test
  public void testLoadNullStringThrowsError() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadString(CONFIG_FILE, NULL_PROPERTY));
  }

  @Test
  public void testLoadNotExistingStringThrowsException() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadInteger(CONFIG_FILE, NULL_PROPERTY));
  }

  @Test
  public void testLoadStringPropertyReturnsValueIfFound() {
    String defaultValue = "DEFAULT";
    String value = loader.loadStringOrDefault(CONFIG_FILE, "string", defaultValue);
    assertThat(value).isEqualTo("John Doe");
  }

  @Test
  public void testLoadStringPropertyReturnsDefaultIfNotFound() {
    String defaultValue = "DEFAULT";
    String value = loader.loadStringOrDefault(CONFIG_FILE, NOT_EXISTING_PROPERTY, defaultValue);
    assertThat(value).isEqualTo(defaultValue);
  }

  @Test
  public void testLoadStringPropertyReturnsDefaultIfNullValueFound() {
    String defaultValue = "DEFAULT";
    String value = loader.loadStringOrDefault(CONFIG_FILE, NULL_PROPERTY, defaultValue);
    assertThat(value).isEqualTo(defaultValue);
  }

  @Test
  public void testLoadIntegerProperty() {
    Integer value = loader.loadInteger(CONFIG_FILE, "integer");
    assertThat(value).isEqualTo(1);
  }

  @Test
  public void testLoadNullIntegerThrowsError() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadInteger(CONFIG_FILE, NULL_PROPERTY));
  }

  @Test
  public void testLoadNotExistingIntegerThrowsException() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadInteger(CONFIG_FILE, NOT_EXISTING_PROPERTY));
  }

  @Test
  public void testLoadIntegerPropertyReturnsValueIfFound() {
    Integer defaultValue = 42;
    Integer value = loader.loadIntegerOrDefault(CONFIG_FILE, "integer", defaultValue);
    assertThat(value).isEqualTo(1);
  }

  @Test
  public void testLoadIntegerPropertyReturnsDefaultIfNotFound() {
    Integer defaultValue = 42;
    Integer value = loader.loadIntegerOrDefault(CONFIG_FILE, NOT_EXISTING_PROPERTY, defaultValue);
    assertThat(value).isEqualTo(defaultValue);
  }

  @Test
  public void testLoadIntegerPropertyReturnsDefaultIfNullValueFound() {
    Integer defaultValue = 42;
    Integer value = loader.loadIntegerOrDefault(CONFIG_FILE, NULL_PROPERTY, defaultValue);
    assertThat(value).isEqualTo(defaultValue);
  }

  @Test
  public void testLoadStringListProperty() {
    List<String> values = loader.loadStringList(CONFIG_FILE, "stringList");
    assertThat(values).containsExactly("one", "two");
  }

  @Test
  public void testLoadNullStringListThrowsException() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadStringList(CONFIG_FILE, NULL_PROPERTY));
  }

  @Test
  public void testLoadNotExistingStringListThrowsException() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadStringList(CONFIG_FILE, NOT_EXISTING_PROPERTY));
  }

  @Test
  public void testLoadStringListPropertyReturnsValueIfFound() {
    List<String> defaultValue = new ArrayList<>();
    List<String> values = loader.loadStringListOrDefault(CONFIG_FILE, "stringList", defaultValue);
    assertThat(values).containsExactly("one", "two");
  }

  @Test
  public void testLoadStringListPropertyReturnsDefaultIfNotFound() {
    List<String> defaultValue = new ArrayList<>();
    List<String> values = loader.loadStringListOrDefault(CONFIG_FILE, NOT_EXISTING_PROPERTY, defaultValue);
    assertThat(values).isEqualTo(defaultValue);
  }

  @Test
  public void testLoadStringListPropertyReturnsDefaultIfNullValueFound() {
    List<String> defaultValue = new ArrayList<>();
    List<String> values = loader.loadStringListOrDefault(CONFIG_FILE, NULL_PROPERTY, defaultValue);
    assertThat(values).isEqualTo(defaultValue);
  }

  @Test
  public void testLoadIntegerListProperty() {
    List<Integer> values = loader.loadIntegerList(CONFIG_FILE, "integerList");
    assertThat(values).containsExactly(1, 2);
  }

  @Test
  public void testLoadNullIntegerListThrowsException() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadIntegerList(CONFIG_FILE, NULL_PROPERTY));
  }

  @Test
  public void testLoadNotExistingIntegerListThrowsException() {
    assertThatExceptionOfType(ModulePropertyNotFoundException.class)
        .isThrownBy(() -> loader.loadIntegerList(CONFIG_FILE, NOT_EXISTING_PROPERTY));
  }

  @Test
  public void testLoadIntegerListPropertyReturnsValueIfFound() {
    List<Integer> defaultValues = new ArrayList<>();
    List<Integer> values = loader.loadIntegerListOrDefault(CONFIG_FILE, "integerList", defaultValues);
    assertThat(values).containsExactly(1, 2);
  }

  @Test
  public void testLoadIntegerListPropertyReturnsDefaultIfNotFound() {
    List<Integer> defaultValues = new ArrayList<>();
    List<Integer> values = loader.loadIntegerListOrDefault(CONFIG_FILE, NOT_EXISTING_PROPERTY, defaultValues);
    assertThat(values).isEqualTo(defaultValues);
  }

  @Test
  public void testLoadIntegerListPropertyReturnsDefaultIfNullValueFound() {
    List<Integer> defaultValues = new ArrayList<>();
    List<Integer> values = loader.loadIntegerListOrDefault(CONFIG_FILE, NULL_PROPERTY, defaultValues);
    assertThat(values).isEqualTo(defaultValues);
  }

  @Test
  public void testParseInnerElement() {
    List<Integer> values = loader.loadIntegerList(CONFIG_FILE, "innerIntegerList.list");
    assertThat(values).containsExactly(1, 2, 3);
  }
}
