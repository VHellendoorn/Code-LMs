const path = require('path');
const webpack = require('webpack');
const Dotenv = require('dotenv-webpack');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');


module.exports = {
  mode: 'production',
  entry: `./src/umd.js`,
  output: {
    library: 'Core',
    filename: 'index.[hash].js',
    path: path.resolve(__dirname, './build/umd/')
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['env', 'react'],
            plugins: ['transform-class-properties']
          }
        }
      },
      {
        test: /\.css$/,
        use: [MiniCssExtractPlugin.loader, 'css-loader']
      }
    ]
  },
  plugins: [
    new Dotenv({ path: `../client/.env` }),
    new MiniCssExtractPlugin({ filename: 'index.[hash].css' })
  ]
};
