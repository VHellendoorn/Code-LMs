import React, { forwardRef } from 'react';
import { Box } from '../box';
import { Icon } from '../icon';
import { getIconWrapperProps, useSelectStyle } from './styles';
import splitProps from './split-props';

const defaultVariant = 'outline';

const Select = forwardRef((
  {
    variant,
    multiple, // multiple options
    size, // multiple options
    isInvalid,
    children,
    ...rest
  },
  ref,
) => {
  // Use fallback values if values are null or undefined
  variant = variant ?? defaultVariant;

  const iconWrapperProps = getIconWrapperProps();
  const styleProps = useSelectStyle({ variant, multiple });
  const [rootProps, selectProps] = splitProps(rest);
  const { disabled, required } = selectProps;

  return (
    <Box
      position="relative"
      width="100%"
      {...rootProps}
    >
      <Box
        ref={ref}
        as="select"
        aria-disabled={disabled}
        aria-required={required}
        aria-invalid={isInvalid}
        multiple={multiple}
        size={size}
        {...styleProps}
        {...selectProps}
      >
        {children}
      </Box>
      {!multiple && (
        <Box
          aria-disabled={disabled}
          {...iconWrapperProps}
        >
          <Icon width="4x" icon="angle-down" />
        </Box>
      )}
    </Box>
  );
});

Select.displayName = 'Select';

export default Select;
