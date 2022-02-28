import React, { forwardRef } from 'react';
import { Box } from '../box';
import {
  getAnimationCSS,
  getVariantCSS,
  useSkeletonStyle,
} from './styles';

const defaultVariant = 'text';

const Skeleton = forwardRef((
  {
    animation,
    variant,
    css,
    ...rest
  },
  ref
) => {
  // Use fallback values if values are null or undefined
  variant = variant ?? defaultVariant;
  css = [
    getAnimationCSS(animation),
    getVariantCSS(variant),
    css,
  ];
  const styleProps = useSkeletonStyle({ animation, variant });

  return (
    <Box
      ref={ref}
      css={css}
      {...styleProps}
      {...rest}
    />
  );
});

Skeleton.displayName = 'Skeleton';

export default Skeleton;
