<?php
require_once(realpath(__DIR__ . '/../../autoload.php'));

$lat = 28.608389;
$long = -80.604333;
$geo_obj = new stdClass();
$geo_obj->type = "Point";
$geo_obj->coordinates = [$long, $lat];
$loc = new \Aerospike\GeoJSON($geo_obj);

echo "\n\n var_dump(getType())\n";
var_dump($loc->getType());
echo "\n\n var_dump(tostring())\n";
var_dump($loc->__toString());
echo "\n\n jsonserialize.\n";
echo json_encode(new \Aerospike\GeoJSON($geo_obj));
var_dump($loc->jsonSerialize());
echo "\n\n var_dump(toarray())\n.";
var_dump($loc->toArray());

$rect = \Aerospike\GeoJSON::fromArray([
    "type" => "Polygon",
    "coordinates" => [[
        [28.60000, -80.590000],
        [28.61800, -80.590000],
        [28.61800, -80.620000],
        [28.600000,-80.620000]]]]);

echo "\n\n var_dump(rect) from array.\n";
var_dump($rect);

echo "\n\n rect ch toarray().\n";
var_dump($rect->toArray());

$json = '{"type":1, "coordinates":[[28.60000, 80.8786], [28.61800, -80.590000], [28.61800, -80.620000], [28.600000,-80.620000]]}';
$json_rect = \Aerospike\GeoJSON::fromJson($json);
echo "\n\n var_dump(react_json).\n";
var_dump($json_rect);
?>
