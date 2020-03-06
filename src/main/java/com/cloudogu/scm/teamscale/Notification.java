package com.cloudogu.scm.teamscale;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notification {
  private String repositoryUrl;
  private String repositoryId;
  private String branchName;
}
