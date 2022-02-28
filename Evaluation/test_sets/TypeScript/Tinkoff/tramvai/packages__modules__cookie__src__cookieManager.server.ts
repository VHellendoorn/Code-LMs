import type { REQUEST_MANAGER_TOKEN, RESPONSE_MANAGER_TOKEN } from '@tramvai/tokens-common';
import { serialize } from 'cookie';
import { calculateExpires, trimSubdomains } from './utils';
import type { CookieManager as Interface, CookieOptions, CookieSetOptions } from './tokens';

export class CookieManager implements Interface {
  private cookies: Record<string, string>;

  private requestManager: typeof REQUEST_MANAGER_TOKEN;

  private responseManager: typeof RESPONSE_MANAGER_TOKEN;

  constructor({
    requestManager,
    responseManager,
  }: {
    requestManager: typeof REQUEST_MANAGER_TOKEN;
    responseManager: typeof RESPONSE_MANAGER_TOKEN;
  }) {
    this.requestManager = requestManager;
    this.responseManager = responseManager;
    this.cookies = { ...requestManager.getCookies() };
  }

  get(name) {
    return this.cookies[name];
  }

  set({ name, value, noSubdomains, ...options }: CookieSetOptions) {
    this.responseManager.setCookie(
      name,
      serialize(name, value, {
        path: '/',
        ...options,
        expires: calculateExpires(options.expires),
        domain: noSubdomains
          ? trimSubdomains(options.domain || this.requestManager.getHost())
          : options.domain,
      })
    );
    // Записываем в cookie request, так как эти данные могут дальше читаться и использоваться
    this.cookies[name] = value;
  }

  all() {
    return this.cookies;
  }

  remove(name: string, options?: CookieOptions) {
    this.set({ ...options, name, value: '', expires: new Date(0) });
    delete this.cookies[name];
  }
}
