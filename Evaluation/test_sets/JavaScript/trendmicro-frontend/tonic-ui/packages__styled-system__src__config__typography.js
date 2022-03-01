import system from '../core/system';

const group = 'typography';
const config = {
  fontFamily: {
    property: 'fontFamily',
    scale: 'fonts',
  },
  fontSize: {
    property: 'fontSize',
    scale: 'fontSizes',
  },
  fontStyle: true,
  fontWeight: {
    property: 'fontWeight',
    scale: 'fontWeights',
  },
  letterSpacing: {
    property: 'letterSpacing',
    scale: 'letterSpacings',
  },
  lineBreak: true,
  lineHeight: {
    property: 'lineHeight',
    scale: 'lineHeights',
  },
  overflowWrap: true,
  textAlign: true,
  textEmphasis: true,
  textIndent: true,
  textJustify: true,
  textOverflow: true,
  textTransform: true,
  whiteSpace: true,
  wordBreak: true,
  wordSpacing: true,
};

const typography = system(config, { group });

export default typography;
