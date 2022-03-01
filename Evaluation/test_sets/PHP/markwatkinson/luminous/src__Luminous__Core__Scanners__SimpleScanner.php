<?php

/** @cond CORE */

namespace Luminous\Core\Scanners;

/**
 * @brief A largely automated scanner
 *
 * LuminousSimpleScanner implements a main() method and observes the
 * patterns added with Scanner::add_pattern()
 *
 * An overrides array allows the caller to override the handling of any token.
 * If an override is set for a token, the override is called when that token is
 * reached and the caller should consume it. If the callback fails to advance
 * the string pointer, an Exception is thrown.
 */
class SimpleScanner extends Scanner
{
    /**
     * @brief Overrides array.
     *
     * A map of TOKEN_NAME => callback.
     *
     * The callbacks are fired by main() when the TOKEN_NAME rule matches.
     * The callback receives the match_groups array, but the scanner is
     * unscan()ed before the callback is fired, so that the pos() is directly
     * in front of the match. The callback is responsible for consuming the
     * token appropriately.
     */
    protected $overrides = array();

    public function main()
    {
        while (!$this->eos()) {
            $index = $this->pos();
            if (($match = $this->nextMatch()) !== null) {
                $tok = $match[0];
                if ($match[1] > $index) {
                    $this->record(substr($this->string(), $index, $match[1] - $index), null);
                }
                $match = $this->match();
                if (isset($this->overrides[$tok])) {
                    $groups = $this->matchGroups();
                    $this->unscan();
                    $p = $this->pos();
                    $ret = call_user_func($this->overrides[$tok], $groups);
                    if ($ret === true) {
                        break;
                    }
                    if ($this->pos() <= $p) {
                        throw new Exception('Failed to consume any string in override for ' . $tok);
                    }
                } else {
                    $this->record($match, $tok);
                }
            } else {
                $this->record(substr($this->string(), $index), null);
                $this->terminate();
                break;
            }
        }
    }
}

/** @endcond CORE */
