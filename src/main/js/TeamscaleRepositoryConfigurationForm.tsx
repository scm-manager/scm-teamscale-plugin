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
import { TeamscaleConfiguration } from "./types";
import { WithTranslation, withTranslation } from "react-i18next";
import { InputField } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  initialConfiguration: TeamscaleConfiguration;
  readOnly: boolean;
  onConfigurationChange: (p1: TeamscaleConfiguration, p2: boolean) => void;
};

class TeamscaleRepositoryConfigurationForm extends React.Component<Props, TeamscaleConfiguration> {
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
    return (
      <>
        <div className="columns is-multiline">
          <div className="column is-full">{this.renderInputField("url")}</div>
        </div>
      </>
    );
  }

  renderInputField = (name: string, type?: string) => {
    const { readOnly, t } = this.props;
    return (
      <InputField
        type={type}
        label={t("scm-teamscale-plugin.config.form." + name)}
        helpText={t("scm-teamscale-plugin.config.form." + name + "Helptext")}
        onChange={this.configChangeHandler}
        value={this.state[name]}
        name={name}
        disabled={readOnly}
      />
    );
  };
}

export default withTranslation("plugins")(TeamscaleRepositoryConfigurationForm);
