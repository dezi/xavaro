<?php

include("../include/json.php");
include("../moviefinder/moviefinder.php");

function sortInfos($a, $b)
{
	if ($a == $b) return 0;
		
	return ($a > $b) ? -1 : 1;
}

function checkSingleByteSpecial($title)
{
	$sbyte = false;
	
	$title = str_replace(chr(0xe2) . chr(0x85) . chr(0x9b), "Ü", $title);
	$title = str_replace(chr(0xc4) . chr(0xa6), "ä", $title);
	$title = str_replace(chr(0xc4) . chr(0xb3), "ö", $title);
	$title = str_replace(chr(0xc3) . chr(0xbe), "ü", $title);
	$title = str_replace(chr(0xe2) . chr(0x85) . chr(0x9e), "ß", $title);
		
	return $title;
}

function checkUnwanted($title)
{
	if ($title == "Thema:") return true;
	if ($title == "Magazin") return true;
	if ($title == "Technik") return true;
	if ($title == "Quickie") return true;
	if ($title == "Bilderbuch") return true;
	
	return false;
}

function readPrograms($countrydir, $channeldir)
{
	$pgminfodir = str_replace("epgdata", "pgminfo", $countrydir);
	
	$cd_dfd = opendir($channeldir);
	$cd_arr = array();

	$channel = basename($channeldir);
	
	while (($epgfile = readdir($cd_dfd)) !== false)
	{
		if (($epgfile == ".") || ($epgfile == "..")) continue;

		if (strlen($epgfile) !=	18) continue;
		if (substr($epgfile, -8) !=	".json.gz") continue;
			 
		$epgfile = "$channeldir/$epgfile";
		echo "$epgfile\n";
		
		$epgs = json_decdat(gzinflate(substr(file_get_contents($epgfile),10,-8)));
		if (! isset($epgs[ "epgdata" ])) continue;
		$epgs = $epgs[ "epgdata" ];
		
		foreach ($epgs as $inx => $val)
		{
			$title = $val[ "title" ];
			
			$title = preg_replace("/\\([^)]*\\)/", "", $title);
			$title = str_replace("  ", " ", $title);
			$title = trim($title);
			
			$title = checkSingleByteSpecial($title);
			
			if (checkUnwanted($title)) continue;
			
			$name = str_replace("/", "_", $title);
			$pgminfofile = $pgminfodir . "/" . $name . ".orig.jpg";
			
			if (file_exists($pgminfofile))
			{
				echo "Exists => $pgminfofile\n";
				continue;
			}
			
			if (($year = identifyMovie($title)) != false) $title = "@movie " . $title;

			if (! isset($cd_arr[ $title ])) $cd_arr[ $title ] = 0;
			$cd_arr[ $title ] += 1;
			
			if (! isset($GLOBALS[ "ttoc" ])) $GLOBALS[ "ttoc" ] = array();
			$GLOBALS[ "ttoc" ][ $title ] = $channel;
		}
	}

	closedir($cd_dfd);	

	return $cd_arr;	
}

function integrateInfos(&$dest, $source)
{
	foreach ($source as $title => $count)
	{
		if (! isset($dest[ $title ]))
		{
			$dest[ $title ] = $count;
		}
		else
		{
			$dest[ $title ] += $count;
		}
	}
}

function writeInfos($filename, $infos)
{
	$array = array();
	
	foreach ($infos as $title => $count)
	{
		$item = array();
		$item[ "t" ] = $title;
		$item[ "c" ] = $count;
		$item[ "s" ] = $GLOBALS[ "ttoc" ][ $title ];
		
		$array[] = $item;
	}
	
	$json = json_encdat($array);
	file_put_contents($filename, $json);
}

function collectInfos()
{
	$channelsdir  = "../../var/epgdata";
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

			@mkdir(str_replace("epgdata", "epginfo", $countrydir), 0755, true);
			
			$co_dfd = opendir($countrydir);
			$co_arr = array();

			while (($channel = readdir($co_dfd)) !== false)
			{
				if (($channel == ".") || ($channel == "..")) continue;
								
				$channeldir = "$countrydir/$channel";
				echo "$channeldir\n";

				$se_arr = readPrograms($countrydir, $channeldir);
				
				integrateInfos($co_arr, $se_arr);
				integrateInfos($ty_arr, $se_arr);
				integrateInfos($ch_arr, $se_arr);

				uasort($se_arr, "sortInfos");
				$se_file = str_replace("epgdata", "epginfo", "$channeldir.json");
				writeInfos($se_file, $se_arr);
			}
	
			closedir($co_dfd);		
		
			uasort($co_arr, "sortInfos");
			$co_file = str_replace("epgdata", "epginfo", "$countrydir.json");
			writeInfos($co_file, $co_arr);
		}
		
		closedir($ty_dfd);	
			
		uasort($ty_arr, "sortInfos");
		$ty_file = str_replace("epgdata", "epginfo", "$typesdir.json");
		writeInfos($ty_file, $ty_arr);
	}
	
	closedir($ch_dfd);
				
	uasort($ch_arr, "sortInfos");
	$ch_file = str_replace("epgdata", "epginfo", "$channelsdir.json");
	writeInfos($ch_file, $ch_arr);
}

collectInfos();

?>
