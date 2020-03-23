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
package com.cloudogu.scm.teamscale;

import com.cloudogu.scm.teamscale.config.Configuration;
import com.cloudogu.scm.teamscale.config.ConfigurationProvider;
import com.google.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifierTest {

  private Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private String SCM_BASE_URL = "http://www.scm-manager.org/scm";
  private String TEAMSCALE_INSTANCE_URL = "http://teamscale.scm-manager.org";

  @Mock
  private Provider<AdvancedHttpClient> httpClientProvider;
  @Mock
  private AdvancedHttpClient httpClient;
  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody body;
  @Mock
  private ScmConfiguration scmConfiguration;
  @Mock
  private ConfigurationProvider configurationProvider;

  @Captor
  private ArgumentCaptor<Notification> jsonCaptor;

  @InjectMocks
  private Notifier notifier;

  @BeforeEach
  void initConfiguration() {
    lenient().when(scmConfiguration.getBaseUrl()).thenReturn("http://www.scm-manager.org/scm");
  }

  @BeforeEach
  void initHttpClient() {
    lenient().when(httpClientProvider.get()).thenReturn(httpClient);
    lenient().when(httpClient.post(any())).thenReturn(body);
    lenient().when(body.jsonContent(jsonCaptor.capture())).thenReturn(body);
  }

  @Test
  void shouldSendNotificationWithBranchName() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEAMSCALE_INSTANCE_URL));

    String branchName = "feature/awesome";
    String nameSpaceAndName = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();

    notifier.notifyWithBranch(REPOSITORY, branchName);

    Notification jsonValue = jsonCaptor.getValue();
    assertThat(jsonValue.getBranchName()).isEqualTo(branchName);
    assertThat(jsonValue.getRepositoryUrl()).isEqualTo(SCM_BASE_URL + "/repo/" + nameSpaceAndName);
    assertThat(jsonValue.getRepositoryId()).isEqualTo(nameSpaceAndName);
  }

  @Test
  void shouldSendNotificationWithoutBranchname() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(createConfiguration(TEAMSCALE_INSTANCE_URL));

    String nameSpaceAndName = REPOSITORY.getNamespace() + "/" + REPOSITORY.getName();

    notifier.notifyWithoutBranch(REPOSITORY);

    Notification jsonValue = jsonCaptor.getValue();
    assertThat(jsonValue.getRepositoryUrl()).isEqualTo(SCM_BASE_URL + "/repo/" + nameSpaceAndName);
    assertThat(jsonValue.getRepositoryId()).isEqualTo(nameSpaceAndName);
  }

  @Test
  void shouldNotSendNotificationIfNoValidConfigExist() {
    when(configurationProvider.evaluateConfiguration(REPOSITORY)).thenReturn(Optional.empty());

    notifier.notifyWithoutBranch(REPOSITORY);

    verify(httpClient, never()).post(anyString());
  }

  private Optional<Configuration> createConfiguration(String url) {
    return Optional.of(new Configuration(url));
  }
}
