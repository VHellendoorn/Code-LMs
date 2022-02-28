import { createReducer, createEvent } from '@tramvai/state';

export const setInstantMetrics = createEvent<any>('set instant metrics map');

export interface State {
  instantMetricsMap: Record<string, boolean>;
}

export const MetricsStore = createReducer<State>('instantMetrics', { instantMetricsMap: {} }).on(
  setInstantMetrics,
  (_prevState, nextState) => {
    return nextState;
  }
);
