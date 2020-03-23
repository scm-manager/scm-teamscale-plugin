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

import com.google.common.io.Resources;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.RestDispatcher;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
class ConfigurationResourceTest {

  Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private ConfigurationService service;

  private ConfigurationResource resource;
  private RestDispatcher dispatcher;

  @BeforeEach
  public void init() {
    initMocks(this);
    resource = new ConfigurationResource(service);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldReturnGlobalConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/teamscale/configuration");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(service).getGlobalConfiguration();
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void shouldUpdateGlobalConfig() throws URISyntaxException, IOException {
    MockHttpRequest request = MockHttpRequest
      .put("/v2/teamscale/configuration")
      .content(readConfigJson("com/cloudogu/scm/resource/globalConfig.json"))
      .contentType(MediaType.APPLICATION_JSON_TYPE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(service).updateGlobalConfiguration(any());
    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  void shouldReturnRepoConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/teamscale/configuration/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(service).getRepositoryConfiguration(REPOSITORY.getNamespace(), REPOSITORY.getName());
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void shouldUpdateRepoConfig() throws URISyntaxException, IOException {
    MockHttpRequest request = MockHttpRequest
      .put("/v2/teamscale/configuration/" + REPOSITORY.getNamespace() + "/" + REPOSITORY.getName())
      .content(readConfigJson("com/cloudogu/scm/resource/repoConfig.json"))
      .contentType(MediaType.APPLICATION_JSON_TYPE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(service).updateRepositoryConfiguration(anyString(), anyString(), any());
    assertThat(response.getStatus()).isEqualTo(204);
  }


  private byte[] readConfigJson(String path) throws IOException {
    URL url = Resources.getResource(path);
    return Resources.toByteArray(url);
  }
}

