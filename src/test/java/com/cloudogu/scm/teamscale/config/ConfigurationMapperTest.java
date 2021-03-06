/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.scm.teamscale.config;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  private URI expectedBaseUri;

  @Mock
  Subject subject;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  ConfigurationMapperImpl mapper;

  @BeforeEach
  void init() {
    lenient().when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve("v2/teamscale/configuration/");
  }

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithAuthorization {

    @BeforeEach
    void setPermission() {
      lenient().when(subject.isPermitted("repository:teamscale:42")).thenReturn(true);
    }

    @Test
    void shouldMapAttributesToDto() {
      ConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
      assertThat("heartofgo.ld").isEqualTo(dto.getUrl());
    }

    @Test
    void shouldAddHalLinksToDto() {
      ConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
      assertThat(expectedBaseUri.toString() + "foo/bar").isEqualTo(dto.getLinks().getLinkBy("self").get().getHref());
      assertThat(expectedBaseUri.toString() + "foo/bar").isEqualTo(dto.getLinks().getLinkBy("update").get().getHref());
    }

    @Test
    void shouldNotAppendUpdateLinkIfNotPermitted() {
      when(subject.isPermitted("repository:teamscale:42")).thenReturn(false);
      ConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
      assertThat(expectedBaseUri.toString() + "foo/bar").isEqualTo(dto.getLinks().getLinkBy("self").get().getHref());
      assertThat(dto.getLinks().getLinkBy("update").isPresent()).isFalse();
    }

    @Test
    void shouldMapAttributesFromDto() {
      Configuration configuration = mapper.map(createDto());
      assertThat("heartofgo.ld").isEqualTo(configuration.getUrl());
    }
  }
    @Test
    void shouldMapGlobalConfigurationAttributesToDto() {
      GlobalConfigurationDto dto = mapper.map(createGlobalConfiguration());
      assertThat(dto.isDisableRepositoryConfiguration()).isFalse();
    }

    @Test
    void shouldMapGlobalConfigurationAttributesFromDto() {
      GlobalConfiguration configuration = mapper.map(createGlobalConfigurationDto());
      assertThat(configuration.isDisableRepositoryConfiguration()).isFalse();
    }

    @Test
    void shouldNotAddUpdateLinkToDtoIfNotPermitted() {
      ConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
      assertThat(dto.getLinks().getLinkBy("update").isPresent()).isFalse();
    }


  private Configuration createConfiguration() {
    return new Configuration("heartofgo.ld");
  }

  private ConfigurationDto createDto() {
    return new ConfigurationDto("heartofgo.ld");
  }

  private GlobalConfiguration createGlobalConfiguration() {
    GlobalConfiguration configuration = new GlobalConfiguration();
    configuration.setUrl("");
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private GlobalConfigurationDto createGlobalConfigurationDto() {
    GlobalConfigurationDto configuration = new GlobalConfigurationDto();
    configuration.setUrl("");
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private Repository createRepository() {
    return new Repository("42", "GIT", "foo", "bar");
  }
}
