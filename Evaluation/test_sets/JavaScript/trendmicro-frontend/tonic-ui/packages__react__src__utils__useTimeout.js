import { useEffect, useRef } from 'react';

const useTimeout = (callback, delay = null) => {
  const savedCallback = useRef(null);

  // Remember the latest callback
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  // Set up the interval
  useEffect(() => {
    function tick() {
      if (typeof savedCallback.current === 'function') {
        savedCallback.current();
      }
    }

    if (delay === null || delay === undefined) {
      return () => {};
    }

    const id = setTimeout(tick, delay);
    return () => clearTimeout(id);
  }, [delay]);
};

export default useTimeout;
