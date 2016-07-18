<?php

include "../include/json.php";

function getSSLPage($url) 
{
	$handle = popen("curl -s '$url'", "r");
	
	$read = "";
	
	while ($chunk = fread($handle, 8192))
	{
		$read .= $chunk;
	}	

	pclose($handle);
	return $read;
}

function dumpdat()
{
	$hspotsfile = "./points.hash";
	
	for ($lon = -180; $lon < 180; $lon++)
	{
		for ($lat = -90; $lat < 90; $lat++)
		{
			$donefile = "./done/$lon-$lat.done";
			if (file_exists($donefile)) continue;
			
			$url = "https://outgress.com/intel/heatmap-data.json?days=7&type=all&mine=1";
			
			$url .= "&lng1=" . $lon;
			$url .= "&lat1=" . $lat;
			$url .= "&lng2=" . ($lon + 1);
			$url .= "&lat2=" . ($lat + 1);
			
			echo "Url=$url\n";
			
			//
			// file_get_contents does not always work on OSX with HTTPS...
			//
			
			$cont = getSSLPage($url);
			if ($cont === false) continue;
			
			$json = json_decdat($cont);
			$points = $json[ "points" ];
			
			$GLOBALS[ "hspots" ] = $hspots = dba_open($hspotsfile, "c", "ndbm");
			
			foreach ($points as $hash => $value)
			{
				array_shift($value);
				
				$text = $value[ 0 ] . "," . $value[ 1 ] . "," . $value[ 2 ];
				
				$known = dba_exists($hash, $hspots) ? "-" : "+";
				
				if ($known == "+") dba_insert($hash, $text, $hspots);
				
				echo "$hash$known=>$text\n";
			}
			
			dba_close($hspots);
			unset($GLOBALS[ "hspots" ]);
			touch($donefile);
		}
	}
}

function details()
{
	$hspotsfile = "./points.hash";

	$hspots = dba_open($hspotsfile, "r", "ndbm");
	
	for($key = dba_firstkey($hspots); $key != false; $key = dba_nextkey($hspots)) 
	{
        $val = dba_fetch($key, $hspots);
        
        echo "$key => $val\n";
    }
    
	dba_close($hspots);
}

//dumpdat();
details();

?>
