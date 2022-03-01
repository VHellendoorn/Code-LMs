import type { NavigationGuard } from '@tinkoff/router';
import type {
  LOGGER_TOKEN,
  REQUEST_MANAGER_TOKEN,
  RESPONSE_MANAGER_TOKEN,
} from '@tramvai/tokens-common';

export const httpMethod = ({
  requestManager,
  responseManager,
  logger,
}: {
  requestManager: typeof REQUEST_MANAGER_TOKEN;
  responseManager: typeof RESPONSE_MANAGER_TOKEN;
  logger: typeof LOGGER_TOKEN;
}): NavigationGuard => {
  const log = logger('route:httpMethod');

  return async ({ to }) => {
    const { httpMethods } = to.config;

    if (httpMethods) {
      const currentMethod = requestManager.getMethod().toLowerCase();

      if (!httpMethods.split(',').includes(currentMethod)) {
        log.info({
          event: 'check-failed',
          currentMethod,
          route: to,
        });

        responseManager.setStatus(405);
        return false;
      }
    }
  };
};
