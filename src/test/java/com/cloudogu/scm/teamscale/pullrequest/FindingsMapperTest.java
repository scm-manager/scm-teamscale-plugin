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
