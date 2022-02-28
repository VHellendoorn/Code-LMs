import each from '@tinkoff/utils/object/each';
import type { Bundle } from '@tramvai/core';
import { Module, Scope, BUNDLE_LIST_TOKEN, DI_TOKEN, provide } from '@tramvai/core';

import { EnvironmentModule } from '@tramvai/module-environment';

import { CookieModule } from '@tramvai/module-cookie';
import { LogModule } from '@tramvai/module-log';

import { Hooks } from '@tinkoff/hook-runner';
import {
  HOOK_TOKEN,
  COMPONENT_REGISTRY_TOKEN,
  PUBSUB_TOKEN,
  BUNDLE_MANAGER_TOKEN,
  CONTEXT_TOKEN,
  ACTION_REGISTRY_TOKEN,
  DISPATCHER_TOKEN,
  STORE_TOKEN,
  DISPATCHER_CONTEXT_TOKEN,
  ADDITIONAL_BUNDLE_TOKEN,
  LOGGER_TOKEN,
} from '@tramvai/tokens-common';
import { BundleManager } from './bundleManager/bundleManager';
import { ComponentRegistry } from './componentRegistry/componentRegistry';
import { RequestManagerModule } from './requestManager/RequestManagerModule';
import { ResponseManagerModule } from './responseManager/ResponseManagerModule';
import { createConsumerContext } from './createConsumerContext/createConsumerContext';

import { CommandModule } from './command/CommandModule';
import { PubSubModule } from './pubsub/PubSubModule';

import { providers as serverProviders } from './providers/serverProviders';
import { ActionModule } from './actions/ActionModule';
import { StateModule } from './state/StateModule';
import { CacheModule } from './cache/CacheModule';

@Module({
  imports: [
    CommandModule,
    EnvironmentModule,
    PubSubModule,
    LogModule,
    CookieModule,
    ActionModule,
    StateModule,
    RequestManagerModule,
    ResponseManagerModule,
    CacheModule,
  ],
  providers: [
    provide({
      // Инстанс хук системы
      provide: HOOK_TOKEN,
      scope: Scope.SINGLETON,
      useClass: Hooks,
    }),
    provide({
      // Регистр ui компонентов
      provide: COMPONENT_REGISTRY_TOKEN,
      scope: Scope.SINGLETON,
      useClass: ComponentRegistry,
      deps: {
        componentList: { token: 'componentDefaultList', optional: true },
      },
    }),
    provide({
      // Управление бандлами, хранение и получение
      provide: BUNDLE_MANAGER_TOKEN,
      scope: Scope.SINGLETON,
      useFactory: ({ additionalBundleList, ...bundleManagerDeps }) => {
        ((additionalBundleList as any) as Bundle[])?.forEach((bundles) => {
          each((bundle, name) => {
            const bundleAlreadyExists = name in bundleManagerDeps.bundleList;

            if (!bundleAlreadyExists) {
              // eslint-disable-next-line no-param-reassign
              bundleManagerDeps.bundleList[name] = () => Promise.resolve({ default: bundle });
            }
          }, bundles);
        });

        return new BundleManager(bundleManagerDeps);
      },
      deps: {
        bundleList: BUNDLE_LIST_TOKEN,
        additionalBundleList: { token: ADDITIONAL_BUNDLE_TOKEN, optional: true },
        componentRegistry: COMPONENT_REGISTRY_TOKEN,
        actionRegistry: ACTION_REGISTRY_TOKEN,
        dispatcher: DISPATCHER_TOKEN,
        dispatcherContext: DISPATCHER_CONTEXT_TOKEN,
        logger: LOGGER_TOKEN,
      },
    }),
    provide({
      //  Клиентский контекст исполнения
      provide: CONTEXT_TOKEN,
      scope: Scope.REQUEST,
      useFactory: createConsumerContext,
      deps: {
        di: DI_TOKEN,
        pubsub: PUBSUB_TOKEN,
        dispatcherContext: DISPATCHER_CONTEXT_TOKEN,
        store: STORE_TOKEN,
      },
    }),
    ...serverProviders,
  ],
})
export class CommonModule {}
