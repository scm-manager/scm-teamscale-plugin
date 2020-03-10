package com.cloudogu.scm.teamscale.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.Validateable;
import sonia.scm.util.Util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "teamscaleConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration implements Validateable {

  private String url;

  @Override
  public boolean isValid() {
    return Util.isNotEmpty(url);
  }

}
