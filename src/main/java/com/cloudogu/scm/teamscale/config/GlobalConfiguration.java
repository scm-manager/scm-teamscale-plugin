package com.cloudogu.scm.teamscale.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "teamscaleGlobalConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalConfiguration extends Configuration {
  private boolean disableRepositoryConfiguration;

  public GlobalConfiguration(String url, String username, String password, boolean disableRepositoryConfiguration) {
    super(url, username, password);
    this.disableRepositoryConfiguration = disableRepositoryConfiguration;
  }
}
