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
import { Title, Configuration } from "@scm-manager/ui-components";
import TeamscaleGlobalConfigurationForm from "./TeamscaleGlobalConfigurationForm";
import { useDocumentTitle } from "@scm-manager/ui-core";

type Props = {
  link: string;
};

const TeamscaleGlobalConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitle(t("scm-teamscale-plugin.config.link"));

  return (
    <>
      <Title title={t("scm-teamscale-plugin.config.title")} />
      <Configuration link={link} render={props => <TeamscaleGlobalConfigurationForm {...props} />} />
    </>
  );
}

export default TeamscaleGlobalConfiguration;
