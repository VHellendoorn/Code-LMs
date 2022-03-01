import {
  SVGIcon,
} from '@tonic-ui/react';

const routes = [
  {
    title: 'Getting Started',
    icon: 'list-open',
    path: 'getting-started',
    routes: [
      { title: 'Installation', path: 'getting-started/installation' },
      { title: 'Usage', path: 'getting-started/usage' },
      { title: 'Migration From v0.x to v1', path: 'getting-started/migration-v0x' },
      { title: 'Versions', path: 'getting-started/versions' },
      { title: 'Contributing', path: 'getting-started/contributing' },
    ],
  },
  {
    title: 'Styled System',
    icon: 'gavel',
    path: 'styled-system',
    routes: [
      { title: 'Style Props', path: 'styled-system/style-props' },
      { title: 'Pseudo Style Props', path: 'styled-system/pseudo-style-props' },
      { title: 'Responsive Styles', path: 'styled-system/responsive-styles' },
    ],
  },
  {
    title: 'Theme',
    icon: (props) => {
      return (
        <SVGIcon
          {...props}
          viewBox="0 0 325.04 325.04"
        >
          <g>
            <path d="M117.866,234.088c-2.956,14.532-4.875,21.558-16.092,22.458c-2.764,0.222-5.015,2.308-5.446,5.047
              c-0.432,2.738,1.069,5.416,3.631,6.477c0.721,0.298,17.877,7.308,37.921,7.309c0.003,0,0.005,0,0.007,0
              c13.968,0,25.95-3.386,35.612-10.063c11.45-7.912,19.344-20.294,23.541-36.788l-38.572-38.88
              C125.871,194.924,121.253,217.436,117.866,234.088z"/>
            <path d="M322.745,63.336c-1.037-1.046-2.887-2.293-5.806-2.293c-3.423,0-12.516,0-67.74,46.992
              c-25.509,21.706-54.92,48.559-78.314,71.41l36.603,36.894c24.061-25.009,52.129-56.355,74.451-83.258
              c14.096-16.986,24.935-31.002,32.216-41.657C323.799,77.311,328.023,68.655,322.745,63.336z"/>
            <path d="M182.595,278.479c-12.387,8.56-27.429,12.899-44.716,12.899c-22.753-0.001-41.919-7.649-44.046-8.527
              c-9.425-3.906-14.898-13.673-13.31-23.749c1.555-9.871,9.463-17.373,19.341-18.446c0.861-2.571,1.813-7.254,2.323-9.758
              c1.878-9.23,4.449-21.873,12.358-33.126c8.637-12.287,21.656-20.165,38.751-23.466c9.811-9.737,21.005-20.443,32.686-31.308
              c-5.905-1.281-11.185-5.127-14.017-10.944c-4.875-10.02-0.623-22.073,9.484-26.895c10.133-4.834,22.287-0.612,27.155,9.423
              c0.961,1.978,1.555,4.033,1.832,6.096c9.688-8.677,19.309-17.099,28.392-24.828c0.054-0.046,0.105-0.09,0.16-0.136
              c-10.209-19.536-24.849-36.845-42.687-50.098c-25.614-19.031-56.114-29.096-88.2-29.104c-0.01,0-0.017,0-0.025,0
              c-21.654,0-47.976,7.566-68.697,19.749C13.981,51.193-0.005,71.163,0,92.49c0.008,25.748,14.53,36.518,26.199,45.171
              c9.515,7.057,17.03,12.63,17.034,24.844c0.003,12.213-7.508,17.781-17.018,24.831c-11.665,8.648-26.184,19.412-26.176,45.163
              c0.006,21.324,14.001,41.299,39.406,56.244c20.736,12.198,47.072,19.78,68.73,19.786c0.015,0,0.028,0,0.042,0
              c39.305,0,76.254-15.171,104.044-42.72c20.837-20.655,34.656-46.416,40.273-74.442c-13.952,15.471-27.997,30.493-40.563,43.322
              C206.641,253.965,196.773,268.682,182.595,278.479z M111.054,77.103c2.498-10.871,13.4-17.657,24.354-15.167
              c10.939,2.478,17.793,13.282,15.313,24.138c-2.499,10.844-13.407,17.631-24.362,15.154
              C115.411,98.764,108.554,87.947,111.054,77.103z M45.054,114.152c-7.005-8.716-5.565-21.401,3.216-28.339
              c8.78-6.925,21.571-5.505,28.589,3.195c6.99,8.703,5.545,21.388-3.229,28.34C64.869,124.288,52.058,122.853,45.054,114.152z
              M55.746,247.168c-8.786-6.944-10.231-19.629-3.226-28.342c7-8.696,19.796-10.122,28.581-3.18
              c8.778,6.943,10.224,19.629,3.225,28.327C77.327,252.686,64.53,254.111,55.746,247.168z"/>
          </g>
        </SVGIcon>
      );
    },
    path: 'theme',
    routes: [
      { title: 'Borders', path: 'theme/borders' },
      { title: 'Breakpoints', path: 'theme/breakpoints' },
      { title: 'Colors', path: 'theme/colors' },
      { title: 'Fonts', path: 'theme/fonts' },
      { title: 'Font Sizes', path: 'theme/font-sizes' },
      { title: 'Font Weights', path: 'theme/font-weights' },
      { title: 'Letter Spacings', path: 'theme/letter-spacings' },
      { title: 'Line Heights', path: 'theme/line-heights' },
      { title: 'Outlines', path: 'theme/outlines' },
      { title: 'Radii', path: 'theme/radii' },
      { title: 'Shadows', path: 'theme/shadows' },
      { title: 'Sizes', path: 'theme/sizes' },
      { title: 'Space', path: 'theme/space' },
      { title: 'zIndices', path: 'theme/z-indices' },
    ],
  },
  {
    title: 'React Components',
    icon: 'widgets',
    path: 'components',
    routes: [
      { title: 'COLORS', heading: true },
      { title: 'Color Mode', path: 'components/color-mode' },
      { title: 'Color Style', path: 'components/color-style' },

      { title: 'DATA DISPLAY', heading: true },
      { title: 'Accordion', path: 'components/accordion' },
      { title: 'Badge', path: 'components/badge' },
      { title: 'Divider', path: 'components/divider' },
      { title: 'Drawer', path: 'components/drawer' },
      { title: 'Modal', path: 'components/modal' },
      { title: 'Popover', path: 'components/popover' },
      { title: 'Table', path: 'components/table' },
      { title: 'Tag', path: 'components/tag' },
      { title: 'Tooltip', path: 'components/tooltip' },

      { title: 'FEEDBACK', heading: true },
      { title: 'Alert', path: 'components/alert' },
      { title: 'Skeleton', path: 'components/skeleton' },
      { title: 'Spinner', path: 'components/spinner' },
      { title: 'Toast', path: 'components/toast' },

      { title: 'FORMS', heading: true },
      { title: 'Button', path: 'components/button' },
      { title: 'ButtonBase', path: 'components/buttonbase' },
      { title: 'ButtonGroup', path: 'components/buttongroup' },
      { title: 'Checkbox', path: 'components/checkbox' },
      { title: 'CheckboxGroup', path: 'components/checkboxgroup' },
      { title: 'FlatButton', path: 'components/flatbutton' },
      { title: 'Input', path: 'components/input' },
      { title: 'InputBase', path: 'components/inputbase' },
      { title: 'InputGroup', path: 'components/inputgroup' },
      { title: 'LinkButton', path: 'components/linkbutton' },
      { title: 'Radio', path: 'components/radio' },
      { title: 'RadioGroup', path: 'components/radiogroup' },
      { title: 'SearchInput', path: 'components/searchinput' },
      { title: 'Select', path: 'components/select' },
      { title: 'Switch', path: 'components/switch' },
      { title: 'Textarea', path: 'components/textarea' },

      { title: 'LAYOUT', heading: true },
      { title: 'Box', path: 'components/box' },
      { title: 'ControlBox', path: 'components/controlbox' },
      { title: 'Flex', path: 'components/flex' },
      { title: 'Grid', path: 'components/grid' },
      { title: 'Space', path: 'components/space' },
      { title: 'Stack', path: 'components/stack' },

      { title: 'MEDIA AND ICONS', heading: true },
      { title: 'Icon', path: 'components/icon' },
      { title: 'Image', path: 'components/image' },
      { title: 'SVGIcon', path: 'components/svgicon' },

      { title: 'NAVIGATION', heading: true },
      { title: 'Link', path: 'components/link' },
      { title: 'ButtonLink 🚧', path: 'components/buttonlink' },
      { title: 'Menu', path: 'components/menu' },
      { title: 'Pagination', path: 'components/pagination' },
      //{ title: 'Tabs', path: 'components/tabs' }, // TODO

      { title: 'TRANSITIONS', heading: true },
      { title: 'Transitions', path: 'components/transitions' },
      { title: 'Transitions / Collapse', path: 'components/transitions/collapse' },
      { title: 'Transitions / Fade', path: 'components/transitions/fade' },
      { title: 'Transitions / Grow', path: 'components/transitions/grow' },
      { title: 'Transitions / Scale', path: 'components/transitions/scale' },
      { title: 'Transitions / Slide', path: 'components/transitions/slide' },
      { title: 'Transitions / Zoom', path: 'components/transitions/zoom' },

      { title: 'TYPOGRAPHY', heading: true },
      { title: 'Text', path: 'components/text' },
      { title: 'TextLabel', path: 'components/textlabel' },

      { title: 'UTILS', heading: true },
      { title: 'CSSBaseline', path: 'components/cssbaseline' },
      { title: 'Presence 🚧', path: 'components/presence' },
      { title: 'Scrollbar', path: 'components/scrollbar' },
      { title: 'VisuallyHidden 🚧', path: 'components/visually-hidden' },
    ],
  },
  {
    title: 'React Hooks',
    icon: 'hook',
    path: 'hooks',
    routes: [
      { title: 'useConst', path: 'hooks/use-const' },
      { title: 'useEffectOnce', path: 'hooks/use-effect-once' },
      { title: 'useHydrated', path: 'hooks/use-hydrated' },
      { title: 'useIsomorphicEffect', path: 'hooks/use-isomorphic-effect' },
      { title: 'useLatest', path: 'hooks/use-latest' },
      { title: 'usePrevious', path: 'hooks/use-previous' },
      { title: 'useToggle', path: 'hooks/use-toggle' },
    ],
  },
];

export {
  routes,
};
