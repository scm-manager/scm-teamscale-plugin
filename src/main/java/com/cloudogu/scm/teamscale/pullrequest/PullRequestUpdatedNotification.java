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

package com.cloudogu.scm.teamscale.pullrequest;

import com.cloudogu.scm.teamscale.Notification;
import lombok.Getter;

@Getter
public class PullRequestUpdatedNotification extends Notification {
  private final String pullRequestId;

  public PullRequestUpdatedNotification(String repositoryUrl, String repositoryId, String pullRequestId) {
    super(repositoryUrl, repositoryId);
    this.pullRequestId = pullRequestId;
  }
}
