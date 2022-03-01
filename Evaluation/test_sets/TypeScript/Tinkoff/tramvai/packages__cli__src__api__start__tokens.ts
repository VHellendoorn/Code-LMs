import { createToken } from '@tinkoff/dippy';

export const INIT_HANDLER_TOKEN = createToken<Array<() => Promise<void>>>('start initHandler', {
  multi: true,
});

export const PROCESS_HANDLER_TOKEN = createToken<Array<() => Promise<void>>>(
  'start processHandler',
  {
    multi: true,
  }
);
export const CLOSE_HANDLER_TOKEN = createToken<Array<() => Promise<void>>>('start closeHandler', {
  multi: true,
});

export const STRICT_ERROR_HANDLE = createToken<boolean>('start strictErrorHandle');
