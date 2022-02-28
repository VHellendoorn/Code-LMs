var htmlparser = require("htmlparser2");
var Rx = require('rx').Rx;


/**
 * Creates an observable sequence of Sax'd elements, matching tags
 * @param {String} html
 * @param {String|String[]} tags
 */
function createObservableSAX(html, tags) {
  return Rx.Observable.Create(function(obs){
    // quick lookup for tags
    var stack = [];
    var tagHash = {};
    if (tags instanceof Array) {
      for (var i in tags) {
        tagHash[tags[i].toLowerCase()] = 'true'; 
      }
    } else {
      tagHash[tags.toLowerCase()] = 'true'
    }
    
    var parser = new htmlparser.Parser({
      onopentag:function(name, attributes, type){
        if (type == 'tag') {
          stack.push({name:name.toLowerCase(), attributes:attributes});
        }
      },
      ontext:function(text){
        if (stack.length) {
          stack[stack.length - 1].text = text;
        }
      },
      onclosetag:function(){
        if (stack.length) {
          var top = stack.pop();
          if (tagHash[top.name]) {
            obs.OnNext(top);
          }
        }
      },
      lowerCaseTags:true
    });
    parser.write(html);
    parser.done();
    // work off any unbalanced tags
    while(stack.length){
      var top = stack.pop();
      if (tagHash[top.name]) {
        obs.OnNext(top);
      }
    }
    obs.OnCompleted();
    
    return function(){};
  })
}


exports.createObservableSAX = createObservableSAX;