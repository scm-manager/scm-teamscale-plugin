import { Links } from "@scm-manager/ui-types";

export type TeamscaleConfiguration = {
  url: string;
  usernameTransformPattern: string;
  _links: Links;
};

export type TeamscaleGlobalConfiguration = TeamscaleConfiguration & {
  disableRepositoryConfiguration: boolean;
};
