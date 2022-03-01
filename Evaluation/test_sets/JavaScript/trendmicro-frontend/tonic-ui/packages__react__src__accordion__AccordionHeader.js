import { ensureBoolean } from 'ensure-type';
import React, { forwardRef } from 'react';
import { Box } from '../box';
import AccordionToggle from './AccordionToggle';
import AccordionToggleIcon from './AccordionToggleIcon';
import useAccordionItem from './useAccordionItem';
import { useAccordionHeaderStyle } from './styles';

const AccordionHeader = forwardRef((
  {
    children,
    disabled: disabledProp,
    ...rest
  },
  ref,
) => {
  const context = useAccordionItem(); // context might be an undefined value
  const ariaControls = context?.bodyId;
  const id = context?.headerId;
  const disabled = ensureBoolean(disabledProp ?? context?.disabled);
  const styleProps = useAccordionHeaderStyle({ disabled });

  return (
    <AccordionToggle
      aria-controls={ariaControls}
      id={id}
      ref={ref}
      {...styleProps}
      {...rest}
    >
      {children && (
        <Box>{children}</Box>
      )}
      <AccordionToggleIcon />
    </AccordionToggle>
  );
});

export default AccordionHeader;
