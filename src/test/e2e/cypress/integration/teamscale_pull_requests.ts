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

import { hri } from "human-readable-ids";
import { withAuth } from "@scm-manager/integration-test-runner/build/lib/helpers";

describe("Pull Request Details", () => {
  beforeEach(() => {
    const namespace = hri.random();
    const repoName = hri.random();
    cy.wrap(namespace).as("namespace");
    cy.wrap(repoName).as("repoName");
    cy.restCreateRepo("git", namespace, repoName);
    cy.restLogin("scmadmin", "scmadmin");
  });

  it("should be able to query for all pull requests", function() {
    cy
      .request(
        withAuth({
          method: "GET",
          url: `/api/v2/teamscale/pull-request/${this.namespace}/${this.repoName}`
        })
      )
      .then(response => {
          expect(response.status).to.eq(200);
          expect(response.headers).to.include({
            'content-type': 'application/vnd.scmm-teamscalepullrequest+json;v=2'
          });
        }
      );
  })

  it("should be able to query for specific pull requests", function() {
    cy
      .request(
        {
          ...withAuth({
                     method: "GET",
                     url:`/api/v2/teamscale/pull-request/${this.namespace}/${this.repoName}/42`
          }),
          failOnStatusCode: false,
        }
      )
      .then(response => {
          // we're fine with a 404 here; we just want to make sure that we do not get a 500
          expect(response.status).to.eq(404);
        }
      );
  })
});
