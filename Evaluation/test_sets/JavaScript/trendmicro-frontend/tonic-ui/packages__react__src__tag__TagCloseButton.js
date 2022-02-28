import React, { forwardRef } from 'react';
import { ButtonBase } from '../button';
import { useTagCloseButtonStyle } from './styles';

const TagCloseButton = forwardRef((
  {
    size,
    ...rest
  },
  ref,
) => {
  const closeButtonStyleProps = useTagCloseButtonStyle({ size });

  return (
    <ButtonBase ref={ref} {...closeButtonStyleProps} {...rest} />
  );
});

export default TagCloseButton;
