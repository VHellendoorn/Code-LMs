import { useColorMode } from '../color-mode';

const baseProps = {
  appearance: 'none',
  border: 'none',
  color: 'inherit',
  outline: 0,
  padding: 0,
  position: 'relative',
  width: '100%',
  display: 'flex',
  alignItems: 'center',
  transition: 'all .2s',
};

const sizes = {
  'md': {
    borderRadius: 'sm',
    fontSize: 'sm',
    lineHeight: 'sm',
    pl: 'calc(.75rem - 1px)', // 12px - 1px
    pr: 'calc(2rem - 1px)', // 32px - 1px
    py: 'calc(.375rem - 1px)', // 6px - 1px
  },
};

const getOutlinedStyle = ({
  colorMode,
}) => {
  const backgroundColor = {
    dark: 'transparent',
    light: 'white',
  }[colorMode];
  const color = {
    dark: 'white:primary',
    light: 'black:primary',
  }[colorMode];
  const borderColor = {
    dark: 'gray:60',
    light: 'gray:30',
  }[colorMode];
  const hoverBorderColor = {
    dark: 'blue:50',
    light: 'blue:50',
  }[colorMode];
  const focusBorderColor = {
    dark: 'blue:60',
    light: 'blue:60',
  }[colorMode];
  const disabledBorderColor = {
    dark: 'gray:60',
    light: 'gray:30',
  }[colorMode];
  const invalidBorderColor = {
    dark: 'red:50',
    light: 'red:50',
  }[colorMode];
  const placeholderColor = {
    dark: 'white:tertiary',
    light: 'black:tertiary',
  }[colorMode];

  return {
    backgroundColor,
    border: 1,
    borderColor,
    color,
    _hover: {
      borderColor: hoverBorderColor,
    },
    _focus: {
      borderColor: focusBorderColor,
    },
    _disabled: {
      borderColor: disabledBorderColor,
      cursor: 'not-allowed',
      opacity: '.28',
    },
    _valid: {
      // XXX - border color for valid input is not defined
    },
    _invalid: {
      borderColor: invalidBorderColor,
    },
    __placeholder: {
      color: placeholderColor,
      // Override Firefox's unusual default opacity
      opacity: 1,
    },
  };
};

const getFilledStyle = ({
  colorMode,
}) => {
  const backgroundColor = {
    dark: 'gray:80',
    light: 'gray:10',
  }[colorMode];
  const color = {
    dark: 'white:primary',
    light: 'black:primary',
  }[colorMode];

  return {
    ...getOutlinedStyle({ colorMode }),
    backgroundColor,
    color,
  };
};

const getUnstyledStyle = ({
  colorMode,
}) => {
  const backgroundColor = {
    dark: 'transparent',
    light: 'white',
  }[colorMode];
  const color = {
    dark: 'white:primary',
    light: 'black:primary',
  }[colorMode];

  return {
    backgroundColor,
    color,
    border: 0,
    borderRadius: 0,
    height: undefined,
    px: undefined,
    py: undefined,
  };
};

const getIconWrapperProps = () => {
  return {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'absolute',
    right: 0,
    top: 0,
    height: '100%',
    pointerEvents: 'none',
    color: 'inherit',
    pr: 'calc(.75rem - 1px)', // 12px - 1px
    pl: '1x',
    _disabled: {
      opacity: '.28',
    },
  };
};

const getSizeProps = (props) => {
  const defaultSize = 'md';

  return sizes[defaultSize];
};

const getVariantProps = (props) => {
  const { colorMode, variant } = props;

  if (variant === 'outline') {
    return getOutlinedStyle({ colorMode });
  }

  if (variant === 'filled') {
    return getFilledStyle({ colorMode });
  }

  if (variant === 'unstyled') {
    return getUnstyledStyle({ colorMode });
  }

  return {};
};

const useSelectStyle = ({
  variant,
  multiple,
}) => {
  const [colorMode] = useColorMode();
  const _props = {
    colorMode,
    variant,
  };
  const sizeProps = getSizeProps(_props); // use default size
  const variantProps = getVariantProps(_props);

  if (multiple) {
    variantProps.height = undefined;
    variantProps.px = undefined;
  }

  return {
    ...baseProps,
    ...sizeProps,
    ...variantProps,
  };
};

const useOptionStyle = () => {
  const [colorMode] = useColorMode();
  const backgroundColor = {
    dark: 'gray:100',
    light: 'white',
  }[colorMode];
  const color = {
    dark: 'white:primary',
    light: 'black:primary',
  }[colorMode];

  return {
    backgroundColor,
    color,
  };
};

const useOptionGroupStyle = () => {
  return useOptionStyle();
};

export {
  getIconWrapperProps,
  useSelectStyle,
  useOptionStyle,
  useOptionGroupStyle,
};
