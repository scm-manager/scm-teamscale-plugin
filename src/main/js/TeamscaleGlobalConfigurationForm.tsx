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
          this.isStateValid()
        )
    );
  };

  isStateValid = () => {
    return !!this.state.url;
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
          valid && this.isStateValid()
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
        helpText={t("scm-teamscale-plugin.config.form." + name + "-helptext")}
        checked={this.state[name]}
        onChange={this.configChangeHandler}
        disabled={readOnly}
      />
    );
  };
}

export default withTranslation("plugins")(TeamscaleGlobalConfigurationForm);
