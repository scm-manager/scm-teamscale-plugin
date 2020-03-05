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
        label={t("scm-redmine-plugin.config.form." + name)}
        helpText={t("scm-redmine-plugin.config.form." + name + "-helptext")}
        checked={this.state[name]}
        onChange={this.configChangeHandler}
        disabled={readOnly}
      />
    );
  };
}

export default withTranslation("plugins")(TeamscaleGlobalConfigurationForm);
