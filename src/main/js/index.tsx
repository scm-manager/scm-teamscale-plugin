import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import TeamscaleRepositoryConfiguration from "./TeamscaleRepositoryConfiguration";
import TeamscaleGlobalConfiguration from "./TeamscaleGlobalConfiguration";

cfgBinder.bindRepositorySetting(
  "/teamscale",
  "scm-teamscale-plugin.config.link",
  "teamscaleConfig",
  TeamscaleRepositoryConfiguration
);

cfgBinder.bindGlobal("/teamscale", "scm-teamscale-plugin.config.link", "teamscaleConfig", TeamscaleGlobalConfiguration);
