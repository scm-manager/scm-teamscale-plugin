package com.cloudogu.scm.teamscale.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GlobalConfigurationDto extends ConfigurationDto {
  private boolean disableRepositoryConfiguration;
}
