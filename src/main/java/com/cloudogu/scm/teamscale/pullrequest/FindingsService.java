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

import com.google.common.base.Strings;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import jakarta.inject.Inject;

public class FindingsService {

  private static final String STORE_NAME = "teamscale-findings";

  private final DataStoreFactory dataStoreFactory;

  @Inject
  public FindingsService(DataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  public Findings getFindings(Repository repository, String id) {
    Findings findings = getStore(repository).get(id);

    if (findings == null || Strings.isNullOrEmpty(findings.getContent())) {
      return new Findings("");
    }
    return findings;
  }

  public void setFindings(Repository repository, String id, String findingsContent) {
    getStore(repository).put(id, new Findings(findingsContent));
  }

  private DataStore<Findings> getStore(Repository repository) {
    return dataStoreFactory.withType(Findings.class).withName(STORE_NAME).forRepository(repository).build();
  }

}
