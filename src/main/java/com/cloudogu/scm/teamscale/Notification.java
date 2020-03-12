package com.cloudogu.scm.teamscale;


import lombok.Getter;

@Getter
public class Notification {
  private String repositoryUrl;
  private String repositoryId;
  private String branchName;

  public Notification(String repositoryUrl, String repositoryId) {
    this.repositoryUrl = repositoryUrl;
    this.repositoryId = repositoryId;
  }

  public Notification(String repositoryUrl, String repositoryId, String branchName) {
    this(repositoryUrl, repositoryId);
    this.branchName = branchName;
  }
}
