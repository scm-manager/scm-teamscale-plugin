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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;

class FindingsServiceTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  private FindingsService findingsService;

  @BeforeEach
  void initService() {
    InMemoryDataStore<Findings> dataStore = new InMemoryDataStore();
    InMemoryDataStoreFactory dataStoreFactory = new InMemoryDataStoreFactory(dataStore);
    findingsService = new FindingsService(dataStoreFactory);
  }

  @Test
  void shouldReturnEmptyFindings() {
    Findings findings = findingsService.getFindings(REPOSITORY, "1");

    assertThat(findings.getContent()).isEqualTo("");
  }

  @Test
  void shouldUpdateFindings() {
    String content = "teamscale found some criticals";
    String pullRequestId = "1";

    findingsService.setFindings(REPOSITORY, pullRequestId, content);

    Findings storedFindings = findingsService.getFindings(REPOSITORY, pullRequestId);

    assertThat(storedFindings.getContent()).isEqualTo(content);
  }

}
