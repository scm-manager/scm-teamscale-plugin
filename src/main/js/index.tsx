import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import TeamscaleRepositoryConfiguration from "./TeamscaleRepositoryConfiguration";

cfgBinder.bindRepositorySetting(
  "/teamscale",
  "scm-teamscale-plugin.config.link",
  "teamscaleConfig",
  TeamscaleRepositoryConfiguration
);
