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

	//echo $curl . "\n";
	
	return getSSLPage($curl);
}

function getPortalDetails($guid)
{
	$portalfile = "./portals/$guid.json";
	
	if (file_exists($portalfile)) 
	{
		//$data = json_decdat(file_get_contents($portalfile));
		//echo "$guid::" . $data[ "result" ][ 4 ] . "\n";
		return;
	}
	
	$func = 'getPortalDetails';
	$json = '{"guid":"' . $guid . '","v":"3372ba001844bd4a42680f3e6a2372d2490580f9"}';
		
	$data = getCurlCommand($func, $json);
	$data = json_decdat($data);
	$json = json_encdat($data);
	
	$level = $data[ "result" ][ 4 ];
	
	echo $guid . "::" . $level . "=>" . strlen($json) . "\n";
	
	if (strlen($json) > 6) file_put_contents($portalfile, $json);
}

function readAllEntities($entities)
{
	$emax = count($entities);
	
	for ($einx = 0; $einx < $emax; $einx++)
	{
		$eguid = $entities[ $einx ][ 0 ];
		//echo "e=$eguid\n";
		
		$entity = $entities[ $einx ][ 2 ];
		
		$portals = $entity[ 2 ];
		
		if (is_array($portals[ 0 ]))
		{
			$pmax = count($portals);
	
			for ($pinx = 0; $pinx < $pmax; $pinx++)
			{
				$pguid = $portals[ $pinx ][ 0 ];
			
				getPortalDetails($pguid);
			}
		}
		else
		{
			$portals = $entity;
			$pmax = count($portals);
			
			if ($portals[ 0 ] == "p") 
			{
				getPortalDetails($eguid);
				
				continue;
			}
			
			for ($pinx = 2; $pinx < $pmax; $pinx += 3)
			{
				$qguid = $portals[ $pinx ];
			
				getPortalDetails($qguid);
			}
		}
	}
}

function readAllTiles()
{
	$tiledir = "./tiles";
	$dfd = opendir($tiledir);
	
	while (($file = readdir($dfd)) !== false)
	{
		if (($file == ".") || ($file == "..")) continue;
		
		$tilefile = $tiledir . "/" . $file;
		
		$fd = fopen($tilefile, "r");
		
		if (! flock($fd, LOCK_EX + LOCK_NB))
		{
			fclose($fd);
			continue;
		}
		
		$data = json_decdat(file_get_contents($tilefile));
		
		if (isset($data[ "result" ]) && isset($data[ "result" ][ "map" ]))
		{
			foreach ($data[ "result" ][ "map" ] as $tile => $tiledata)
			{
				if (isset($tiledata[ "gameEntities" ]))
				{
					readAllEntities($tiledata[ "gameEntities" ]);
				}
			}
		}
		
		flock($fd, LOCK_UN);
		fclose($fd);
	}
	
	closedir($dfd);
}

readAllTiles();

?>