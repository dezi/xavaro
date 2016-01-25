<?php

include("../include/json.php");

function sortChannels($a, $b)
{
	$starta = mb_strtolower($a[ "name" ], "UTF-8");
	$startb = mb_strtolower($b[ "name" ], "UTF-8");
	
	return ($starta > $startb) ? 1 : -1;
}

function compressChannels()
{
	$channelsdir  = "../../var/channels";
	echo "$channelsdir\n";
	
	$ch_dfd = opendir($channelsdir);
	$ch_arr = array();
	
	while (($type = readdir($ch_dfd)) !== false)
	{
		if (($type == ".") || ($type == "..")) continue;
		
		$typesdir = "$channelsdir/$type";
		if (! is_dir($typesdir)) continue;
		echo "$typesdir\n";
		
		$ty_dfd = opendir($typesdir);
		$ty_arr = array();

		while (($country = readdir($ty_dfd)) !== false)
		{
			if (($country == ".") || ($country == "..")) continue;

			$countrydir = "$typesdir/$country";
			if (! is_dir($countrydir)) continue;
			echo "$countrydir\n";

			$co_dfd = opendir($countrydir);
			$co_arr = array();

			while (($channel = readdir($co_dfd)) !== false)
			{
				if (($channel == ".") || ($channel == "..")) continue;
								
				$channelfile = "$countrydir/$channel";
				if (substr($channelfile, -5) != ".json") continue;
				echo "$channelfile\n";

				$channeljson = json_decdat(file_get_contents($channelfile));

				$co_arr[] = $channeljson;
				$ty_arr[] = $channeljson;
				$ch_arr[] = $channeljson;
			}
	
			closedir($co_dfd);		
		
			usort($co_arr, "sortChannels");
			$co_json = json_encdat($co_arr);
			$co_file = "$countrydir.json.gz";
			file_put_contents($co_file, gzencode($co_json, 9));
		}
		
		closedir($ty_dfd);	
			
		usort($ty_arr, "sortChannels");
		$ty_json = json_encdat($ty_arr);
		$ty_file = "$typesdir.json.gz";
		file_put_contents($ty_file, gzencode($ty_json, 9));
	}
	
	closedir($ch_dfd);
				
	usort($ch_arr, "sortChannels");
	$ch_json = json_encdat($ch_arr);
	$ch_file = "$channelsdir.json.gz";
	file_put_contents($ch_file, gzencode($ch_json, 9));
}

compressChannels();

?>
