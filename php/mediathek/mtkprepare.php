<?php

include("../include/json.php");

$GLOBALS[ "bootstrapurl" ] = "http://zdfmediathk.sourceforge.net/akt.xml";
$GLOBALS[ "mtkdirectory" ] = "../../var/mtkdata/tv/de";

function prepareList()
{
	$filexy = $GLOBALS[ "mtkdirectory" ] . "/rawdata.xy";

	$pfd = popen("xzcat < $filexy", "r");
	
	$lines = 0;
	$channel = null;
	
	while (($line = fgets($pfd)) !== false)
	{
		$line = trim($line);
		
		if (substr($line, 0, 7) != "\"X\" : [") continue;
		
		if (substr($line, -1) == ",")
		{
			$show = json_decdat(substr($line, 6, -1));
		}
		else
		{
			$show = json_decdat(substr($line, 6));
		}

		//var_dump($show);
		//exit(0);
		
		if (($show[ 0 ] != "") && ($show[ 0 ] != $channel))
		{
			$channel = $show[ 0 ];
			error_log("Channel: $channel");
		}
		
		$lines++;
	}
	
	pclose($pfd);
	
	error_log("Lines: $lines\n");
}

function getLatestList()
{
	if (! file_exists($GLOBALS[ "mtkdirectory" ])) 
	{
		mkdir($GLOBALS[ "mtkdirectory" ], 0775, true);
	}
	
	if (! file_exists($GLOBALS[ "mtkdirectory" ])) 
	{
		error_log("Cannot create directory: " . $GLOBALS[ "mtkdirectory" ]);
		
		exit(1);
	}
	
	$serverxml = file_get_contents($GLOBALS[ "bootstrapurl" ]);
	$serverxml = str_replace("\r", "\n", $serverxml);
	$serverxml = str_replace("\n\n", "\n", $serverxml);
	
	preg_match_all("/(http:|https:)([a-zA-Z0-9.\\-\\/]+)/", $serverxml, $matches);

	foreach ($matches[ 0 ] as $index => $serverurl)
	{
		$listxy = file_get_contents($serverurl);
		$filexy = $GLOBALS[ "mtkdirectory" ] . "/rawdata.xy";
		
		if (file_put_contents($filexy, $listxy))
		{
			error_log("Wrote $filexy");
			
			break;
		}
	}
}

//getLatestList();
prepareList();

?>