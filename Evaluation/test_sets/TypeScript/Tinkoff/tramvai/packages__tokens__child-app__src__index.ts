import { createToken } from '@tinkoff/dippy';
import type { Action, Command } from '@tramvai/core';
import type { INITIAL_APP_STATE_TOKEN } from '@tramvai/tokens-common';
import type { ComponentType } from 'react';
import type {
  ChildAppLoader,
  ChildAppDiManager,
  ChildAppPreloadManager,
  ChildAppCommandLineRunner,
  ChildAppRequestConfig,
  WrapperProps,
  RootStateSubscription,
  ChildAppStateManager,
  ChildAppFinalConfig,
  ChildAppRenderManager,
  ChildAppResolutionConfig,
  ResolutionConfig,
} from './types';

export * from './types';

const multiOptions = { multi: true };

/**
 * @public
 * @description CommandLineRunner steps specific for child app
 */
export const commandLineListTokens = {
  // section: client processing
  customerStart: createToken<Command>('child-app customer_start', multiOptions), // Инициализация клиента
  resolveUserDeps: createToken<Command>('child-app resolve_user_deps', multiOptions), // Получение данных о клиенте
  resolvePageDeps: createToken<Command>('child-app resolve_page_deps', multiOptions), // Получение данных необходимых для роута

  // section: clear data
  clear: createToken<Command>('child-app clear', multiOptions), // Очистка данных

  // section: spa transitions
  spaTransition: createToken<Command>('child-app spa_transition', multiOptions),
};

/**
 * @public
 * @description Contains child app configs that was used to figure out how to load child apps
 */
export const CHILD_APP_RESOLUTION_CONFIGS_TOKEN = createToken<ChildAppResolutionConfig[]>(
  'child-app resolve configs'
);

/**
 * @public
 * @description async function to execute any preload action before any child-app starts execute
 */
export const CHILD_APP_PRELOAD_EXTERNAL_CONFIG_TOKEN = createToken<() => Promise<void>>(
  'child-app preload external config'
);

/**
 * @public
 * @description Used to resolve resolution config for a specific child-app
 */
export const CHILD_APP_GET_RESOLUTION_CONFIG_TOKEN = createToken<
  (config: ChildAppRequestConfig) => ResolutionConfig
>('child-app get resolution config');

/**
 * @public
 * @description Used to resolve external config with urls to external code entries
 */
export const CHILD_APP_RESOLVE_CONFIG_TOKEN = createToken<
  (config: ChildAppRequestConfig) => ChildAppFinalConfig
>('child-app resolve external config');

/**
 * @public
 * @description Base url for external urls for child apps on client
 */
export const CHILD_APP_RESOLVE_BASE_URL_TOKEN = createToken<string>(
  'child-app resolve external base url'
);

/**
 * @public
 * @description Allows to preload child app for the specific page
 */
export const CHILD_APP_PRELOAD_MANAGER_TOKEN = createToken<ChildAppPreloadManager>(
  'child-app preload manager'
);

/**
 * @public
 * @description Contains child app config that was used to load current child app
 */
export const CHILD_APP_INTERNAL_CONFIG_TOKEN = createToken<ChildAppFinalConfig>(
  'child-app current config'
);

/**
 * @public
 * @description Actions of child app
 */
export const CHILD_APP_INTERNAL_ACTION_TOKEN = createToken<Action>(
  'child-app action',
  multiOptions
);

/**
 * @public
 * @description Subscription on a root state updates
 */
export const CHILD_APP_INTERNAL_ROOT_STATE_SUBSCRIPTION_TOKEN = createToken<RootStateSubscription>(
  'child-app root state subscription',
  multiOptions
);

/**
 * @public
 * @description Allows to recreate token implementation the same way as in root di, but specific to child-app di
 */
export const CHILD_APP_INTERNAL_ROOT_DI_BORROW_TOKEN = createToken<any>(
  'child-app root di borrow',
  multiOptions
);

/**
 * @private
 * @description boolean flag indicating that current di if for a child-app
 */
export const IS_CHILD_APP_DI_TOKEN = createToken<boolean>('child-app isChildApp Di');

/**
 * @private
 * @description Manages Singleton-Scope DIs for every child app
 */
export const CHILD_APP_SINGLETON_DI_MANAGER_TOKEN = createToken<ChildAppDiManager>(
  'child-app singleton di manager'
);

/**
 * @private
 * @description Manages Request-Scope DIs for every child app
 */
export const CHILD_APP_DI_MANAGER_TOKEN = createToken<ChildAppDiManager>('child-app di manager');

/**
 * @private
 * @description Bridge from React render to di providers for child apps
 */
export const CHILD_APP_RENDER_MANAGER_TOKEN = createToken<ChildAppRenderManager>(
  'child-app render manager'
);

/**
 * @private
 * @description Manages state dehydration for child-app
 */
export const CHILD_APP_STATE_MANAGER_TOKEN = createToken<ChildAppStateManager>(
  'child-app state manager'
);

/**
 * @private
 * @description Manages loading child-app resources from the external place
 */
export const CHILD_APP_LOADER_TOKEN = createToken<ChildAppLoader>('child-app loader');

/**
 * @private
 * @description Implements CommandLineRunner for child apps
 */
export const CHILD_APP_COMMAND_LINE_RUNNER_TOKEN = createToken<ChildAppCommandLineRunner>(
  'child-app command runner'
);

/**
 * @private
 * @description Stores the common server-dehydrated state for all of child apps
 */
export const CHILD_APP_COMMON_INITIAL_STATE_TOKEN = createToken<
  Record<string, typeof INITIAL_APP_STATE_TOKEN>
>('child-app initialAppState');

/**
 * @private
 * @description Used as render function for a child app. Usually implemented as a wrapper over child app render itself with an additional logic for di and connections to root app
 */
export const CHILD_APP_INTERNAL_RENDER_TOKEN = createToken<ComponentType<WrapperProps<any>>>(
  'child-app render'
);
