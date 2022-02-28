import { useEffectOnce } from '@tonic-ui/react-hooks';
import React, { forwardRef, useRef, useState } from 'react';
import { Box } from '../box';
import useForkRef from '../utils/useForkRef';
import warnRemovedProps from '../utils/warnRemovedProps';
import { usePopover } from './context';

const PopoverTrigger = forwardRef((
  {
    shouldWrapChildren, // removed

    children,
    ...rest
  },
  ref,
) => {
  useEffectOnce(() => {
    const prefix = `${PopoverTrigger.displayName}:`;

    if (shouldWrapChildren !== undefined && !shouldWrapChildren) {
      warnRemovedProps('shouldWrapChildren', {
        prefix,
        message: 'Use Function as Child Component (FaCC) to render the popover trigger instead.',
      });
    }
  }, true); // TODO: check if `when` is true for each prop

  const {
    anchorRef,
    popoverId,
    onToggle,
    trigger,
    onOpen,
    isOpen,
    onClose,
    isHoveringRef,
    enterDelay,
    leaveDelay,
    setMouseCoordinate,
    nextToCursor,
    followCursor
  } = usePopover();
  const combinedRef = useForkRef(anchorRef, ref);
  const openTimeout = useRef(null);
  const [enableMouseMove, setEnableMouseMove] = useState(true);

  const eventHandlerProps = {};

  if (trigger === 'click') {
    eventHandlerProps.onClick = onToggle;
    eventHandlerProps.onKeyDown = (event) => {
      if (event.key === 'Enter') {
        setTimeout(onOpen, enterDelay);
      }
    };
  }

  if (trigger === 'hover') {
    eventHandlerProps.onFocus = onOpen;
    eventHandlerProps.onKeyDown = (event) => {
      if (event.key === 'Escape') {
        setTimeout(onClose, leaveDelay);
      }
    };
    eventHandlerProps.onBlur = onClose;
    eventHandlerProps.onMouseEnter = (event) => {
      isHoveringRef.current = true;
      openTimeout.current = setTimeout(() => {
        setEnableMouseMove(followCursor);
        onOpen();
      }, enterDelay || ((nextToCursor || followCursor) && 500));
    };
    eventHandlerProps.onMouseLeave = (event) => {
      isHoveringRef.current = false;
      setEnableMouseMove(true);
      if (openTimeout.current) {
        clearTimeout(openTimeout.current);
        openTimeout.current = null;
      }
      setTimeout(() => {
        if (isHoveringRef.current === false) {
          onClose();
        }
      }, leaveDelay || 100); // keep opening popover when cursor quick move from trigger element to popover.
    };
    eventHandlerProps.onMouseMove = (event) => {
      (enableMouseMove || followCursor) && setMouseCoordinate(event);
    };
  }

  const getPopoverTriggerProps = () => {
    const popoverTriggerStyleProps = {
      display: 'inline-flex',
    };

    return {
      'aria-haspopup': 'dialog',
      'aria-controls': popoverId,
      'aria-expanded': isOpen,
      ref: combinedRef,
      role: 'button',
      ...popoverTriggerStyleProps,
      ...eventHandlerProps,
    };
  };

  if (typeof children === 'function') {
    return children({ getPopoverTriggerProps });
  }

  return (
    <Box
      {...getPopoverTriggerProps()}
      {...rest}
    >
      {children}
    </Box>
  );
});

PopoverTrigger.displayName = 'PopoverTrigger';

export default PopoverTrigger;
