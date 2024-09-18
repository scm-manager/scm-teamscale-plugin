/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

import jakarta.inject.Inject;
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

  public boolean isTeamscaleConfigured(Repository repository) {
    return configurationProvider.evaluateConfiguration(repository).isPresent();
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
        .spanKind("Teamscale")
        .jsonContent(notification)
        .header("X-SCM-Event", eventType)
        .request();
    } catch (IOException e) {
      LOG.warn("Could not notify teamscale instance with url: {}", configuration.getUrl(), e);
    }
  }

  private String createTeamscaleHookUrl(String url) {
    return HttpUtil.append(url, "api/scm-manager/web-hook");
  }
}
