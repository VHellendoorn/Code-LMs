"use strict";

var _ = require("underscore");
var util = require("substance-util");
var Document = require("../index");
var TextNode = Document.TextNode;

// setting a default splitInto property:

var Paragraph = function(node, doc) {
  TextNode.call(this, node, doc);
};
Paragraph.Prototype = function() {};
Paragraph.Prototype.prototype = TextNode.prototype;
Paragraph.prototype = new Paragraph.Prototype();

var Heading = function(node, doc) {
  TextNode.call(this, node, doc);
};
Heading.Prototype = function() {
  this.splitInto = "paragraph";
}
Heading.Prototype.prototype = TextNode.prototype;
Heading.prototype = new Heading.Prototype();

var ImageNode = function(node, doc) {
  Document.Node.call(this, node, doc);
};
ImageNode.Prototype = function() {
  this.getLength = function() {
    return 1;
  };
  this.deleteOperation = function() {
    return null;
  };
};
ImageNode.Prototype.prototype = Document.Node.prototype;
ImageNode.prototype = new ImageNode.Prototype();
Document.Node.defineProperties(ImageNode.prototype, ["url"]);

var List = function(node, doc) {
  Document.Composite.call(this, node, doc);
};
List.Prototype = function() {
  this.getLength = function() {
    return this.properties.items.length;
  };
  this.getNodes = function() {
    return _.clone(this.properties.items);
  };
  this.isMutable = function() {
    return true;
  };
  this.insertChild = function(doc, pos, nodeId) {
    doc.update([this.id, "items"], ["+", pos, nodeId]);
  };
  this.deleteChild = function(doc, nodeId) {
    var pos = this.items.indexOf(nodeId);
    doc.update([this.id, "items"], ["-", pos, nodeId]);
    doc.delete(nodeId);
  };
  this.canJoin = function(other) {
    return (other.type === "list");
  };
  this.isBreakable = function() {
    return true;
  };
  this.break = function(doc, childId, charPos) {
    var childPos = this.properties.items.indexOf(childId);
    if (childPos < 0) {
      throw new Error("Unknown child " + childId);
    }
    var child = doc.get(childId);
    var newNode = child.break(doc, charPos);
    doc.update([this.id, "items"], ["+", childPos+1, newNode.id]);
    return newNode;
  };
};
List.Prototype.prototype = Document.Composite.prototype;
List.prototype = new List.Prototype();
Document.Node.defineProperties(List.prototype, ["items"]);

var Figure = function(node, doc) {
  Document.Composite.call(this, node, doc);
};
Figure.Prototype = function() {
  this.getLength = function() {
    return 2;
  };
  this.getNodes = function() {
    var result = [];
    // TODO: we should allow an empty image (e.g., for boot strapping)
    if (this.properties.image) result.push(this.properties.image);
    if (this.properties.caption) result.push(this.properties.caption);
    return result;
  };
  this.deleteChild = function(doc, nodeId) {
    if (nodeId === this.image) {
      doc.set([this.id, "image"], null);
      doc.delete(nodeId);
    } else if (nodeId === this.caption) {
      doc.set([this.caption, "content"], "");
    }
  }
};
Figure.Prototype.prototype = Document.Composite.prototype;
Figure.prototype = new Figure.Prototype();
Document.Node.defineProperties(Figure.prototype, ["image", "caption"]);

var Schema = util.clone(Document.schema);
_.extend(Schema.types, {
  "document": {
    "properties": {
      "views": ["array", "view"],
      "guid": "string",
      "creator": "string",
      "title": "string",
      "abstract": "string"
    }
  },
  "node": {
    "parent": "content",
    "properties": {}
  },
  "composite": {
    "parent": "node",
    "properties": {
      "nodes": ["array", "node"]
    }
  },
  "paragraph": {
    "parent": "node",
    "properties": {
      "content": "string"
    }
  },
  "heading": {
    "parent": "node",
    "properties": {
      "content": "string",
      "level": "number"
    }
  },
  "image": {
    "parent": "node",
    "properties": {
      "url": "string"
    }
  },
  "list": {
    "parent": "composite",
    "properties": {
      "items": ["array", "paragraph"]
    }
  },
  "figure": {
    "parent": "composite",
    "properties": {
      "image": "image",
      "caption": "paragraph"
    }
  },
  "annotation": {
    "properties": {
      "path": ["array", "string"], // -> e.g. ["text_1", "content"]
      "range": "object"
    }
  },
  "strong": {
    "parent": "annotation",
    "properties": {
    }
  },
  "idea": {
    "parent": "annotation",
    "properties": {
    }
  },
});

var nodeTypes = {
  "paragraph": { Model: Paragraph },
  "heading": { Model: Heading },
  "image": { Model: ImageNode },
  "list": { Model: List },
  "figure": { Model: Figure }
};

var TestDocument = function(options) {
  options = options || {};

  options.schema = util.deepclone(Schema);

  if (options.seed === undefined) {
    options.seed = TestDocument.Seed;
  }

  // Call parent constructor
  // --------

  Document.call(this, options);

  this.nodeTypes = nodeTypes;
};

TestDocument.Prototype = function() {
  this.fromSnapshot = function(data, options) {
    options = options || {};
    options.seed = data;
    return new TestDocument(options);
  };
};
TestDocument.Prototype.prototype = Document.prototype;
TestDocument.prototype = new TestDocument.Prototype();

TestDocument.Schema = Schema;

TestDocument.Seed = {
  nodes : {
    document: {
      id: "document",
      type: "document",
      views: ["content"],
    },
    content: {
      id: "content",
      type: "view",
      nodes: []
    }
  }
};

TestDocument.Paragraph = Paragraph;
TestDocument.Heading = Heading;

module.exports = TestDocument;
