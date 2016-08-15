<?php

include("../include/json.php");

$GLOBALS[ "bootstrapurl"    ] = "http://zdfmediathk.sourceforge.net/akt.xml";
$GLOBALS[ "configfile"      ] = "./mtkprepare.de.json";
$GLOBALS[ "mtkdirectory"    ] = "../../var/mtkdata/tv/de";
$GLOBALS[ "minimumduration" ] = "001000";
 
//
//  0 => Sender
//  1 => Thema 
//  2 => Titel 
//  3 => Datum 
//  4 => Zeit 
//  5 => Dauer 
//  6 => Größe [MB] 
//  7 => Beschreibung 
//  8 => Url 
//  9 => Website 
// 10 => Url Untertitel 
// 11 => Url RTMP 
// 12 => Url Klein 
// 13 => Url RTMP Klein 
// 14 => Url HD 
// 15 => Url RTMP HD 
// 16 => DatumL 
// 17 => Url History 
// 18 => Geo 
// 19 => neu
//

function readConfig()
{
	$GLOBALS[ "config" ] = json_decdat(file_get_contents($GLOBALS[ "configfile" ]));
	
	if (! $GLOBALS[ "config" ])
	{
		error_log("Cannot read config: " . $GLOBALS[ "configfile" ]);
		exit(1);
	}
}

function prepareDate(&$entry)
{
	$date = "";
	
	if ($entry[ 3 ] == "")
	{
		$date = $date . "00000000";
	}
	else
	{
		$date = $date
			  . substr($entry[ 3 ], 6, 4) 
			  . substr($entry[ 3 ], 3, 2)
			  . substr($entry[ 3 ], 0, 2)
			  ;
	}

	if ($entry[ 4 ] == "")
	{
		$date = $date . "0000";
	}
	else
	{
		$date = $date
		      . substr($entry[ 4 ], 0, 2)
		      . substr($entry[ 4 ], 3, 2)
		      ;
	}
	
	return $date;
}

function prepareDuration(&$entry)
{
	if ($entry[ 5 ] == "") return "000000";
	return str_replace(":", "", $entry[ 5 ]);
}

function checkSpecialVersion(&$entry)
{
	$version = "N";
	
	$title = $entry[ 2 ];
	
	if (strpos($title, "Hörfassung: ") !== false)
	{
		$title = trim(str_replace("Hörfassung: ", " ", $title));
		$version = "H";
	}
	
	if (strpos($title, "- Hörfassung") !== false)
	{
		$title = trim(str_replace("- Hörfassung", " ", $title));
		$version = "H";
	}
	
	if (strpos($title, "(Hörfassung)") !== false)
	{
		$title = trim(str_replace("(Hörfassung)", " ", $title));
		$version = "H";
	}
	
	if (strpos($title, "(Hörfassung)") !== false)
	{
		$title = trim(str_replace("(Hörfassung)", " ", $title));
		$version = "H";
	}
	
	if (strpos($title, "(mit Audiodeskription)") !== false)
	{
		$title = trim(str_replace("(mit Audiodeskription)", " ", $title));
		$version = "H";
	}

	if (strpos($title, "(mit Audiodeskription, ") !== false)
	{
		$title = trim(str_replace("(mit Audiodeskription, ", "(", $title));
		$version = "H";
	}

	if (strpos($title, " mit Gebärdensprache") !== false)
	{
		$title = trim(str_replace(" mit Gebärdensprache", " ", $title));
		$version = "G";
	}
	
	if (strpos($title, " Gebärden") !== false)
	{
		$title = trim(str_replace(" Gebärden", " ", $title));
		$version = "G";
	}
	
	if (strpos($title, " - AD") !== false)
	{
		$title = trim(str_replace(" - AD", " ", $title));
		$version = "H";
	}
	
	if (strpos($title, " (AD)") !== false)
	{
		$title = trim(str_replace(" (AD)", " ", $title));
		$version = "H";
	}
	
	$title = str_replace("  ", " ", $title);
	$title = str_replace("  ", " ", $title);

	$entry[ 2 ] = $title;

	return $version;
}

function prepareTitle($show, &$entry)
{
	$shownames = array();
	$shownames[] = $show;
	
	if (isset($GLOBALS[ "config" ][ "shows" ][ $show ][ "synonyms" ]))
	{
		foreach($GLOBALS[ "config" ][ "shows" ][ $show ][ "synonyms" ] as $index => $altname)
		{
			$shownames[] = $altname;
		}
	}
	
	$title = prepareString($entry, 2);
	
	foreach ($shownames as $index => $showname)
	{
		$showlen = strlen($showname);
	
		if ((substr($title, 0, 1) == "\"") && (substr($title, -1) == "\""))
		{
			$title = trim(substr($title, 1, -1));
		}
	
		if (substr($title, 0, $showlen + 2) == "\"$showname\"")
		{
			$title = trim(str_replace("\"$showname\"", "$showname", $title));
		}
	
		if (substr($title, 0, $showlen + 3) == "$showname - ")
		{
			$title = trim(substr($title, $showlen + 3));
		}

		if (substr($title, 0, $showlen + 2) == "$showname: ")
		{
			$title = trim(substr($title, $showlen + 2));
		}
	
		if (substr($title, 0, $showlen + 4) == "$showname vom")
		{
			$title = "";
		}
	
		if (substr($title, 0, $showlen + 3) == "$showname am")
		{
			$title = "";
		}
		
		if (substr($title, 0, $showlen + 1) == "$showname ")
		{
			$title = trim(substr($title, $showlen + 1));
		}

		if ($title == $showname)
		{
			$title = "";
		}
	
		if ((substr($title, 0, 1) == "\"") && (substr($title, -1) == "\""))
		{
			$title = trim(substr($title, 1, -1));
		}
	}
	
	return $title;
}

function prepareString(&$entry, $inx)
{
	$item = $entry[ $inx ];
	
	$item = str_replace(" ...", "...", $item);
	
	$item = str_replace("“", "\"", $item);
	$item = str_replace("„", "\"", $item);
	
	$item = str_replace("|", "/", $item);
	$item = str_replace("–", "-", $item);
	
	$item = str_replace("\r", " ", $item);
	$item = str_replace("\n", " ", $item);
	
	$item = str_replace("   ", " ", $item);
	$item = str_replace("  ", " ", $item);
	
	return $item;
}

function writeEntries($outputfd, $show, &$entrylines)
{
	fwrite($outputfd, "  \"$show\":\n");
	fwrite($outputfd, "  [\n");

	rsort($entrylines, SORT_STRING);
	
	$lastdate = "";
	
	for ($inx = 0; $inx < count($entrylines); $inx++)
	{
		$line = $entrylines[ $inx ];
		
		$parts = explode("|", $line);
		
		if ($parts[ 1 ] < $GLOBALS[ "minimumduration" ]) continue;
		
		if ($lastdate != $parts[ 0 ])
		{
			$lastdate = $parts[ 0 ];
		}
		else
		{
			if ($parts[ 3 ] == "N")
			{
				$line = str_replace("|*|", "|-|", $line);
			}
		}
		
		fwrite($outputfd, "    \"" . $line . "\"");
		
		if (($inx + 1) < count($entrylines)) fwrite($outputfd, ",");
		
		fwrite($outputfd, "\n");
	}
	
	fwrite($outputfd, "  ]\n");
}

function activateIfModified($outputfile)
{
	$finalfile = str_replace(".tmp.", ".", $outputfile);
	
	$finalmd5 = file_exists($finalfile) ? md5_file($finalfile) : "";
	$outputmd5 = md5_file($outputfile);
	
	error_log("MD5=$finalmd5:$finalfile");
	error_log("MD5=$outputmd5:$outputfile");
	
	if ($finalmd5 == $outputmd5)
	{
		unlink($outputfile);
	}
	else
	{
		error_log("GZIP=$outputfile");
		
		system("gzip < \"$outputfile\" > \"$outputfile.gz\"");
		
		rename($outputfile, $finalfile);
		rename($outputfile . ".gz", $finalfile . ".gz");
	}
}

function prepareList()
{
	$filexy = $GLOBALS[ "mtkdirectory" ] . "/rawdata.xy";

	$pfd = popen("xzcat < $filexy", "r");
	
	$lines = 0;
	$channel = "";
	$show = "";
	$showlc = "";
	
	$ccount = 0;
	$scount = 0;
	$ecount = 0;
	$tcount = 0;

	while (($line = fgets($pfd)) !== false)
	{
		$line = trim($line);
		
		if (substr($line, 0, 7) != "\"X\" : [") continue;
		
		if (substr($line, -1) == ",")
		{
			$entry = json_decdat(substr($line, 6, -1));
		}
		else
		{
			$entry = json_decdat(substr($line, 6));
		}
		
		//
		// Strip ORF junk.
		//
			
		if (substr($entry[ 1 ], 0, 5) == "AD | ")
		{
			$entry[ 1 ] = substr($entry[ 1 ], 5);
		}
		
		if (substr($entry[ 2 ], 0, 5) == "AD | ")
		{
			$entry[ 2 ] = substr($entry[ 2 ], 5);
		}
		
		$temp = $entry[ 1 ];
		$temp = str_replace("   ", " ", $temp);
		$temp = str_replace("  ", " ", $temp);
		$temp = str_replace("–", "-", $temp);
		$temp = trim($temp);
		
		if (substr($temp, -10) == "Livestream") continue;
		
		$templc = mb_convert_case($temp, MB_CASE_LOWER);
		$templc = str_replace("!", " ", $templc);
		$templc = str_replace(".", " ", $templc);
		$templc = str_replace("-", " ", $templc);
		$templc = str_replace("   ", " ", $templc);
		$templc = str_replace("  ", " ", $templc);
		$templc = trim($templc);

		if (($entry[ 0 ] != "") && ($entry[ 0 ] != $channel))
		{
			if ($show != "") 
			{
				writeEntries($outputfd, $show, $entrylines);
			}
			
			if ($channel != "") 
			{
				error_log("$channel => $scount => $tcount");
				
				fwrite($outputfd, "}\n");
				fclose($outputfd);
				
				activateIfModified($outputfile);
			}
			
			$channel = $entry[ 0 ];
			$show = "";
			
			$ccount = 0;
			$scount = 0;
			$ecount = 0;
			$tcount = 0;
			
			$outputfile = $GLOBALS[ "mtkdirectory" ] . "/$channel.tmp.json";
			
			$outputfd = fopen($outputfile, "w");
			fwrite($outputfd, "{\n");
		}
		
		if (($templc != "") && ($templc != $showlc))
		{
			if ($show != "") 
			{
				error_log("$channel $show => $ecount");
		
				writeEntries($outputfd, $show, $entrylines);
			}
			
			$show = $temp;
			$showlc = $templc;
					
			$scount++;
			$ecount = 0;
			$entrylines = array();
		}

		$entryline = prepareDate($entry) . "|" 
				   . prepareDuration($entry) . "|" 
				   . checkSpecialVersion($entry) . "|"
				   . "*|" 
				   . prepareTitle($show, $entry) . "|" 
				   . prepareString($entry, 7) .  "|"
				   . prepareString($entry, 8) .  "|"
				   . prepareString($entry, 9)
				   ;
				   
		$entryline = str_replace("\\", "/", $entryline);  
		$entryline = str_replace("\"", "\\\"", $entryline);
		
		$entrylines[] = $entryline;
		
		$ecount++;
		$ccount++;
		$tcount++;
		$lines++;
	}
	
	if ($show != "") 
	{
		error_log("$channel $show => $ecount");

		writeEntries($outputfd, $show, $entrylines);
	}
	
	if ($channel != "") 
	{
		error_log("$channel => $scount => $tcount");
		
		fwrite($outputfd, "}\n");
		fclose($outputfd);
		
		activateIfModified($outputfile);
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

readConfig();
//getLatestList();
prepareList();

?>