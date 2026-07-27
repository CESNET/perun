package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PerunAppsConfigLoader {

  private Resource configPath;

  public void initialize() {
    YAMLMapper mapper = YAMLMapper.builder().build();
    try (InputStream is = configPath.getInputStream()) {
      PerunAppsConfig.setInstance(mapper.readValue(is, PerunAppsConfig.class));
    } catch (JacksonException e) {
      throw new InternalErrorException("Configuration file for perun apps has invalid format.", e);
    } catch (IOException e) {
      throw new InternalErrorException("Configuration file not found for perun apps. It should be in: " + configPath,
          e);
    }
  }

  public void setConfigPath(Resource configPath) {
    this.configPath = configPath;
  }
}
