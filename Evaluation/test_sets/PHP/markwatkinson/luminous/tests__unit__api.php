<?php
if (php_sapi_name() !== 'cli') {
    die('This must be run from the command line');
}
/*
 * API test - tests the various configuration options
 */

include __DIR__ . '/helper.inc';

function assertSet($setting, $value)
{
    Luminous::set($setting, $value);
    $real = Luminous::setting($setting);
    if ($real !== $value) {
        echo "Set $setting to $value, but it is $real\n";
        assert(0);
    }
}

function assertSetException($setting, $value)
{
    $val = Luminous::setting($setting);
    $exception = false;
    try {
        Luminous::set($setting, $value);
    } catch (Exception $e) {
        $exception = true;
    }
    if (!$exception) {
        echo "set($setting, " . var_export($value, true) . ") failed to throw exception\n";
        assert(0);
    }
    assert($val === Luminous::setting($setting));
}

function testSet()
{
    // first we'll check legal settings
    $legalVals = array(
        'autoLink' => array(true, false),
        'cacheAge' => array(-1, 0, 1, 200, 100000000),
        'failureTag' => array(null, '', 'pre', 'table', 'div'),
        'format' => array('html', 'html-full', 'html-inline', 'latex', null, 'none'),
        'htmlStrict' => array(true, false),
        'includeJavascript' => array(true, false),
        'includeJquery' => array(true, false),
        'lineNumbers' => array(true, false),
        'startLine' => array(1, 2, 3, 100, 10000, 9999999),
        'maxHeight' => array(-1, 0, 1, 2, 3, 100, '100', '200px', '250%'),
        'relativeRoot' => array(null, '', 'xyz', '/path/to/somewhere/'),
        'theme' => Luminous::themes(),
        'wrapWidth' => array(-1, 0, 2, 3, 100, 10000000, 999999999),
        'highlightLines' => array(  array(0, 1, 2) )
    );

    foreach ($legalVals as $k => $vs) {
        foreach ($vs as $v) {
            assertSet($k, $v);
        }
    }

    // now the illegal ones should throw exceptions
    $illegalVals = array(
        'autoLink' => array(1, 0, 'yes', 'no', null),
        'cacheAge' => array(true, false, 1.1, 'all year', null),
        'failureTag' => array(true, false, array()),
        'format' => array('someformatter', '', true, false, 1, 2, 3),
        'htmlStrict' => array(1, 0, 'yes', 'no', null, array()),
        'includeJavascript' => array(1, 0, 'yes', 'no', null, array()),
        'includeJquery' => array(1, 0, 'yes', 'no', null, array()),
        'lineNumbers' => array(1, 0, 'yes', 'no', null, array()),
        'startLine' => array(0, -1, true, false, null, array()),
        'maxHeight' => array(null, true, false, array()),
        'relativeRoot' => array(1, 0, true, false, array()),
        'theme' => array('mytheme', null, true, false, 1, array()),
        'wrapWidth' => array('wide', 1.5, true, false, null, array()),
        'highlightLines' => array(1, 2, 3)
    );

    foreach ($illegalVals as $k => $vs) {
        foreach ($vs as $v) {
            assertSetException($k, $v);
        }
    }

    // finally, we're going to use the old fashioned array indices and check that
    // they still correspond to the new keys. The old fashioned way used dashes
    // to separate words in the array. For impl. reasons we had to switch these
    // to underscores, but they should be aliases of each other as far as the
    // API is concerned.

    // FIXME: The conversion needs to be adjusted for new camelCaps option names
    foreach ($legalVals as $k => $vs) {
        foreach ($vs as $v) {
            $kOld = str_replace('_', '-', $k);
            Luminous::set($k, $v);
            assert(Luminous::setting($kOld) === $v);
            Luminous::set($kOld, $v);
            assert(Luminous::setting($k) === $v);
        }
    }
}

function assertFormatterOption($setting, $value)
{
    // convert API setting name to the property name in the formatter
    $settingPropertyMap = array(
        'wrapWidth' => 'wrapLength',
        'maxHeight' => 'height',
        'htmlStrict' => 'strictStandards',
        'autoLink' => 'link',
        'lineNumbers' => 'lineNumbers',
    );
    Luminous::set($setting, $value);
    $formatter = Luminous::formatter();
    $mapped = $settingPropertyMap[$setting];
    $val = $formatter->$mapped;
    if ($val !== $value) {
        echo "formatter->$mapped == {$val}, should be $value\n";
        assert(0);
    }
}

function testFormatterOptions()
{
    // check that each of the formatter options is applied correctly to the
    // formatter.
    $formatters = array('html', 'html-full', 'html-inline', 'latex', 'none', null);
    foreach ($formatters as $f) {
        Luminous::set('format', $f);
        assertFormatterOption('wrapWidth', 1337);
        assertFormatterOption('wrapWidth', -1);
        assertFormatterOption('maxHeight', 100);
        assertFormatterOption('maxHeight', '100');
        assertFormatterOption('maxHeight', '100px');
        assertFormatterOption('maxHeight', 0);
        assertFormatterOption('maxHeight', -1);
        assertFormatterOption('lineNumbers', false);
        assertFormatterOption('lineNumbers', true);
        assertFormatterOption('autoLink', false);
        assertFormatterOption('autoLink', true);
        assertFormatterOption('htmlStrict', true);
        assertFormatterOption('htmlStrict', false);
    }
}

$sqlExecuted = false;
function sql($query)
{
    global $sqlExecuted;
    $sqlExecuted = true;
    return false;
}
// tests that setting the SQL function results in the SQL backend being used
function testCache()
{
    global $sqlExecuted;
    $sqlExecuted = false;
    Luminous::set('sql_function', 'sql');
    // this will throw a cache not creatable warning which we don't really care
    // about
    @Luminous::highlight('plain', '123', true);
    assert($sqlExecuted);
}

testSet();
testFormatterOptions();
testCache();
