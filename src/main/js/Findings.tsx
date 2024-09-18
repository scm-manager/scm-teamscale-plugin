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

import React, { FC, useEffect, useState } from "react";
import { Repository, Link } from "@scm-manager/ui-types";
import { apiClient, Loading, ErrorNotification, MarkdownView } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import styled from "styled-components";

type Props = {
  repository: Repository;
  pullRequest: any;
};

const Label = styled.div.attrs(() => ({
  className: "field-label is-inline-flex"
}))`
  text-align: left;
  margin-right: 0;
  min-width: 6.7em;
  max-width: 6.7em;
`;

const Findings: FC<Props> = ({ pullRequest }) => {
  const [t] = useTranslation("plugins");
  const [findings, setFindings] = useState("");
  const [error, setError] = useState<Error | undefined>(undefined);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if ((pullRequest?._links?.teamscaleFindings as Link)?.href) {
      setLoading(true)
      apiClient
        .get((pullRequest?._links?.teamscaleFindings as Link)?.href)
        .then(r => r.json())
        .then(finding => setFindings(finding.content))
        .then(() => setLoading(false))
        .catch(setError);
    }
  }, [pullRequest]);

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!findings) {
    return null;
  }

  return (
    <div className="field is-horizontal">
      <Label>{t("scm-teamscale-plugin.pullRequest.findings")}:</Label>
      <MarkdownView content={findings} />
    </div>
  );
};

export default Findings;
