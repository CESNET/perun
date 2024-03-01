package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PerunAppsConfigLoader {

  private Resource configPath;

  public void setConfigPath(Resource configPath) {
    this.configPath = configPath;
  }

  public void initialize() {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try (InputStream is = configPath.getInputStream()) {
      PerunAppsConfig.setInstance(mapper.readValue(is, PerunAppsConfig.class));
    } catch (JsonProcessingException e) {
      throw new InternalErrorException("Configuration file for perun apps has invalid format.", e);
    } catch (IOException e) {
      throw new InternalErrorException("Configuration file not found for perun apps. It should be in: " + configPath,
          e);
    }
  }
}
