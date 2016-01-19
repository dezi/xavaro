<?php

include("../include/json.php");

$rawdata = "./astra.tv.json";

if (! file_exists($rawdata))
{
	$rawjson = file_get_contents("http://www.astra.de/webservice/v3/channels"
			 . "?limit=9999&page=1&sort_by=name&sort_direction=asc"
			 . "&domain_identifier=d3d3LmFzdHJhLmRl&free=yes&pay=yes&mode=tv");

	$json = json_decdat($rawjson);

	file_put_contents($rawdata,json_encdat($json));
}

?>
