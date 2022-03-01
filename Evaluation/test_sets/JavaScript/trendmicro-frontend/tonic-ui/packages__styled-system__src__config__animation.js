import system from '../core/system';

const group = 'animation';
const config = {
  animation: true,
  animationDelay: true,
  animationDirection: true,
  animationDuration: true,
  animationFillMode: true,
  animationIterationCount: true,
  animationName: true,
  animationPlayState: true,
  animationTimingFunction: true,
};

const animation = system(config, { group });

export default animation;
