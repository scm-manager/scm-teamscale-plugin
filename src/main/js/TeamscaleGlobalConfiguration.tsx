import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Subtitle, Configuration } from "@scm-manager/ui-components";
import TeamscaleGlobalConfigurationForm from "./TeamscaleGlobalConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class TeamscaleGlobalConfiguration extends React.Component<Props> {
  render() {
    const { link, t } = this.props;

    return (
      <>
        <Subtitle subtitle={t("scm-teamscale-plugin.config.title")} />
        <Configuration link={link} render={props => <TeamscaleGlobalConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(TeamscaleGlobalConfiguration);
