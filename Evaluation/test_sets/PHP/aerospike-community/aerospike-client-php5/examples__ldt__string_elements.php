<?php
################################################################################
# Copyright 2013-2015 Aerospike, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
################################################################################
require_once(realpath(__DIR__ . '/../examples_util.php'));

function parse_args() {
    $shortopts  = "";
    $shortopts .= "h::";  /* Optional host */
    $shortopts .= "p::";  /* Optional port */
    $shortopts .= "a";    /* Optionally annotate output with code */
    $shortopts .= "c";    /* Optionally clean up before leaving */

    $longopts  = array(
        "host::",         /* Optional host */
        "port::",         /* Optional port */
        "annotate",       /* Optionally annotate output with code */
        "clean",          /* Optionally clean up before leaving */
        "help",           /* Usage */
    );
    $options = getopt($shortopts, $longopts);
    return $options;
}

$args = parse_args();
if (isset($args["help"])) {
    echo "php llist.php [-hHOST] [-pPORT] [-a] [-c]\n";
    echo " or\n";
    echo "php llist.php [--host=HOST] [--port=PORT] [--annotate] [--clean]\n";
    exit(1);
}
$HOST_ADDR = (isset($args["h"])) ? (string) $args["h"] : ((isset($args["host"])) ? (string) $args["host"] : "localhost");
$HOST_PORT = (isset($args["p"])) ? (integer) $args["p"] : ((isset($args["port"])) ? (string) $args["port"] : 3000);

echo colorize("Connecting to the host ≻", 'black', true);
$start = __LINE__;
$config = array("hosts" => array(array("addr" => $HOST_ADDR, "port" => $HOST_PORT)));
$db = new Aerospike($config, false);
if (!$db->isConnected()) {
    echo fail("Could not connect to host $HOST_ADDR:$HOST_PORT [{$db->errorno()}]: {$db->error()}");
    exit(1);
}
echo success();
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Adding a record to test.shows with PK='futurama' ≻", 'black', true);
$start = __LINE__;
$key = $db->initKey("test", "shows", "futurama");
$futurama = array("channel" => array("Fox" => array(1999,2000,2001,2002),
                                     "Comedy Central" => array(2008,2009,2010,2011,2012)),
                  "creator" => array("Matt Groening", "David X. Cohen"),
                  "show" => "Futurama");
$options = array(Aerospike::OPT_POLICY_KEY => Aerospike::POLICY_KEY_SEND);
$status = $db->put($key, $futurama, 0, $options);
if ($status === Aerospike::OK) {
    echo success();
} else {
    echo standard_fail($db);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Instantiating an LList representing bin 'characters' of the given record ≻", 'black', true);
$start = __LINE__;
require_once(realpath(__DIR__ . '/../../autoload.php'));
$characters = new \Aerospike\LDT\LList($db, $key, 'characters');
if ($characters->errorno() === Aerospike::OK) {
    echo success();
} else {
    echo standard_fail($characters);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Checking if the server actually has an LList at bin 'characters' of the record ≻", 'black', true);
$start = __LINE__;
if (!$characters->isLDT()) {
    echo fail("No LList exists yet at bin 'characters' of record {$key['key']}. Adding elements will initialize it.");
} else {
    echo success();
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Add an element to the record's LList bin ≻", 'black', true);
$start = __LINE__;
$character = "Philip J. Fry";
$status = $characters->add($character);
if ($status === Aerospike::OK) {
    echo success();
} else {
    echo standard_fail($characters);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Add several other elements to the record's LList bin ≻", 'black', true);
$start = __LINE__;
$others = array("Turanga Leela","Bender Bending Rodríguez","Dr. Amy Wong",
                    "Hermes Conrad","Professor Hubert J. Farnsworth", "Dr. John A. Zoidberg");
$status = $characters->addMany($others);
if ($status === Aerospike::OK) {
    echo success();
} else {
    echo standard_fail($characters);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Getting the full record ≻", 'black', true);
$start = __LINE__;
$status = $db->get($key, $record);
if ($status === Aerospike::OK) {
    echo success();
    var_dump($record);
} elseif ($status === Aerospike::ERR_RECORD_NOT_FOUND) {
    echo fail("Could not find a show with PK={$key['key']} in the set test.shows");
} else {
    echo standard_fail($db);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Counting the elements in the record's LList bin ≻", 'black', true);
$start = __LINE__;
$status = $characters->size($num_elements);
if ($status === Aerospike::OK) {
    echo success();
    echo colorize("There are $num_elements elements in the LList\n", 'green');
} else {
    echo standard_fail($characters);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Get the elements whose first letter is between 'B' and 'E' ≻", 'black', true);
$start = __LINE__;
$status = $characters->findRange("B", "E", $elements);
if ($status === Aerospike::OK) {
    echo success();
    var_dump($elements);
} else {
    echo standard_fail($characters);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Find the LList element 'Turanga Leela' ≻", 'black', true);
$start = __LINE__;
$status = $characters->find("Turanga Leela", $elements);
if ($status === Aerospike::OK) {
    echo success();
    var_dump($elements);
} else {
    echo standard_fail($characters);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Register a filter module that will be used with this LDT ≻", 'black', true);
$start = __LINE__;
$copied = copy(__DIR__.'/lua/keyfilters.lua', ini_get('aerospike.udf.lua_user_path').'/keyfilters.lua');
if (!$copied) {
    echo fail("Could not copy the local lua/keyfilters.lua to ". ini_get('aerospike.udf.lua_user_path'));
}
$status = $db->register(ini_get('aerospike.udf.lua_user_path').'/keyfilters.lua', "keyfilters.lua");
if ($status == Aerospike::OK) {
    echo success();
} elseif ($status == Aerospike::ERR_UDF_NOT_FOUND) {
    echo fail("Could not find the udf file lua/keyfilters.lua");
} else {
    echo standard_fail($db);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

echo colorize("Filter the LList for elements starting with the letter 'P'", 'black', true);
$start = __LINE__;
$status = $characters->scan($elements, 'keyfilters', 'range_filter', array('P','Q'));
if ($status === Aerospike::OK) {
    echo success();
    var_dump($elements);
} else {
    echo standard_fail($characters);
}
if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

if (isset($args['c']) || isset($args['clean'])) {
    $start = __LINE__;
    echo colorize("Destroying the LDT ≻", 'black', true);
    $status = $characters->destroy();
    if ($status === Aerospike::OK) {
        echo success();
    } else {
        echo standard_fail($db);
    }
    if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

    $start = __LINE__;
    echo colorize("Removing the record ≻", 'black', true);
    $status = $db->remove($key);
    if ($status === Aerospike::OK) {
        echo success();
    } else {
        echo standard_fail($db);
    }
    if (isset($args['a']) || isset($args['annotate'])) display_code(__FILE__, $start, __LINE__);

}

$db->close();
?>
