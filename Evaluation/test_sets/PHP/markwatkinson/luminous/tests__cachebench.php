<?php
if (php_sapi_name() !== 'cli') die('This must be run from the command line');
include __DIR__ . '/../src/luminous.php';

$mysql = mysql_connect('localhost', 'testuser') or die(mysql_error());
$db = mysql_select_db('test');

function query($sql) {
  $r = mysql_query($sql) or die("Error in\n$sql\n" .  mysql_error());
  if (is_bool($r)) return $r;
  $ret = array();
  while ($row = mysql_fetch_assoc($r)) {
    $ret[] = $row;
  }
  return $ret;
}

$read_sql = array();
$write_sql = array();

$read_fs = array();
$write_fs = array();

for($i=0; $i<1000; $i++) {
  $data = '';
  for($j=0; $j<1024/8; $j++) {
    // unrolled for speed
    $data .= chr(rand(32, 126));
    $data .= chr(rand(32, 126));
    $data .= chr(rand(32, 126));
    $data .= chr(rand(32, 126));
    $data .= chr(rand(32, 126));
    $data .= chr(rand(32, 126));
    $data .= chr(rand(32, 126));
    $data .= chr(rand(32, 126));
  }
  $id = md5($data);
  $sql_cache = new LuminousSQLCache($id);
  $sql_cache->set_sql_function('query');
  $t = microtime(true);
  $sql_cache->write($data);
  $t1 = microtime(true);
  $data1 = $sql_cache->read();
  $t2 = microtime(true);

  $read_sql[] = $t2-$t1;
  $write_sql[] = $t1-$t;

  assert($data === $data1);

  $fs_cache = new LuminousFileSystemCache($id);
  $t = microtime(true);
  $fs_cache->write($data);
  $t1 = microtime(true);
  $data1 = $fs_cache->read();
  $t2 = microtime(true);
  $read_fs[] = $t2-$t1;
  $write_fs[] =  $t1-$t;
  assert($data === $data1);
}

echo "Average SQL read time:  " . (array_sum($read_sql)/count($read_sql)) . "\n";
echo "Average SQL write time: " . (array_sum($write_sql)/count($write_sql)) . "\n";

echo "Average FS read time:  " . (array_sum($read_fs)/count($read_fs)) . "\n";
echo "Average FS write time: " . (array_sum($write_fs)/count($write_fs)) . "\n";

