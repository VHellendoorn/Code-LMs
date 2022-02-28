import { commandLineListTokens, createApp, provide } from '@tramvai/core';
import {
  HTML_ATTRS,
  RENDER_SLOTS,
  RESOURCES_REGISTRY,
  ResourceSlot,
  ResourceType,
} from '@tramvai/module-render';
import { modules, bundles } from '@tramvai/internal-test-utils/shared/common';
import { StorageRecord } from '@tinkoff/htmlpagebuilder';
import { RESOURCE_INLINE_OPTIONS } from '@tramvai/tokens-render';

createApp({
  name: 'render',
  modules,
  bundles,
  providers: [
    {
      provide: HTML_ATTRS,
      useValue: {
        target: 'html',
        attrs: {
          class: 'html',
          lang: 'ru',
        },
      },
      multi: true,
    },
    {
      provide: HTML_ATTRS,
      useValue: {
        target: 'body',
        attrs: {
          style: 'display: block; margin: 0;',
        },
      },
      multi: true,
    },
    {
      provide: HTML_ATTRS,
      useValue: {
        target: 'app',
        attrs: {
          'data-attr': 'value',
          bool: true,
        },
      },
      multi: true,
    },
    provide({
      provide: commandLineListTokens.resolvePageDeps,
      multi: true,
      useFactory: ({ resourcesRegistry }) => {
        return () => {
          resourcesRegistry.register({
            slot: ResourceSlot.BODY_END,
            type: ResourceType.style,
            payload: 'https://test.acdn.tinkoff.ru/123.css',
          });
        };
      },
      deps: {
        resourcesRegistry: RESOURCES_REGISTRY,
      },
    }),
    {
      provide: RESOURCE_INLINE_OPTIONS,
      useValue: {
        threshold: 10000,
        types: [StorageRecord.style],
      },
    },
  ],
});
