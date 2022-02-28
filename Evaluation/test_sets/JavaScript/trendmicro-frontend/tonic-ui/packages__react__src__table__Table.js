import { useEffectOnce } from '@tonic-ui/react-hooks';
import React, { forwardRef } from 'react';
import { Box } from '../box';
import { useColorMode } from '../color-mode';
import warnRemovedProps from '../utils/warnRemovedProps';
import { TableProvider } from './context';
import { useTableStyle } from './styles';

const Table = forwardRef((
  {
    isHoverable, // deprecated
    children,
    size = 'md',
    variant = 'default',
    ...rest
  },
  ref,
) => {
  useEffectOnce(() => {
    const prefix = `${Table.displayName}:`;

    if (isHoverable !== undefined) {
      warnRemovedProps('isHoverable', {
        prefix,
        message: 'Use the \'_hover\' prop on the \'TableRow\' component instead.',
      });
    }
  }, true); // TODO: check if `when` is true for each prop

  const styleProps = useTableStyle({});
  const minimalist = (variant === 'default');
  const context = {
    variant,
    size,
  };

  return (
    <TableProvider value={context}>
      <Box
        ref={ref}
        {...styleProps}
        {...rest}
      >
        { children }
        { !minimalist && (
          <>
            <HorizontalLine position="absolute" top="0" />
            <VerticalLine position="absolute" top="0" right="0" />
            <HorizontalLine position="absolute" bottom="0" />
            <VerticalLine position="absolute" top="0" left="0" />
          </>
        )}
      </Box>
    </TableProvider>
  );
});

const VerticalLine = (props) => {
  const [colorMode] = useColorMode();
  const isDark = colorMode === 'dark';
  return (
    <Box
      borderLeft={1}
      borderColor={isDark ? 'gray:70' : 'gray:50'}
      height="100%"
      width="1px"
      {...props}
    />
  );
};
const HorizontalLine = (props) => {
  const [colorMode] = useColorMode();
  const isDark = colorMode === 'dark';
  return (
    <Box
      borderTop={1}
      borderColor={isDark ? 'gray:70' : 'gray:50'}
      height="1px"
      width="100%"
      {...props}
    />
  );
};

Table.displayName = 'Table';

export default Table;
