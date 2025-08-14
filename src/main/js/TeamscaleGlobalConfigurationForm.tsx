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

import React from "react";
import { TeamscaleGlobalConfiguration } from "./types";
import { WithTranslation, withTranslation } from "react-i18next";
import { Checkbox } from "@scm-manager/ui-components";
import TeamscaleRepositoryConfigurationForm from "./TeamscaleRepositoryConfigurationForm";

type Props = WithTranslation & {
  initialConfiguration: TeamscaleGlobalConfiguration;
  readOnly: boolean;
  onConfigurationChange: (p1: TeamscaleGlobalConfiguration, p2: boolean) => void;
};

class TeamscaleGlobalConfigurationForm extends React.Component<Props, TeamscaleGlobalConfiguration> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  configChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };

  render() {
    const { readOnly } = this.props;
    return (
      <>
        <TeamscaleRepositoryConfigurationForm
          initialConfiguration={this.state}
          readOnly={readOnly}
          onConfigurationChange={this.configChange}
        />
        {this.renderCheckbox("disableRepositoryConfiguration")}
      </>
    );
  }

  configChange = (config: TeamscaleGlobalConfiguration, valid: boolean) => {
    const { disableRepositoryConfiguration } = this.state;
    this.setState(
      {
        ...config,
        disableRepositoryConfiguration
      },
      () => {
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          valid
        );
      }
    );
  };

  renderCheckbox = (name: string) => {
    const { readOnly, t } = this.props;
    return (
      <Checkbox
        name={name}
        label={t("scm-teamscale-plugin.config.form." + name)}
        helpText={t("scm-teamscale-plugin.config.form." + name + "Helptext")}
        checked={this.state[name]}
        onChange={this.configChangeHandler}
        disabled={readOnly}
      />
    );
  };
}

export default withTranslation("plugins")(TeamscaleGlobalConfigurationForm);
