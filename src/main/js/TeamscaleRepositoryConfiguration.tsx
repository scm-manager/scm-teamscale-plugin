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

import React, {FC} from "react";
import { useTranslation } from "react-i18next";
import { Subtitle, Configuration } from "@scm-manager/ui-components";
import ConfigurationForm from "./TeamscaleRepositoryConfigurationForm";
import { useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { Repository } from "@scm-manager/ui-types";

type Props = {
  link: string;
  repository: Repository;
};

const TeamscaleRepositoryConfiguration: FC<Props> = ({ link, repository }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitleForRepository(repository, t("scm-teamscale-plugin.config.link"));

  return (
    <>
      <Subtitle subtitle={t("scm-teamscale-plugin.config.title")} />
      <Configuration link={link} render={props => <ConfigurationForm {...props} />} />
    </>
  );
};

export default TeamscaleRepositoryConfiguration;
