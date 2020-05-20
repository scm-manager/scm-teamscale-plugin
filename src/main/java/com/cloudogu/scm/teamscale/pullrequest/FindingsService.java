package com.cloudogu.scm.teamscale.pullrequest;

import com.google.common.base.Strings;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;

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
