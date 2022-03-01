import { createReducer, createEvent } from '@tramvai/state';
import type { ChildAppRequestConfig } from '@tramvai/tokens-child-app';

export const setPreloaded = createEvent<ChildAppRequestConfig[]>('child-app set preloaded');

const initialState: {
  preloaded: ChildAppRequestConfig[];
} = {
  preloaded: [],
};

export const ChildAppStore = createReducer('child-app', initialState).on(
  setPreloaded,
  (_, preloaded) => {
    return {
      preloaded,
    };
  }
);
