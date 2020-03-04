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
          this.isStateValid()
        )
    );
  };

  isStateValid = () => {
    return !!this.state.url;
  };

  render() {
    return (
      <>
        <div className="columns is-multiline">
          <div className="column is-full">{this.renderInputField("url")}</div>
          <div className="column is-half">{this.renderInputField("username")}</div>
          <div className="column is-half">{this.renderInputField("password", "password")}</div>
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
        helpText={t("scm-teamscale-plugin.config.form." + name + "-helptext")}
        onChange={this.configChangeHandler}
        value={this.state[name]}
        name={name}
        disabled={readOnly}
      />
    );
  };
}

export default withTranslation("plugins")(TeamscaleRepositoryConfigurationForm);
