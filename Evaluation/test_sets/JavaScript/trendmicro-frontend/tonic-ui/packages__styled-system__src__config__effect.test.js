import effect from './effect';

test('returns effect styles', () => {
  const style = effect({
    theme: {
      shadows: {
        small: '0 1px 4px rgba(0, 0, 0, .125)',
      },
    },
    backdropFilter: 'blur(20px)',
    backgroundBlendMode: 'screen',
    boxShadow: 'small',
    filter: 'blur(5px)',
    mixBlendMode: 'multiply',
    opacity: 0.5,
  });
  expect(style).toEqual({
    backdropFilter: 'blur(20px)',
    backgroundBlendMode: 'screen',
    boxShadow: '0 1px 4px rgba(0, 0, 0, .125)',
    filter: 'blur(5px)',
    mixBlendMode: 'multiply',
    opacity: 0.5,
  });
});
