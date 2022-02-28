import compose from '../../src/core/compose';
import system from '../../src/core/system';

const color = system({
  color: true,
  bg: {
    property: 'backgroundColor',
  },
});

const fontSize = system({
  fontSize: {
    property: 'fontSize',
    scale: 'fontSizes',
  },
});

test('compose combines style parsers', () => {
  const parser = compose(
    color,
    fontSize
  );
  const styles = parser({
    color: 'tomato',
    bg: 'black',
    fontSize: 32,
  });
  expect(typeof parser).toBe('function');
  expect(styles).toEqual({
    fontSize: 32,
    color: 'tomato',
    backgroundColor: 'black',
  });
});
