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
