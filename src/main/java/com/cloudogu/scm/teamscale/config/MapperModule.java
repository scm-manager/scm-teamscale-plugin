package com.cloudogu.scm.teamscale.config;

import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.plugin.Extension;

@Extension
public class MapperModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ConfigurationMapper.class).to(Mappers.getMapper(ConfigurationMapper.class).getClass());
  }
}
