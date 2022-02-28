import type { Provider } from '@tinkoff/dippy';
import {
  CONFIG_MANAGER_TOKEN,
  COMMAND_PARAMETERS_TOKEN,
  CONFIG_ENTRY_TOKEN,
} from '../../../di/tokens';
import { ConfigManager } from '../../../config/configManager';
import type { ApplicationConfigEntry } from '../../../typings/configEntry/application';
import type { Params } from '../index';

export const sharedProviders: readonly Provider[] = [
  {
    provide: CONFIG_MANAGER_TOKEN,
    useFactory: ({
      configEntry,
      parameters,
    }: {
      configEntry: ApplicationConfigEntry;
      parameters: Params;
    }) => {
      return new ConfigManager(configEntry, {
        ...parameters,
        env: 'production',
        buildType: 'client',
      });
    },
    deps: {
      configEntry: CONFIG_ENTRY_TOKEN,
      parameters: COMMAND_PARAMETERS_TOKEN,
    },
  },
];
