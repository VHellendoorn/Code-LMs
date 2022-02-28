import compose from './core/compose';
import animation from './config/animation';
import background from './config/background';
import border from './config/border';
import color from './config/color';
import containment from './config/containment';
import effect from './config/effect';
import flexbox from './config/flexbox';
import gap from './config/gap';
import grid from './config/grid';
import image from './config/image';
import interactivity from './config/interactivity';
import layout from './config/layout';
import listStyle from './config/list-style';
import margin from './config/margin';
import outline from './config/outline';
import padding from './config/padding';
import position from './config/position';
import shape from './config/shape';
import text from './config/text';
import transform from './config/transform';
import transition from './config/transition';
import typography from './config/typography';

const system = compose(
  animation,
  background,
  border,
  color,
  containment,
  effect,
  flexbox,
  gap,
  grid,
  image,
  interactivity,
  layout,
  listStyle,
  margin,
  outline,
  padding,
  position,
  shape,
  text,
  transform,
  transition,
  typography,
);

export default system;
