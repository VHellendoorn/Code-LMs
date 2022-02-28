<?php

namespace Luminous\Scanners;

use Luminous\Core\Utils;
use Luminous\Core\TokenPresets;
use Luminous\Core\Scanners\SimpleScanner;
use Luminous\Scanners\Keywords\VimScriptKeywords;

// I can't find some formal definition of vimscript's grammar.
// I'm pretty sure it's more complex than this, but, who knows.

class VimScriptScanner extends SimpleScanner
{
    public function stringOverride()
    {
        $comment = $this->bol();
        $this->skipWhitespace();
        assert($this->peek() === '"');
        if ($comment) {
            $this->record($this->scan("/.*/"), 'COMMENT');
        } else {
            if ($this->scan("/ \" (?> [^\n\"\\\\]+ | \\\\. )*$ /mx")) {
                $this->record($this->match(), 'COMMENT');
            } else {
                $m = $this->scan(TokenPresets::$DOUBLE_STR);
                assert($m !== null);
                $this->record($m, 'STRING');
            }
        }
    }

    public static function commentFilter($token)
    {
        $token = Utils::escapeToken($token);
        $str = &$token[1];
        // It pays to run the strpos checks first.
        if (strpos(substr($str, 1), '"') !== false) {
            $str = preg_replace('/(?<!^)"(?>[^"]*)"/', "<STRING>$0</STRING>", $str);
        }

        if (strpos($str, ':') !== false) {
            $str = preg_replace(
                '/(?<=^")((?>\W*))((?>[A-Z]\w+(?>(?>\s+\w+)*)))(:\s*)(.*)/',
                '$1<DOCTAG>$2</DOCTAG>$3<DOCSTR>$4</DOCSTR>',
                $str
            );
        }

        return $token;
    }

    public function init()
    {
        $this->addPattern('COMMENT_STRING', "/[\t ]*\"/");
        $this->addPattern('STRING', "/'(?>[^\n\\\\']+ | \\\\. )*'/x");
        $this->addPattern('NUMERIC', '/\#[a-f0-9]+/i');
        $this->addPattern('NUMERIC', TokenPresets::$NUM_HEX);
        $this->addPattern('NUMERIC', TokenPresets::$NUM_REAL);
        $this->addPattern('IDENT', '/[a-z_]\w*/i');
        $this->addPattern('OPERATOR', '@[~¬!%^&*\-=+;:,<.>/?\|]+@');

        $this->addIdentifierMapping('FUNCTION', VimScriptKeywords::$FUNCTIONS);
        $this->addIdentifierMapping('KEYWORD', VimScriptKeywords::$KEYWORDS);

        $this->removeStreamFilter('oo-syntax');
        $this->removeFilter('comment-to-doc');
        $this->addFilter('comment', 'COMMENT', array($this, 'commentFilter'));
        $this->overrides = array('COMMENT_STRING' => array($this, 'stringOverride'));
    }
}
