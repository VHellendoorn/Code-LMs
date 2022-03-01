/*
 * SystemJS Modules bootstrap
 * This file is designed to run as both a module, a script, a worker module, and a worker script
 *
 * For use with <script type="module"></script> it should be loaded via:
 *
 *   <script type="module" src="modules-bootstrap.js" main="dist-esm/main.js"></script>
 *   <script defer src="modules-bootstrap.js" main="dist-system/main.js" systemjs="system.js"></script>
 *
 * Works by checking if we are in a modules environment or not (feature-detectable by this === undefined)
 * If in a modules environment, it dynamically loads the modules main, otherwise it loads SystemJS and
 * then System.import loads the system module format main.
 */
(function (topLevelThis) {

var isModule = topLevelThis === undefined;
var global = self;

// this is needed for modules since document.currentScript doesn't seem supported
// implementation just returns the first <script type="module"> which is adequate for this case
function getCurrentModule () {
  var scripts = document.querySelectorAll('script');
  for (var i = 0; i < scripts.length; i++) {
    if (scripts[i].type !== 'module')
      continue;
    return scripts[i];
  }
}

// running as a <script type="module" msrc="main.js"></script>
// -> load the main.js
if (isModule) {
  var module = document.createElement('script');
  module.type = 'module';
  module.src = getCurrentModule().getAttribute('main');
  document.head.appendChild(module);
  global.__supportsScriptTypeModule = true;
}
// running as a <script msrc="main.js"></script>
// -> load SystemJS and then System.import('main.js')
else if (!global.__supportsScriptTypeModule) {
  var systemScript = document.createElement('script');
  systemScript.src = document.currentScript.getAttribute('systemjs') || 'system.js';

  var mainSrc = document.currentScript.getAttribute('main');

  systemScript.addEventListener('load', load, false);
  document.head.appendChild(systemScript);

  function load () {
    systemScript.removeEventListener('load', load, false);
    document.head.removeChild(systemScript);

    SystemJS.import(mainSrc);
  }
}
})(this);
