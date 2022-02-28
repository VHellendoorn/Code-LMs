import React from 'react';
import { Flex, Text } from '@tonic-ui/react';

const Error = ({ statusCode }) => {
  const errorDescription = {
    404: 'This page could not be found.',
  }[statusCode] || 'An error occurred on this page.';

  return (
    <Flex
      alignItems="center"
      justifyContent="center"
      height="100%"
    >
      <Text
        borderRight={1}
        borderColor="rgba(0, 0, 0, .3)"
        fontSize="1.5em"
        lineHeight="1.5em"
        fontWeight="semibold"
        pr="3x"
        verticalAlign="top"
      >
        {statusCode}
      </Text>
      <Text
        pl="3x"
        verticalAlign="middle"
      >
        {errorDescription}
      </Text>
    </Flex>
  );
};

Error.getInitialProps = ({ res, err }) => {
  let statusCode = -1;

  if (res) {
    statusCode = res.statusCode;
  } else if (err) {
    statusCode = err.statusCode;
  } else {
    statusCode = 404;
  }

  return { statusCode };
};

export default Error;
