import memoize from 'micro-memoize';
import React, { useCallback, useEffect, useState } from 'react';
import noop from '../utils/noop';
import { ColorModeContext } from './context';
import canUseDOM from '../utils/dom/canUseDOM';
import { getColorScheme, colorSchemeQuery } from './utils';

const ensureColorMode = (colorMode) => {
  return colorMode === 'dark' ? 'dark' : 'light';
};

const getMemoizedState = memoize(state => ({ ...state }));

const ColorModeProvider = ({
  children,
  defaultValue: defaultValueProp,
  value: valueProp,
  onChange: onChangeProp,
  useSystemColorMode,
}) => {
  const defaultColorMode = (defaultValueProp === 'dark') ? 'dark' : 'light';
  const [colorMode, setColorMode] = useState(ensureColorMode(valueProp ?? defaultColorMode));

  useEffect(() => {
    if (valueProp !== undefined) {
      setColorMode(ensureColorMode(valueProp));
    }
  }, [valueProp]);

  const onChange = useCallback((nextValue) => {
    if (valueProp !== undefined) {
      setColorMode(ensureColorMode(valueProp));
    } else {
      setColorMode(ensureColorMode(nextValue));
    }
    if (typeof onChangeProp === 'function') {
      onChangeProp(nextValue); // Pass original value to the onChange callback
    }
  }, [valueProp, onChangeProp]);

  useEffect(() => {
    if (valueProp !== null && valueProp !== undefined) {
      // bypass the system color mode if `valueProp` is set
      return noop;
    }
    if (!useSystemColorMode) {
      return noop;
    }
    if (!canUseDOM) {
      return noop;
    }

    const systemColorMode = getColorScheme(defaultColorMode);
    onChange(systemColorMode);

    const mediaQueryList = window?.matchMedia?.(colorSchemeQuery.dark);
    const listener = () => {
      onChange(mediaQueryList.matches ? 'dark' : 'light');
    };
    mediaQueryList.addEventListener('change', listener);
    return () => {
      mediaQueryList.removeEventListener('change', listener);
    };
  }, [defaultValueProp, valueProp, useSystemColorMode, defaultColorMode, onChange]);

  const colorModeState = getMemoizedState({
    colorMode,
    onChange,
  });

  return (
    <ColorModeContext.Provider value={colorModeState}>
      {children}
    </ColorModeContext.Provider>
  );
};

ColorModeProvider.displayName = 'ColorModeProvider';

export default ColorModeProvider;
