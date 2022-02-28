'use strict';
const path = require('path');
const fs = require('fs');
const userHome = require('user-home');
const message = require('../message');

exports.description = 'Global configuration file is valid';

const errors = {
  syntax(err, configPath) {
    return message.get('global-config-syntax', {
      message: err.message,
      path: configPath
    });
  },

  misc(configPath) {
    return message.get('global-config-misc', {
      path: configPath
    });
  }
};
exports.errors = errors;

exports.configPath = path.join(userHome, '.yo-rc-global.json');

exports.verify = async () => {
  if (!fs.existsSync(this.configPath)) {
    return null;
  }

  try {
    JSON.parse(fs.readFileSync(this.configPath, 'utf8'));
  } catch (error) {
    if (error instanceof SyntaxError) {
      return errors.syntax(error, this.configPath);
    }

    return errors.misc(this.configPath);
  }

  return null;
};
