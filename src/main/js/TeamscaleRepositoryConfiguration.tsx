import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Subtitle, Configuration } from "@scm-manager/ui-components";
import TeamscaleRepositoryConfigurationForm from "./TeamscaleRepositoryConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class TeamscaleRepositoryConfiguration extends React.Component<Props> {
  render() {
    const { link, t } = this.props;

    return (
      <>
        <Subtitle subtitle={t("scm-teamscale-plugin.config.title")} />
        <Configuration link={link} render={props => <TeamscaleRepositoryConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(TeamscaleRepositoryConfiguration);
