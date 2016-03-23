<?php

include("../include/json.php");

$raw = file_get_contents("http://bulk.openweathermap.org/sample/city.list.json.gz");
file_put_contents("city.list.json.gz",$raw);
system("gunzip < city.list.json.gz > city.list.json");

$lines = file("city.list.json");
$lcount = count($lines);
$sort = array();

for ($inx = 0; $inx < $lcount; $inx++)
{
	$city = json_decdat($lines[ $inx ]);
	
	$id = $city[ "_id" ];
	while (strlen($id) < 8) $id = "0" . $id;
	
	$name = $city[ "name" ];
	$country = strtolower($city[ "country" ]);
	
	$lat = round ($city[ "coord" ][ "lat" ], 3);
	$lon = round ($city[ "coord" ][ "lon" ], 3);
	
	$line = "$id|$country|$name|$lat|$lon\n";
	$key = "$country|$name";
	
	$sort[ $key ] = $line;
}

ksort($sort);

$content = "";

foreach ($sort as $key => $line)
{
	$content .= $line;
}

$fd = fopen("city.csv", "w");
fwrite($fd, $content);
fclose($fd);

$gzencoded = gzencode($content, 9);

if ($fp = fopen("city.csv.gz","w"))
{
	fwrite($fp, $gzencoded);
	fclose($fp);
}

?>
