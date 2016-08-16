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

function checkSpecialVersion($channel, $show, &$entry)
{
	$version = "N";
	
	if (isset($GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "special" ]))
	{
		$version = $GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "special" ];
	}
	
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
	
	if (strpos($title, "die Sendung in Gebärdensprache") !== false)
	{
		$title = trim(str_replace("die Sendung in Gebärdensprache", " ", $title));
		$version = "G";
	}

	if (strpos($title, "mit Gebärdenübersetzung") !== false)
	{
		$title = trim(str_replace("mit Gebärdenübersetzung", " ", $title));
		$version = "G";
	}

	if (strpos($title, "(Mit Gebärdensprache)") !== false)
	{
		$title = trim(str_replace("(Mit Gebärdensprache)", " ", $title));
		$version = "G";
	}

	if (strpos($title, "(mit Gebärdensprache)") !== false)
	{
		$title = trim(str_replace("(mit Gebärdensprache)", " ", $title));
		$version = "G";
	}

	if (strpos($title, " - mit Audiodeskription)") !== false)
	{
		$title = trim(str_replace(" - mit Audiodeskription)", ")", $title));
		$version = "H";
	}

	if (strpos($title, ", mit Audiodeskription)") !== false)
	{
		$title = trim(str_replace(", mit Audiodeskription)", ")", $title));
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

	if (strpos($title, " - Gebärdensprache") !== false)
	{
		$title = trim(str_replace(" - Gebärdensprache", " ", $title));
		$version = "G";
	}
	
	if (strpos($title, " mit Gebärdensprache") !== false)
	{
		$title = trim(str_replace(" mit Gebärdensprache", " ", $title));
		$version = "G";
	}
	
	if (strpos($title, " Gebärdensprache") !== false)
	{
		$title = trim(str_replace(" Gebärdensprache", " ", $title));
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

function sortByStringLength($a, $b)
{
  	if (strlen($a) == strlen($b)) return 0;
  	return (strlen($a) > strlen($b) ? -1 : 1);
}

function findAtStart($haystack, $needle, $replace = null)
{
	if (substr($haystack, 0, strlen($needle)) == $needle)
	{
		if ($replace)
		{
			$haystack = trim($replace . substr($haystack, strlen($needle)));
		}
		else
		{
			$haystack = trim(substr($haystack, strlen($needle)));
		}
	}
	
	$haystack = str_replace("   ", " ", $haystack);
	$haystack = str_replace("  ", " ", $haystack);
	
	return $haystack;
}

function prepareInfos($channel, $show, &$entry)
{
	$infos = prepareString($entry, 7);
	
	//
	// Strip bogues lead ins.
	//
	
	$infos = findAtStart($infos, "Die Themen der Sendung:");
	$infos = findAtStart($infos, "Themen der Sendung:");
	$infos = findAtStart($infos, "Mit folgenden Themen:");
	$infos = findAtStart($infos, "Mit den Themen:");
	$infos = findAtStart($infos, "Unsere Themen:");
	$infos = findAtStart($infos, "Die Themen:");
	$infos = findAtStart($infos, "Mit diesen Themen:");
	$infos = findAtStart($infos, "Mit denThemen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Themen:");
	$infos = findAtStart($infos, "Zu Gast sind", "Gäste:");

	//
	// Convert first char to upper case.
	//
	
	$infos = mb_convert_case(mb_substr($infos, 0, 1), MB_CASE_UPPER) . mb_substr($infos, 1);
		
	$infos = str_replace("   ", " ", $infos);
	$infos = str_replace("  ", " ", $infos);

	return $infos;

}

function prepareTitle($channel, $show, &$entry)
{
	$shownames = array();
	$shownames[] = $show;
	
	if (isset($GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "synonyms" ]))
	{
		$synonyms = $GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "synonyms" ];
		
		foreach($synonyms as $index => $altname)
		{
			$shownames[] = $altname;
		}
		
		usort($shownames, "sortByStringLength");
	}
	
	$title = prepareString($entry, 2);
	
	//
	// Replace single quotes.
	//
	
	$title = trim(preg_replace("/^'/", "\"", $title));
	$title = trim(preg_replace("/'$/", "\"", $title));
	$title = trim(preg_replace("/ '/", " \"", $title));
	$title = trim(preg_replace("/' /", "\" ", $title));

	//
	// Remove fully quoted titles quotes.
	//
	
	if ((substr($title, 0, 1) == "\"") && (substr($title, -1) == "\""))
	{
		$title = trim(substr($title, 1, -1));
	}
	
	//
	// Remove repeatations of show name.
	//
	
	foreach ($shownames as $index => $showname)
	{
		$title = trim(preg_replace("/\"$showname\"/ui", $showname, $title));
		
		$title = trim(preg_replace("/" . $showname . "[ ]*[\\?\\-\\:]+/ui", "", $title));
		$title = trim(preg_replace("/" . $showname . " (vom|am)/ui", "", $title));
		$title = trim(preg_replace("/" . $showname . "[ ]+/ui", "", $title));
		$title = trim(preg_replace("/^" . $showname . "$/ui", "", $title));
		$title = trim(preg_replace("/^" . $showname . "\\(/ui", "(", $title));
	}
	
	//
	// Remove restrictions.
	//
	
	$title = trim(preg_replace("/\\(Video tgl. ab 20 Uhr\\)/ui", "", $title));
	$title = trim(preg_replace("/\\(Video tgl. ab 22 Uhr\\)/ui", "", $title));
	$title = trim(preg_replace("/\\(FSK 12, ab 20 Uhr abrufbar\\)/ui", "", $title));
	$title = trim(preg_replace("/\\(ab 20 Uhr abrufbar\\)/ui", "", $title));

	//
	// Remove fully quoted titles quotes once more.
	//
	
	if ((substr($title, 0, 1) == "\"") && (substr($title, -1) == "\""))
	{
		$title = trim(substr($title, 1, -1));
	}
	
	//
	// Remove bogus date spezifications.
	//
		
	$title = trim(preg_replace("/^[0-9]+\\.[0-9]+\\.$/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.[0-9]+\\.[0-9]+$/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. [\\p{L}]+ [0-9]+$/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.[\\p{L}]+ [0-9]+$/ui", "", $title));
	$title = trim(preg_replace("/\\([0-9]+\\.[0-9]+\\.[0-9]+\\)/ui", "", $title));
	
	$title = trim(preg_replace("/^[0-9]+\\.[0-9]+\\.[0-9]+ [0-9]+\\:[0-9]+$/ui", "", $title));
	
	$title = trim(preg_replace("/Die ganze Sendung/ui", "Sendung", $title));
	$title = trim(preg_replace("/Die ganz Sendung/ui", "Sendung", $title));
	$title = trim(preg_replace("/Die Sendung/ui", "Sendung", $title));
	$title = trim(preg_replace("/Sendung (vom|von|am)/ui", "Sendung", $title));
		
	$title = trim(preg_replace("/(Sendung|vom|von|am) [\\p{L}]+, *[0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));

	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\.[0-9]+\\.[0-9]+ -/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\.[0-9]+\\.[0-9]+$/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\.[0-9]+\\.[0-9]+:/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\.[0-9]+\\.[0-9]+ /ui", "", $title));
	
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+ [0-9]+ -/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+ [0-9]+$/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+ [0-9]+:/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+ [0-9]+ /ui", "", $title));
	
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+ -/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+$/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+:/ui", "", $title));
	$title = trim(preg_replace("/(Sendung|vom|von|am) *[0-9]+\\. [\\p{L}]+ /ui", "", $title));

	$title = trim(preg_replace("/^[0-9]+(\\.|\\,) *[0-9]+(\\.|\\,) *[0-9][0-9][0-9][0-9] -/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+(\\.|\\,) *[0-9]+(\\.|\\,) *[0-9][0-9][0-9][0-9]$/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+(\\.|\\,) *[0-9]+(\\.|\\,) *[0-9][0-9][0-9][0-9]:/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+(\\.|\\,) *[0-9]+(\\.|\\,) *[0-9][0-9][0-9][0-9] /ui", "", $title));
	
	$title = trim(preg_replace("/ [0-9][0-9](\\.|\\,)[0-9][0-9](\\.|\\,)[0-9][0-9][0-9][0-9]$/ui", "", $title));
	$title = trim(preg_replace("/ [0-9][0-9](\\.|\\,) [\\p{L}]+ [0-9][0-9][0-9][0-9]$/ui", "", $title));
	
	$title = trim(preg_replace("/^Montag,* [0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));
	$title = trim(preg_replace("/^Dienstag,* [0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));
	$title = trim(preg_replace("/^Mittwoch,* [0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));
	$title = trim(preg_replace("/^Donnerstag,* [0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));
	$title = trim(preg_replace("/^Freitag,* [0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));
	$title = trim(preg_replace("/^Samstag,* [0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));
	$title = trim(preg_replace("/^Sonntag,* [0-9]+\\.[0-9]+\\.[0-9]+/ui", "", $title));
		
	$title = trim(preg_replace("/^[0-9]+\\.* *Januar [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *Februar [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *März [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *April [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *Mai [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *Juni [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *Juli [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *August [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *Ausgust [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *September [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *Oktober [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *November [0-9][0-9][0-9][0-9]:*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\.* *Dezember [0-9][0-9][0-9][0-9]:*/ui", "", $title));

	$title = trim(preg_replace("/^[0-9]+\\. *Januar(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *Februar(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *März(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *April(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *Mai(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *Juni(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *Juli(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *August(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *Ausgust(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *September(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *Oktober(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *November(:| -)*/ui", "", $title));
	$title = trim(preg_replace("/^[0-9]+\\. *Dezember(:| -)*/ui", "", $title));

	//
	// Change episode stuff.
	//
	
	$title = trim(preg_replace("/Folge ([0-9]+) -/ui", "($1) ", $title));
	$title = trim(preg_replace("/Folge ([0-9]+)-/ui", "($1) ", $title));
	$title = trim(preg_replace("/Folge ([0-9]+):/ui", "($1) ", $title));
	$title = trim(preg_replace("/Folge ([0-9]+) /ui", "($1) ", $title));
	$title = trim(preg_replace("/Folge ([0-9]+)$/ui", "($1) ", $title));
	$title = trim(preg_replace("/\\(Folge ([0-9]+)\\)$/ui", "($1) ", $title));
	
	$title = trim(preg_replace("/\\(([0-9]+)\\/[0-9]+\\)/ui", "($1) ", $title));
	$title = trim(preg_replace("/\\(([0-9]+)\\) - /ui", "($1) ", $title));
	
	$title = trim(preg_replace("/^(\\([0-9]+\\)) (.*?)$/ui", "$2 $1", $title));

	//
	// Remove copyright stuff.
	//
	
	$title = trim(preg_replace("/© [ a-zA-Z0-9\\.\\,\\-\\/\\(\\)\\_\\&\\p{L}]+$/u", "", $title));
	
	//
	// Remove small junk at start.
	//
	
	$title = trim(preg_replace("/^- /u", "", $title));
	$title = trim(preg_replace("/^\\/ /u", "", $title));
	
	//
	// Remove redundant entries.
	//
	
	$title = trim(preg_replace("/^Sendung [0-9]+:[0-9]+$/ui", "", $title));
	$title = trim(preg_replace("/^Die Nachrichten in voller Länge$/ui", "", $title));
	$title = trim(preg_replace("/^Die komplette Sendung$/ui", "", $title));
	$title = trim(preg_replace("/^Die ganze Sendung$/ui", "", $title));
	$title = trim(preg_replace("/ - ganze sendung$/ui", "", $title));
	$title = trim(preg_replace("/in voller Länge$/ui", "", $title));
	$title = trim(preg_replace("/^Die ganze Folge$/ui", "", $title));
	$title = trim(preg_replace("/^ganze sendung$/ui", "", $title));
	$title = trim(preg_replace("/^sendung$/ui", "", $title));
	$title = trim(preg_replace("/^die sendung$/ui", "", $title));
	$title = trim(preg_replace("/^Mediathek$/ui", "", $title));
	$title = trim(preg_replace("/^Alle Beiträge:/ui", "", $title));
	$title = trim(preg_replace("/^TV$/ui", "", $title));
	
	//
	// Remove fully quoted titles quotes once again.
	//
	
	if ((substr($title, 0, 1) == "\"") && (substr($title, -1) == "\""))
	{
		$title = trim(substr($title, 1, -1));
	}
	
	//
	// Convert first char to upper case.
	//
	
	$title = mb_convert_case(mb_substr($title, 0, 1), MB_CASE_UPPER) . mb_substr($title, 1);
		
	$title = str_replace("   ", " ", $title);
	$title = str_replace("  ", " ", $title);

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
	
	return trim($item);
}

function writeEntries($outputfd, $channel, $show, &$entrylines)
{
	if (isset($GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "skip" ]))
	{
		if ($GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "skip" ])
		{
			//
			// Skip show.
			//
			
			return;
		}
	}
	
	$minlength = $GLOBALS[ "minimumduration" ];
	
	if (isset($GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "minlength" ]))
	{
		//
		// Override minimum length.
		//

		$minlength = $GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "minlength" ];
	}
	
	$cleantags = null;
	
	if (isset($GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "clean" ]))
	{
		//
		// Match array with entries to clean.
		//

		$cleantags = $GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $show ][ "clean" ];
	}

	$skipvideourls = null;
	
	if (isset($GLOBALS[ "config" ][ "channels" ][ $channel ][ "skipvideourls" ]))
	{
		//
		// Match array with entries to clean.
		//

		$skipvideourls = $GLOBALS[ "config" ][ "channels" ][ $channel ][ "skipvideourls" ];
	}

	//
	// Preflight entries.
	//
	
	$numentries = 0;
	
	for ($inx = 0; $inx < count($entrylines); $inx++)
	{
		$line = $entrylines[ $inx ];
		
		$parts = explode("|", $line);
		
		if ($parts[ 1 ] < $minlength) continue;
		
		$skip = false;
		
		if ($cleantags)
		{
			foreach ($cleantags as $index => $clean)
			{
				if (strpos($parts[ 4 ], $clean) !== false)
				{
					$skip = true;
					break;
				}
			} 
		}
		
		if ($skipvideourls)
		{
			foreach ($skipvideourls as $index => $skipurl)
			{
				if (strpos($parts[ 6 ], $skipurl) !== false)
				{
					$skip = true;
					break;
				}
			} 
		}
		
		if ($skip) continue;
		
		$numentries++;
	}
	
	if ($numentries == 0)
	{
		//
		// Skip empty shows.
		//
		
		return;
	}

	//
	// Dump show.
	//
	
	fwrite($outputfd, "  \"$show\":\n");
	fwrite($outputfd, "  [\n");

	rsort($entrylines, SORT_STRING);
	
	$lastdate = "";
	$lasttype = "";
	
	for ($inx = 0; $inx < count($entrylines); $inx++)
	{
		$line = $entrylines[ $inx ];
		
		$parts = explode("|", $line);
		
		if ($parts[ 1 ] < $minlength) continue;
		
		$skip = false;
		
		if ($cleantags)
		{
			foreach ($cleantags as $index => $clean)
			{
				if (strpos($parts[ 4 ], $clean) !== false)
				{
					$skip = true;
					break;
				}
			} 
		}
		
		if ($skipvideourls)
		{
			foreach ($skipvideourls as $index => $skipurl)
			{
				if (strpos($parts[ 6 ], $skipurl) !== false)
				{
					$skip = true;
					break;
				}
			} 
		}
		
		if ($skip) continue;

		if ($lastdate != $parts[ 0 ])
		{
			$lastdate = $parts[ 0 ];
			$lasttype = $parts[ 2 ];
		}
		else
		{
			if ($parts[ 2 ] == $lasttype)
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
	$filexy = $GLOBALS[ "mtkdirectory" ] . "/rawdata.xz";

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
		
		//
		// Strip off global character junk.
		//
		
		$line = str_replace("  ", " - ", $line);
		$line = str_replace("…", "...", $line);
		$line = str_replace(" ", " ", $line);

		//
		// Decode line into parts.
		//
		
		if (substr($line, -1) == ",")
		{
			$entry = json_decdat(substr($line, 6, -1));
		}
		else
		{
			$entry = json_decdat(substr($line, 6));
		}
		
		//
		// Strip off ORF junk.
		//
			
		if (substr($entry[ 1 ], 0, 5) == "AD | ")
		{
			$entry[ 1 ] = substr($entry[ 1 ], 5);
		}
		
		if (substr($entry[ 2 ], 0, 5) == "AD | ")
		{
			$entry[ 2 ] = substr($entry[ 2 ], 5);
		}
		
		if (($entry[ 0 ] != "") && ($entry[ 0 ] != $channel))
		{
			if ($show != "") 
			{
				writeEntries($outputfd, $channel, $show, $entrylines);
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
		
		//
		// Rename shows upfront.
		//
		
		if (isset($GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $entry[ 1 ] ][ "rename" ]))
		{
			//
			// Rename show.
			//

			echo "Rename===>>>>> " . $entry[ 1 ] . "\n";
			
			$entry[ 1 ] = $GLOBALS[ "config" ][ "channels" ][ $channel ][ "shows" ][ $entry[ 1 ] ][ "rename" ];
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
		
		if (($templc != "") && ($templc != $showlc))
		{
			if ($show != "") 
			{
				error_log("$channel $show => $ecount");
		
				writeEntries($outputfd, $channel, $show, $entrylines);
			}
			
			$show = $temp;
			$showlc = $templc;
					
			$scount++;
			$ecount = 0;
			$entrylines = array();
		}

		$entryline = prepareDate($entry) . "|" 
				   . prepareDuration($entry) . "|" 
				   . checkSpecialVersion($channel, $show, $entry) . "|"
				   . "*|" 
				   . prepareTitle($channel, $show, $entry) . "|" 
				   . prepareInfos($channel, $show, $entry) .  "|"
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

		writeEntries($outputfd, $channel, $show, $entrylines);
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
		$filexy = $GLOBALS[ "mtkdirectory" ] . "/rawdata.xz";
		
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