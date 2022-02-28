<?php
/**
 * DokuWiki Plugin syntaxhighlighter4 (Action Component).
 *
 * @license GPL 2 http://www.gnu.org/licenses/gpl-2.0.html
 * @author  CrazyMax <contact@crazymax.dev>
 */

// must be run within Dokuwiki
if (!defined('DOKU_INC')) {
  die();
}

class action_plugin_syntaxhighlighter4 extends DokuWiki_Action_Plugin {

  /**
   * Registers a callback function for a given event.
   *
   * @param Doku_Event_Handler $controller DokuWiki's event controller object
   *
   * @return void
   */
  public function register(Doku_Event_Handler $controller) {
    $controller->register_hook('TPL_METAHEADER_OUTPUT', 'BEFORE', $this, 'handle_metaheader');
    $controller->register_hook('TPL_ACT_RENDER', 'AFTER', $this, 'handle_jsprocessing');
  }

  /**
   * [Custom event handler which performs action].
   *
   * @param Doku_Event $event event object by reference
   * @param mixed    $param [the parameters passed as fifth argument to register_hook() when this
   *              handler was registered]
   *
   * @return void
   */
  public function handle_metaheader(Doku_Event $event, $param) {
    // Add SyntaxHighlighter theme.
    $event->data['link'][] = array(
      'rel' => 'stylesheet',
      'type' => 'text/css',
      'href' => DOKU_BASE.'lib/plugins/syntaxhighlighter4/dist/'.$this->getConf('theme'),
    );

    // Override some CSS
    $event->data['link'][] = array(
      'rel' => 'stylesheet',
      'type' => 'text/css',
      'href' => DOKU_BASE.'lib/plugins/syntaxhighlighter4/dist/override.css',
    );

    // Register SyntaxHighlighter javascript.
    $event->data['script'][] = array(
      'type' => 'text/javascript',
      'src' => DOKU_BASE.'lib/plugins/syntaxhighlighter4/dist/syntaxhighlighter.js',
      '_data' => '',
    );
  }

  public function handle_jsprocessing(Doku_Event $event, $param) {
    global $ID, $INFO;

    // Ensures code will be written only on base page
    if ($ID != $INFO['id']) {
      return;
    }

    // Load Syntaxhighlighter config
    ptln('');
    ptln("<script type='text/javascript'>");
    ptln('syntaxhighlighterConfig = {');
    ptln('  autoLinks: '.($this->getConf('autoLinks') == 1 ? 'true' : 'false').',');
    $firstLine = $this->getConf('first-line');
    if ($firstLine > 0) {
      ptln('  firstLine: '.$firstLine.',');
    }
    ptln('  gutter: '.($this->getConf('gutter') == 1 ? 'true' : 'false').',');
    ptln('  htmlScript: '.($this->getConf('htmlScript') == 1 ? 'true' : 'false').',');
    $tabSize = $this->getConf('tabSize');
    if ($tabSize > 0) {
      ptln('  tabSize: '.$tabSize.',');
    }
    ptln('  smartTabs: '.($this->getConf('smartTabs') == 1 ? 'true' : 'false'));
    ptln('}');
    ptln('</script>');
  }
}
