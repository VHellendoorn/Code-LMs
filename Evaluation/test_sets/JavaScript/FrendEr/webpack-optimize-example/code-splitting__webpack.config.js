const path = require('path');

/**
 * loaders list
 */
const loaders = [
  {
    test: /\.jsx?$/,
    exclude: /node_modules/,
    loader: 'babel-loader',
    query: {
      presets: ['react', 'es2015', 'stage-0'],
      plugins: ['add-module-exports']
    }
  }
];

/**
 * plugins list
 */
const plugins = [];

module.exports = {
  entry: {
    index: path.join(__dirname, 'src/index.js')
  },
  output: {
    path: path.join(__dirname, 'build'),
    filename: '[name].js'
  },
  module: {
    loaders: loaders
  },
  plugins: plugins
};
