package com.cloudogu.scm.teamscale;

import com.cloudogu.scm.teamscale.config.ConfigStore;
import com.cloudogu.scm.teamscale.config.Configuration;
import com.cloudogu.scm.teamscale.config.GlobalConfiguration;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConfigurationException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookFeature;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class Notifier {

  private static final String PUSH_EVENT = "SCM-Push-Event";

  private static final Logger LOG = LoggerFactory.getLogger(Notifier.class);

  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final ConfigStore configStore;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public Notifier(Provider<AdvancedHttpClient> httpClientProvider, ConfigStore configStore, ScmConfiguration scmConfiguration) {
    this.httpClientProvider = httpClientProvider;
    this.configStore = configStore;
    this.scmConfiguration = scmConfiguration;
  }

  public void sendCommitNotification(PostReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();
    Configuration configuration = evaluateConfiguration(event);

    if (event.getContext().isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
      for (String branchName : getBranchesFromEvent(event)) {
        notifyWithBranch(configuration, repository, branchName);
      }
    } else {
      notifyWithoutBranch(configuration, repository);
    }
  }

  private List<String> getBranchesFromEvent(PostReceiveRepositoryHookEvent event) {
    return event.getContext().getBranchProvider().getCreatedOrModified();
  }

  private Configuration evaluateConfiguration(PostReceiveRepositoryHookEvent event) {
    GlobalConfiguration globalConfig = configStore.getGlobalConfiguration();
    Configuration repoConfig = configStore.getConfiguration(event.getRepository());

    if (!globalConfig.isDisableRepositoryConfiguration() && repoConfig.isValid()) {
      return repoConfig;
    } else if (globalConfig.isValid()) {
      return globalConfig;
    } else {
      throw new ConfigurationException("Could not send push notification to teamscale since no valid configuration exists.");
    }
  }

  private void notifyWithBranch(Configuration config, Repository repository, String branchName) {
    Notification notification = createNotificationWithBranch(repository, branchName);
    notifyViaHttp(config, notification);
  }

  private void notifyWithoutBranch(Configuration config, Repository repository) {
    Notification notification = createNotification(repository);
    notifyViaHttp(config, notification);
  }

  private void notifyViaHttp(Configuration configuration, Notification notification) {
    AdvancedHttpClient client = httpClientProvider.get();
    try {
      client
        .post(createTeamscaleHookUrl(configuration.getUrl()))
        .basicAuth(configuration.getUsername(), configuration.getPassword())
        .jsonContent(notification)
        .header("X-SCM-Event", PUSH_EVENT)
        .request();
    } catch (IOException e) {
      LOG.error("Could not notify teamscale instance with url: {}", configuration.getUrl());
    }
  }

  private String createTeamscaleHookUrl(String url) {
    return url + "/scm-manager-hook";
  }

  private Notification createNotification(Repository repository) {
    Notification notification = new Notification();
    String repositoryUrl = String.format("%s/repo/%s/%s", scmConfiguration.getBaseUrl(), repository.getNamespace(), repository.getName());
    notification.setRepositoryUrl(repositoryUrl);
    notification.setRepositoryId(repository.getNamespace() + "/" + repository.getName());
    return notification;
  }

  private Notification createNotificationWithBranch(Repository repository, String branchName) {
    Notification notification = createNotification(repository);
    notification.setBranchName(branchName);
    return notification;
  }
}
