import express from 'express';
import { Module } from '@tramvai/core';
import {
  SERVER_MODULE_STATICS_OPTIONS,
  WEB_APP_TOKEN,
  WEB_APP_BEFORE_INIT_TOKEN,
} from '@tramvai/tokens-server';

const ONE_YEAR = 365 * 24 * 60 * 60;

@Module({
  providers: [
    {
      provide: WEB_APP_BEFORE_INIT_TOKEN,
      useFactory: ({ app, options }) => {
        const path = options?.path || 'public';

        return () => {
          app.use(
            express.static(path, {
              setHeaders: (res) => {
                const oneYearForward = new Date(Date.now() + ONE_YEAR * 1000);

                res.set('cache-control', `public, max-age=${ONE_YEAR}`);
                res.set('expires', oneYearForward.toUTCString());
              },
            })
          );
        };
      },
      deps: {
        app: WEB_APP_TOKEN,
        options: {
          token: SERVER_MODULE_STATICS_OPTIONS,
          optional: true,
        },
      },
      multi: true,
    },
  ],
})
export class ServerStaticsModule {}
