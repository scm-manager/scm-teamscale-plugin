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
