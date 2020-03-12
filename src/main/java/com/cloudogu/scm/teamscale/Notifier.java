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

  private static final String PUSH_EVENT = "SCM-Push-Event";

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

  public void notifyWithBranch(Repository repository, String branchName) {
    Notification notification = createNotificationWithBranch(repository, branchName);
    notifyViaHttp(repository, notification);
  }

  public void notifyWithoutBranch(Repository repository) {
    Notification notification = createNotification(repository);
    notifyViaHttp(repository, notification);
  }

  private void notifyViaHttp(Repository repository, Notification notification) {
    Optional<Configuration> configuration = configurationProvider.evaluateConfiguration(repository);
    configuration.ifPresent(config -> notifyViaHttp(config, notification));
  }

  private void notifyViaHttp(Configuration configuration, Notification notification) {
    AdvancedHttpClient client = httpClientProvider.get();
    try {
      client
        .post(createTeamscaleHookUrl(configuration.getUrl()))
        .jsonContent(notification)
        .header("X-SCM-Event", PUSH_EVENT)
        .request();
    } catch (IOException e) {
      LOG.warn("Could not notify teamscale instance with url: {}", configuration.getUrl());
    }
  }

  private String createTeamscaleHookUrl(String url) {
    return HttpUtil.append(url, "scm-manager-hook");
  }

  private Notification createNotification(Repository repository) {
    String repositoryUrl = String.format("%s/repo/%s/%s", scmConfiguration.getBaseUrl(), repository.getNamespace(), repository.getName());
    String repositoryId = repository.getNamespace() + "/" + repository.getName();
    return new Notification(repositoryUrl, repositoryId);
  }

  private Notification createNotificationWithBranch(Repository repository, String branchName) {
    Notification notification = createNotification(repository);
    return new Notification(notification.getRepositoryUrl(), notification.getRepositoryId(), branchName);
  }
}
