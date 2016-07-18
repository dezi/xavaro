<?php

include "../include/json.php";

function getSSLPage($curl) 
{
	$handle = popen($curl, "r");
	
	$read = "";
	
	while ($chunk = fread($handle, 8192))
	{
		$read .= $chunk;
	}	

	pclose($handle);
	return $read;
}

function getCurlCommand($func, $json)
{
	$curl = "curl -s ";
	
	$curl .= '-H "X-CSRFToken: Tk6DgctziZc18oeI8HDnSx7zFUCAKCyZ" ';
	$curl .= '-H "Referer: https://www.ingress.com/intel" ';
	$curl .= '-H "Content-Type: application/json" ';

	$curl .= '-H "Cookie: ';
	$curl .= 'csrftoken=';
	$curl .= 'Tk6DgctziZc18oeI8HDnSx7zFUCAKCyZ';
	
	$curl .= '; ';
	
	$curl .= 'SACSID=';
	//$curl .= '~AJKiYcELSLi-NKAonF1gzeAaObN8hvsHcR5t_AOwIw1WROYz-A3QROxk6toPqwT0GuvCusMGpqbMyq9EkAWkkJ0zNwmayK_m3lhbCEfvuVLmUPdqjDh7kqE08SAaPZ8_YA0EaiPwUK33BeIDHX8S93IrPkZH9BwmXkGNh2pX-bAU4acvQCwJ6ULFuOGdtXRll4sdSjVlkIthlCptJ4VylmpcsSScDaW-hxYMPMrh7EzP0IgLLh8dW-rZ3rjxTqT28U2uJQgSU6pSrsUNUoeaRYEciO6WHSZBsxurXhFA1YLc1YsKZmxjMnOl3TfhUSZFM7gcm5WHhzYXD7Z6ZI9H0pl8GZYWGwQZlw';
	$curl .= '~AJKiYcH2Nt-aZGTHOdQjrrPNdkL-oZzwEwX-n1BIn-M4STxsPJEW6yCJdJUJae9uN69nn4r_HRzF2RLPP-mdEKmy9uZ-PCn9OEF8FcRm8sryfKoPfcDWJjc-IVIRo7KaoD_PDDSLTGqx42Q97csXjjQ2exhfq6VN89hoAwjc1WdgKn8f7-200nVvkvH4yFoUPNsDN__adoLPdG3Au8IkyzlwVlxF9RRqafq5T0nlmko8aee4f3qF6poFSwGe8Xqpsvg2uRI6AyhPlRZloIJD0YS3rAaDo5AqhlTzLX3oPsh0vBffpJFyZJoyPetzgMrxvWh2JbfsI_MLSOJC5-5rH97Xzn1NZTQXcQ';
	
	$curl .= '" ';
	
	$curl .= '-X POST -d ';
	$curl .= '\'';
	$curl .= $json;
	$curl .= '\' ';

	$curl .= 'https://www.ingress.com/r/' . $func;

	//echo $curl . "\n"; exit();
	
	return getSSLPage($curl);
}

function getPortalDetails($guid)
{
	$func = 'getPortalDetails';
	$json = '{"guid":"' . $guid . '","v":"3372ba001844bd4a42680f3e6a2372d2490580f9"}';
	
	return getCurlCommand($func, $json);
}

function getTileData($pad, $z, $x, $y, $level)
{
	$zpad = str_pad($z, 2, "0", STR_PAD_LEFT);
	$xpad = str_pad($x, $pad, "0", STR_PAD_LEFT);
	$ypad = str_pad($y, $pad, "0", STR_PAD_LEFT);
	
	$tile = $z . "_" . $x . "_" . $y . "_" . $level . "_8_100";
	$tilepad = $zpad . "_" . $xpad . "_" . $ypad . "_" . $level . "_8_100";
	
	$tilefile = "./tiles/$tilepad.json";
	if (file_exists($tilefile)) return;
	
	$func = 'getEntities';
	$json = '{"tileKeys":["' . $tile . '"],"v":"3372ba001844bd4a42680f3e6a2372d2490580f9"}';
	
	$data = getCurlCommand($func, $json);
	$data = json_decdat($data);
	$json = json_encdat($data);
	
	if (strlen($json) > 6) file_put_contents($tilefile, $json);
	
	$count = "-";
	
	if (isset($data[ "result" ])
	 && isset($data[ "result" ][ "map" ])
	 && isset($data[ "result" ][ "map" ][ $tile ])
	 && isset($data[ "result" ][ "map" ][ $tile ][ "gameEntities" ]))
	{
		$count = count($data[ "result" ][ "map" ][ $tile ][ "gameEntities" ]);
	}
	 
	echo "$tilepad.json=$count\n";

	return $data;
}

/*
for ($x = 160; $x < 320; $x++)
{
	for ($y = 60; $y < 240; $y++)
	{
		getTileData(3, 7, $x, $y, 0);
	}
}
*/

/*
for ($x = 0; $x < 1000; $x += 10)
{
	for ($y = 0; $y < 1000; $y += 10)
	{
		getTileData(3, 8, $x, $y, 0);
	}
}
*/

/*
for ($x = 16000; $x < 32000; $x += 100)
{
	for ($y = 16000; $y < 32000; $y += 100)
	{
		getTileData(5, 15, $x, $y, 0);
	}
}
*/

for ($x = 8000; $x < 16000; $x += 500)
{
	for ($y = 2000; $y < 8000; $y += 500)
	{
		getTileData(5, 14, $x, $y, 0);
	}
}

//echo getPortalDetails("1912063a06c041078bb89acf6085f492.16") . "\n";

?>
