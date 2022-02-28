import type { RequestManager } from '@tramvai/tokens-common';

export const createMockRequestManager = ({
  body = null,
  url = 'http://localhost',
  method = 'GET',
  cookies = {},
  headers = {},
  clientIp = '127.0.0.1',
  host = 'localhost',
}: {
  body?: any;
  url?: string;
  method?: string;
  cookies?: Record<string, string>;
  headers?: Record<string, string>;
  clientIp?: string;
  host?: string;
} = {}): RequestManager => {
  return {
    getBody: () => body,
    getUrl: () => url,
    getMethod: () => method,
    getCookie: (name: string) => cookies[name],
    getCookies: () => cookies,
    getHeader: (name: string) => headers[name],
    getHeaders: () => headers,
    getClientIp: () => clientIp,
    getHost: () => host,
  };
};
