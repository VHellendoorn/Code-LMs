import React, { forwardRef } from 'react';
import Text from './Text';
import { useTextLabelStyle } from './styles';

const TextLabel = forwardRef((props, ref) => {
  const styleProps = useTextLabelStyle();

  return (
    <Text
      as="label"
      ref={ref}
      {...styleProps}
      {...props}
    />
  );
});

TextLabel.displayName = 'TextLabel';

export default TextLabel;
