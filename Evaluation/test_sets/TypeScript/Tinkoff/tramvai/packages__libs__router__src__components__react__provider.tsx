import React, { useEffect, useState } from 'react';
import type { Url } from '@tinkoff/url';
import { RouterContext, RouteContext, UrlContext } from './context';
import type { AbstractRouter } from '../../router/abstract';
import type { NavigationRoute } from '../../types';

export const Provider: React.FunctionComponent<{ router: AbstractRouter }> = ({
  router,
  children,
}) => {
  const [state, setState] = useState<{ route: NavigationRoute; url: Url }>({
    route: router.getCurrentRoute(),
    url: router.getCurrentUrl(),
  });

  useEffect(() => {
    return router.registerSyncHook('change', ({ to, url }) => {
      setState({ route: to, url });
    });
  }, [router]);

  return (
    <RouterContext.Provider value={router}>
      <RouteContext.Provider value={state.route}>
        <UrlContext.Provider value={state.url}>{children}</UrlContext.Provider>
      </RouteContext.Provider>
    </RouterContext.Provider>
  );
};

Provider.displayName = 'Provider';
