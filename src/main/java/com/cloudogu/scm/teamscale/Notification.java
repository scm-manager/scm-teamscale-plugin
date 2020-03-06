package com.cloudogu.scm.teamscale;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Notification {
  private String repositoryUrl;
  private String repositoryId;
  private String branchName;
}
