import system from '../core/system';

const group = 'color';
const config = {
  color: {
    property: 'color',
    scale: 'colors',
  },
  colorScheme: true,
  /**
   * The CSS `fill` property for SVG elements
   */
  fill: {
    property: 'fill',
    scale: 'colors',
  },
  /**
   * The CSS `stroke` property for SVG elements
   */
  stroke: {
    property: 'stroke',
    scale: 'colors',
  },
};

const color = system(config, { group });

export default color;
