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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.Repository;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

public class Notifier {

  private static final Logger LOG = LoggerFactory.getLogger(Notifier.class);

  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final ScmConfiguration scmConfiguration;
  private final ConfigurationProvider configurationProvider;

  @Inject
  public Notifier(Provider<AdvancedHttpClient> httpClientProvider, ScmConfiguration scmConfiguration, ConfigurationProvider configurationProvider) {
    this.httpClientProvider = httpClientProvider;
    this.scmConfiguration = scmConfiguration;
    this.configurationProvider = configurationProvider;
  }

  public String createRepositoryId(Repository repository) {
    return repository.getNamespace() + "/" + repository.getName();
  }

  public String createRepositoryUrl(Repository repository) {
    return String.format("%s/repo/%s/%s", scmConfiguration.getBaseUrl(), repository.getNamespace(), repository.getName());
  }

  public void notifyViaHttp(Repository repository, Notification notification, String eventType) {
    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(repository);
    configuration.ifPresent(config -> notifyViaHttp(config, notification, eventType));
  }

  public void notifyViaHttp(Configuration configuration, Notification notification, String eventType) {
    AdvancedHttpClient client = httpClientProvider.get();
    try {
      client
        .post(createTeamscaleHookUrl(configuration.getUrl()))
        .jsonContent(notification)
        .header("X-SCM-Event", eventType)
        .request();
    } catch (IOException e) {
      LOG.warn("Could not notify teamscale instance with url: {}", configuration.getUrl());
    }
  }

  private String createTeamscaleHookUrl(String url) {
    return HttpUtil.append(url, "scm-manager-hook");
  }
}
