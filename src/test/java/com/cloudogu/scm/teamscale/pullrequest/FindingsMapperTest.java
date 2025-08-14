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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindingsMapperTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "hitchhiker", "HeartOfGold");

  private static final String PULL_REQUEST_ID = "1";

  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private Subject subject;

  @InjectMocks
  private FindingsMapperImpl mapper;


  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapFindingsWithLinks() {
    Findings findings = new Findings("awesome teamscale findings");

    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm/"));
    when(subject.isPermitted("repository:writeTeamscaleFindings:1")).thenReturn(true);

    FindingsDto dto = mapper.map(findings, REPOSITORY, PULL_REQUEST_ID);

    assertThat(dto.getContent()).isEqualTo(findings.getContent());
    assertThat(dto.getLinks().getLinkBy("self").isPresent()).isTrue();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/scm/v2/teamscale/pull-request/hitchhiker/HeartOfGold/1/findings");
    assertThat(dto.getLinks().getLinkBy("update").isPresent()).isTrue();
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo("/scm/v2/teamscale/pull-request/hitchhiker/HeartOfGold/1/findings");
  }

  @Test
  void shouldMapFindingsWithoutUpdateLink() {
    Findings findings = new Findings("awesome teamscale findings");

    when(scmPathInfoStore.get()).thenReturn(() -> URI.create("/scm/"));
    when(subject.isPermitted("repository:writeTeamscaleFindings:1")).thenReturn(false);

    FindingsDto dto = mapper.map(findings, REPOSITORY, PULL_REQUEST_ID);

    assertThat(dto.getContent()).isEqualTo(findings.getContent());
    assertThat(dto.getLinks().getLinkBy("self").isPresent()).isTrue();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/scm/v2/teamscale/pull-request/hitchhiker/HeartOfGold/1/findings");
    assertThat(dto.getLinks().getLinkBy("update").isPresent()).isFalse();
  }


}
