package com.cloudogu.scm.teamscale.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GlobalConfigurationDto extends ConfigurationDto {
  private boolean disableRepositoryConfiguration;
}
