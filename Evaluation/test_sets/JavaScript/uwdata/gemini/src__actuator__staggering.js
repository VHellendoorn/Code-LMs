import * as d3 from "d3";
import { flatten } from "../util/util.js";
import { getEaseFn } from "./util";

const ORDER = {
  ascending: d3.ascending,
  descending: d3.descending
};

function getOrderFn(isNumber, order) {
  if (isNumber && order) {
    return (a, b) => ORDER[order](Number(a), Number(b));
  }
  return ORDER[order];
}

function staggeredTiming(staggering, data, duration) {
  let N;
  let grouped;
  const dataWithTiming = data.map((d, i) => {
    return { ...d, __staggering_id__: i };
  });
  const subStaggering = staggering.staggering;

  const isNumber =
    staggering.by &&
    dataWithTiming.reduce((acc, d) => {
      let val;
      if (typeof staggering.by === "string") {
        val = (d.initial || d.final)[staggering.by];
      } else if (staggering.by.initial || staggering.by.final) {
        const which = staggering.by.initial ? "initial" : "final";
        val = (which === "initial"
          ? d.initial || d.final
          : d.final || d.initial)[staggering.by[which]];
      }
      return (acc = acc && (val !== undefined ? !isNaN(Number(val)) : true));
    }, true);
  if (!staggering.by) {


    const orderFn = getOrderFn(true, staggering.order);
    grouped = d3.groups(dataWithTiming, d => {
      const val = d.__staggering_id__;
      return val === undefined ? "__empty__" : val;
    })
    if (typeof(orderFn) === "function") {
      grouped.sort((a,b) => orderFn(a[0], b[0]));
    }
  } else if (typeof staggering.by === "string") {


    grouped = d3.groups(dataWithTiming, d => {
      const val = (d.initial || d.final)[staggering.by];
      return val === undefined ? "__empty__" : val;
    })

    const orderFn = getOrderFn(isNumber, staggering.order);
    if (typeof(orderFn) === "function") {
      grouped.sort((a,b) => orderFn(a[0], b[0]));
    }
  } else if (staggering.by.initial || staggering.by.final) {
    const which = staggering.by.initial ? "initial" : "final";


    grouped = d3.groups(dataWithTiming, d => {
      const val = (which === "initial"
        ? d.initial || d.final
        : d.final || d.initial)[staggering.by[which]];
      return val === undefined ? "__empty__" : val;
    })

    const orderFn = getOrderFn(isNumber, staggering.order);
    if (typeof(orderFn) === "function") {
      grouped.sort((a,b) => orderFn(a[0], b[0]));
    }
  }

  N = grouped.length;

  const ease = getEaseFn(staggering.ease || "linear") || d3.easeLinear;
  const r = staggering.overlap === undefined ? 1 : staggering.overlap;
  const delta_e = i => ease((i + 1) / N) - ease(i / N);
  const alpha = 1 / (delta_e(0) * r + 1 - r);

  let durations = new Array(N).fill(0);
  durations = durations.map((d, i) => delta_e(i) * alpha * duration);
  let delayAcc = 0;
  const delays = durations.map((dur, i, durations) => {
    const currDelay = delayAcc;
    if (i < N - 1) {
      delayAcc = delayAcc + dur - durations[i + 1] * r;
    }
    return currDelay;
  });

  if (subStaggering) {
    const timings = delays.map((d, i) => {
      return {
        delay: d,
        duration: durations[i]
      };
    });

    timings.groups = grouped.map((g, i) => {
      return staggeredTiming(subStaggering, g[1], durations[i]);
    });

    return getFlattenTimings(timings);
  }
  grouped.forEach((group, i) => {
    group[1].forEach(datum => {
      datum.delay = delays[i];
      datum.duration = durations[i];
    });
  });

  return dataWithTiming;
}

function getFlattenTimings(timings) {
  if (!timings.groups) {
    return timings;
  }
  return flatten(
    timings.map((g_t, i) => {
      return getFlattenTimings(timings.groups[i]).map(t => {
        return Object.assign({}, t, { delay: t.delay + g_t.delay });
      });
    })
  );
}

export { staggeredTiming };
