"use strict";

var _ = require("underscore");
var util = require("substance-util");

// Document.Controller
// -----------------
//
// Provides means for editing and viewing a Substance.Document. It introduces
// a Selection API in order to move a cursor through the document, support
// copy and paste, etc.
//
// Note: it is quite intentional not to expose the full Substance.Document interface
//       to force us to explicitely take care of model adaptations.
//
// Example usage:
//
//     var doc = new Substance.Document();
//     var editor = new Substance.Document.Controller(doc);
//     var editor.insert("Hello World");

var Controller = function(document, options) {
  options = options || {};
  this.view = options.view || 'content';
  this.__document = document;
  this.container = document.get(this.view);
};

Controller.Prototype = function() {

  // Document Facette
  // --------

  this.getNodes = function(idsOnly) {
    return this.container.getNodes(idsOnly);
  };

  this.getContainer = function() {
    return this.container;
  };

  // Given a node id, get position in the document
  // --------
  //

  this.getPosition = function(id, flat) {
    return this.container.getPosition(id, flat);
  };

  this.getNodeFromPosition = function(nodePos) {
    return this.container.getNodeFromPosition(nodePos);
  };

  // See Annotator
  // --------
  //

  this.getAnnotations = function(options) {
    options = options || {};
    options.view = this.view;
    return this.annotator.getAnnotations(options);
  };

  // Delegate getter
  this.get = function() {
    return this.__document.get.apply(this.__document, arguments);
  };

  this.on = function() {
    return this.__document.on.apply(this.__document, arguments);
  };

  this.off = function() {
    return this.__document.off.apply(this.__document, arguments);
  };

  this.getDocument = function() {
    return this.__document;
  };

};

// Inherit the prototype of Substance.Document which extends util.Events
Controller.prototype = _.extend(new Controller.Prototype(), util.Events.Listener);

// Property accessors for convenient access of primary properties
Object.defineProperties(Controller.prototype, {
  id: {
    get: function() {
      return this.__document.id;
    },
    set: function() { throw "immutable property"; }
  },
  nodeTypes: {
    get: function() {
      return this.__document.nodeTypes;
    },
    set: function() { throw "immutable property"; }
  },
  title: {
    get: function() {
      return this.__document.get('document').title;
    },
    set: function() { throw "immutable property"; }
  },
  updated_at: {
    get: function() {
      return this.__document.get('document').updated_at;
    },
    set: function() { throw "immutable property"; }
  },
  creator: {
    get: function() {
      return this.__document.get('document').creator;
    },
    set: function() { throw "immutable property"; }
  }
});

module.exports = Controller;
