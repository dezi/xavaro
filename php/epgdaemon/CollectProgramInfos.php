<?php

set_time_limit(0);

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

function deMoronizeEPG($string)
{
	$string = str_replace(chr(0xc4) . chr(0xa6), "ä", $string);
	$string = str_replace(chr(0xc4) . chr(0xb3), "ö", $string);
	$string = str_replace(chr(0xc3) . chr(0xbe), "ü", $string);
	$string = str_replace(chr(0xe2) . chr(0x85) . chr(0x99), "Ä", $string);
	$string = str_replace(chr(0xe2) . chr(0x85) . chr(0x9a), "Ö", $string);
	$string = str_replace(chr(0xe2) . chr(0x85) . chr(0x9b), "Ü", $string);
	$string = str_replace(chr(0xe2) . chr(0x85) . chr(0x9e), "ß", $string);

	return $string;
}

function startsWith($title, $start)
{
	return (substr($title, 0, strlen($start)) == $start);
}

function removeEnd($title, $start)
{
	return substr($title, 0, strlen($start));
}

function chopTitleName($title)
{
	if (startsWith($title, $start = "Tagesschau - Vor 20 Jahren")) return removeEnd($title, $start);
	if (startsWith($title, $start = "Sportclub live - 3. Liga")) return removeEnd($title, $start);
	
	return $title;
}

function readPrograms($countrydir, $channeldir)
{
	$channel = basename($channeldir);
	
	$pgminfodir = str_replace("epgdata", "pgminfo", $countrydir);
	
	$cd_dfd = opendir($channeldir);
	$cd_arr = array();
	
	if (substr($channel, -3) == " HD") $channel = substr($channel, 0, -3);
	if (substr($channel, -12) == " Deutschland") $channel = substr($channel, 0, -12);
	
	while (($epgfile = readdir($cd_dfd)) !== false)
	{
		if (($epgfile == ".") || ($epgfile == "..")) continue;

		if ((strlen($epgfile) != 18) && ($epgfile != "current.json.gz")) continue;
		if (substr($epgfile, -8) !=	".json.gz") continue;
			 
		$epgfile = "$channeldir/$epgfile";
		echo "$epgfile\n";
		
		$epgs = json_decdat(gzinflate(substr(file_get_contents($epgfile),10,-8)));
		if (! isset($epgs[ "epgdata" ])) continue;
		
		foreach ($epgs[ "epgdata" ] as $inx => $val)
		{
			//
			// Corrections.
			//
			
			unset($val[ "language" ]);
			
			if (isset($val[ "title" ]))
			{
				$val[ "title" ] = deMoronizeEPG($val[ "title" ]);
			}
			
			if (isset($val[ "subtitle" ]))
			{
				$val[ "subtitle" ] = deMoronizeEPG($val[ "subtitle" ]);
			}
			
			if (isset($val[ "description" ]))
			{
				$val[ "description" ] = deMoronizeEPG($val[ "description" ]);
			}

			$epgs[ "epgdata" ][ $inx ] = $val;
			
			//
			// Check if title image exists.
			//
			
			$title = $val[ "title" ];
			
			$realtitle = $title;
			$realtitle = preg_replace("/\\([^)]*\\)/", "", $realtitle);
			$realtitle = trim($realtitle);
			$realtitle = chopTitleName($realtitle);
			$realtitle = str_replace("  ", " ", $realtitle);
			$realtitle = str_replace("/", "_", $realtitle);
					
			$name = $realtitle;
			$pgminfofile = $pgminfodir . "/" . $name . ".orig.jpg";
			
			if (file_exists($pgminfofile))
			{
				//
				// Remove legacy property.
				//
				
				unset($epgs[ "epgdata" ][ $inx ][ "img" ]);
				
				//
				// Add image info.
				//
				
				$info = getimagesize($pgminfofile);
				$epgs[ "epgdata" ][ $inx ][ "imgsize" ] = $info[ 0 ] . "x" . $info[ 1 ];

				if ($realtitle != $title)
				{
					$epgs[ "epgdata" ][ $inx ][ "imgname" ] = $realtitle;
				}
				
				continue;
			}
			else
			{
				unset($epgs[ "epgdata" ][ $inx ][ "img" ]);
				unset($epgs[ "epgdata" ][ $inx ][ "imgsize" ]);
				unset($epgs[ "epgdata" ][ $inx ][ "imgname" ]);
			}
			
			//
			// Identify if known movie.
			//
			
			if (($year = identifyMovie($title)) != false) $title = "@movie " . $title;

			if (strpos($countrydir,"/tv/") > 0) checkWiki($epgs[ "epgdata" ][ $inx ]);
			
			//
			// Check subtitle for year.
			//
			
			/*
			if (isset($val[ "subtitle" ]))
			{
				//echo $val[ "subtitle" ] . "\n";
				
				if (preg_match("/^[^0-9]*[12][0-9][0-9][0-9]$/", $val[ "subtitle" ], $matches))
				{
					echo "=========================>>>" . $matches[ 0 ] . "\n"; 
				}
			}
			*/
			
			/*
			if (isset($val[ "description" ]))
			{
				//echo $val[ "subtitle" ] . "\n";
				
				if (preg_match("/^([^0-9(]{1,48}[12][0-9][0-9][0-9][A-ZÄÖÜ].{10})/", $val[ "description" ], $matches))
				{
					echo "=========================>>>" . $matches[ 0 ] . "\n"; 
				}
			}
			*/
			
			if (! isset($cd_arr[ $realtitle ])) $cd_arr[ $realtitle ] = 0;
			$cd_arr[ $realtitle ] += 1;
			
			if (! isset($GLOBALS[ "ttoc" ])) $GLOBALS[ "ttoc" ] = array();
			$GLOBALS[ "ttoc" ][ $realtitle ] = $channel;
		}
		
		file_put_contents($epgfile, gzencode(json_encdat($epgs), 9));
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

function checkWiki(&$epg)
{
	$okiyear = null;		
	$badyear = null;		
	
	if (($badyear === null) && isset($epg[ "title" ]) && 
		preg_match("/^[^0-9(]{1,48}([12][0-9][0-9][0-9])/", $epg[ "title" ], $matches))
	{
		$badyear = $matches[ 1 ];
	}
	
	if (($okiyear === null) && isset($epg[ "subtitle" ]) && 
		preg_match("/^[^0-9(]{1,48}([12][0-9][0-9][0-9])/", $epg[ "subtitle" ], $matches))
	{
		$okiyear = $matches[ 1 ];
	}
	
	if (($okiyear === null) && isset($epg[ "description" ]) &&
		preg_match("/^[^0-9(]{1,48}([12][0-9][0-9][0-9])/", $epg[ "description" ], $matches))
	{
		$okiyear = $matches[ 1 ];
	}
	
	if ($okiyear === null) 
	{
		$okiyear = identifyMovie($epg[ "title" ]);
	}
	
	if (($okiyear == null) || ($badyear != null) ||
		($okiyear < 1900) || ($okiyear > 2020)) return;
	
	if (! isset($GLOBALS[ "wikidone" ])) readWiki();
	
	$title = $epg[ "title" ] . " (" . $okiyear . ")";
	
	if (! (isset($GLOBALS[ "wikidone" ][ $title ]) 
	    || isset($GLOBALS[ "wikifail" ][ $title ])))
	{
		$url = trim($epg[ "title" ]);
		$url = trim(str_replace(" ", "_", $url));
		$url = trim(str_replace("&", "%26", $url));
		$url = trim(str_replace(" II ", " 2 ", $url . " "));
		$url = trim(str_replace(" III ", " 3 ", $url . " "));
		$url = trim(str_replace(" IV ", " 4 ", $url . " "));
		$url = trim(str_replace(" V ", " 5 ", $url . " "));
		$url = trim(str_replace(" VI ", " 6 ", $url . " "));
		
		$wikiurl = "";
		
		if (@file_get_contents("http://de.wikipedia.org/wiki/" . $url))
		{
			$wikiurl = $url;
			
			$GLOBALS[ "wikidone" ][ $title ] = $wikiurl;

			writeWiki();
		}
		else
		{
			$GLOBALS[ "wikifail" ][ $title ] = false;
			
			writeWiki();
		}
		
		echo "================================> $title => $wikiurl\n";
		
		//sleep(1);
	}
}

function readWiki()
{
	$GLOBALS[ "wikidone" ] = array();
	$GLOBALS[ "wikifail" ] = array();

	if (file_exists("../include/wikifilm.done.json"))
	{
		$GLOBALS[ "wikidone" ] = json_decdat(file_get_contents("../include/wikifilm.done.json"));
	}
	
	if (file_exists("../include/wikifilm.done.json"))
	{
		$GLOBALS[ "wikifail" ] = json_decdat(file_get_contents("../include/wikifilm.fail.json"));
	}
}

function writeWiki()
{
	ksort($GLOBALS[ "wikidone" ]);
	ksort($GLOBALS[ "wikifail" ]);
	
	file_put_contents("../include/wikifilm.done.json", json_encdat($GLOBALS[ "wikidone" ]));
	file_put_contents("../include/wikifilm.fail.json", json_encdat($GLOBALS[ "wikifail" ]));
}

collectInfos();
echo "Done...\n";
?>
