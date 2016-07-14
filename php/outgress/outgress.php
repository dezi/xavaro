<?php

include "../include/json.php";

function closedatabase()
{
	if (isset($GLOBALS[ "hspots" ])) dba_close($GLOBALS[ "hspots" ]);
	
	echo "Closed...\n";
}

function dumpdat()
{
	$hspotsfile = "./points.hash";
	
	for ($lon = 2; $lon < 180; $lon++)
	{
		for ($lat = 50; $lat < 90; $lat++)
		{
			$donefile = "./done/$lon-$lat.done";
			if (file_exists($donefile)) continue;
			
			$url = "https://outgress.com/intel/heatmap-data.json?days=7&type=all&mine=1";
			
			$url .= "&lng1=" . $lon;
			$url .= "&lat1=" . $lat;
			$url .= "&lng2=" . ($lon + 1);
			$url .= "&lat2=" . ($lat + 1);
			
			echo "Url=$url\n";
			
			$cont = file_get_contents($url);
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

//pcntl_signal(SIGTERM, "closedatabase");
//pcntl_signal(SIGHUP,  "closedatabase");
//pcntl_signal(SIGUSR1, "closedatabase");

dumpdat();

?>
