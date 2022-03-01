import { keyframes } from '@emotion/react';
import { ensurePlainObject } from 'ensure-type';
import _get from 'lodash.get';
import React, { forwardRef } from 'react';
import { useTheme } from '../theme';
import SVGIcon from './SVGIcon';

const cwSpin = keyframes`
  0% {
      transform: rotate(0deg)
  }
  to {
      transform: rotate(1turn)
  }
`;

const ccwSpin = keyframes`
  0% {
      transform: rotate(0deg)
  }
  to {
      transform: rotate(-1turn)
  }
`;

const Icon = forwardRef((
  {
    icon,
    spin = false,
    ...rest
  },
  ref
) => {
  const { icons = {} } = useTheme();
  const tmicon = _get(icons, [`tmicon-${icon}`]);
  const { path, ...restIconProps } = ensurePlainObject(_get(icons, icon, tmicon));
  const styleProps = {
    animation: (() => {
      if (spin === 'ccw') {
        return `${ccwSpin} 2s linear infinite`;
      }
      if (spin === 'cw' || spin === true) {
        return `${cwSpin} 2s linear infinite`;
      }
      return undefined;
    })(),
  };

  return (
    <SVGIcon
      ref={ref}
      {...styleProps}
      {...restIconProps}
      {...rest}
    >
      {path}
    </SVGIcon>
  );
});

Icon.displayName = 'Icon';

export default Icon;
