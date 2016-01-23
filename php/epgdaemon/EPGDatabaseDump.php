<?php

include("../include/json.php");
include("../include/countries.php");
include("../include/languages.php");
include("../include/astra.php");

function errorexit($message)
{
	fwrite(STDERR, "$message\n");
	
	exit(1);
}

function readByte($fd)
{
	$data = @fread($fd,1);
	if ($data === false) return -1;
	
	$data = unpack("C",$data);
	return $data[ 1 ];
}

function readInt($fd)
{
	$len = 4;
	$val = 0;

	for ($inx = 0; $inx < $len; $inx++)
	{ 
		$data = fread($fd,1);
		if ($data === false) return -1;

		$val = ($val << 8) + ord($data[ 0 ]);
	}

	return $val;
}

function readNumber($fd, $len)
{
	$data = fread($fd,$len);

	$val = gmp_init(0);

	for ($inx = $len - 1; $inx >= 0; $inx--)
	{ 
		$val = gmp_mul($val, gmp_init(256));
		$val = gmp_add($val, gmp_init(ord($data[ $inx ])));
	}

	return $val;
}

function readString($fd,$len)
{
	if ($len == 0) return "";
	return fread($fd,$len);
}

function decodeLength($fd, $length, &$json, $level = 0)
{
	$pad = str_pad("", $level * 4, " ");

	$rest = 0;

	while (($rest + 5) < $length)
	{
		$type = readByte($fd);
		$nlen = readByte($fd);
		$dlen = readInt ($fd,4);

		if (($type < 0) || ($nlen < 0) || ($dlen < 0))
		{
			errorexit("Database update in progress, exitting...");
		}
		
		$name = readString($fd,$nlen);

		$rest += 6 + $nlen + $dlen;

		if (($type == 1) || ($type == 5))
		{
			$subj = array();
			decodeLength($fd, $dlen, $subj, $level + 1);

			if ($name == "")
			{
				$json[] = $subj;
			}
			else
			{
				$json[ $name ] = $subj;
			}

			continue;
		}

		if ($type == 2)
		{
			$number = readNumber($fd,$dlen);

			if ($name == "")
			{
				$json[] = $number;
			}
			else
			{
			    $json[ $name ] = $number;
			}

			continue;
		}

		if ($type == 3)
		{
			$data = readString($fd,$dlen);
			
			if ($name == "")
			{
				$json[] = $data;
			}
			else
			{
				$json[ $name ] = $data;
			}

			continue;
		}

		errorexit("Database update in progress, exitting...");
	}
}

function readHomedir()
{
	$env = posix_getpwuid(posix_getuid());

	$GLOBALS[ "homedir" ] = $env[ "dir" ];
}

function readHostname()
{
	$GLOBALS[ "hostname" ] = trim(file_get_contents("/etc/hostname"));
}

function readChannels()
{
	if (! isset($GLOBALS[ "channels" ])) $GLOBALS[ "channels" ] = array();

	$channeldir = "/home/pi/.hts/tvheadend/channel/config";

	echo "Scan: $channeldir\n";

	$dfd = opendir($channeldir);
	if ($dfd === false) errorexit("Cannot scan $channeldir");
	
	while (($entry = readdir($dfd)) !== false)
	{
		if ($entry == ".") continue;
		if ($entry == "..") continue;

		$jsondata = file_get_contents($channeldir . "/" . $entry);
		$json = json_decdat($jsondata);

		$GLOBALS[ "channels" ][ $entry ] = $json;
	}

	closedir($dfd);
}

function readTags()
{
	if (! isset($GLOBALS[ "tags" ])) $GLOBALS[ "tags" ] = array();

	$tagsdir = "/home/pi/.hts/tvheadend/channel/tag";

	echo "Scan: $tagsdir\n";

	$dfd = opendir($tagsdir);
	if ($dfd === false) errorexit("Cannot scan $tagsdir");
	
	while (($entry = readdir($dfd)) !== false)
	{
		if ($entry == ".") continue;
		if ($entry == "..") continue;

		$jsondata = file_get_contents($tagsdir . "/" . $entry);
		$json = json_decdat($jsondata);

		 $GLOBALS[ "tags" ][ $entry ] = $json;
	}

	closedir($dfd);
}

function readNetworks()
{
	if (! isset($GLOBALS[ "networks" ])) $GLOBALS[ "networks" ] = array();

	$networksdir = "/home/pi/.hts/tvheadend/input/dvb/networks";

	echo "Scan: $networksdir\n";
	
	$dfd = opendir($networksdir);
	if ($dfd === false) errorexit("Cannot scan $networksdir");

	while (($entry = readdir($dfd)) !== false)
	{
		if ($entry == ".") continue;
		if ($entry == "..") continue;

		$networksconfig = $networksdir . "/" . $entry . "/config";
		
		echo "Read: $networksconfig\n";
		
		$jsondata = file_get_contents($networksconfig);
		$json = json_decdat($jsondata);

		$GLOBALS[ "networks" ][ $entry ] = $json;
		
		readMuxes($networksdir . "/" . $entry . "/muxes", $json[ "networkname" ]);
	}

	closedir($dfd);
}

function readMuxes($muxesdir, $networkname)
{
	if (! isset($GLOBALS[ "muxes" ])) $GLOBALS[ "muxes" ] = array();

	//echo "Scan: $muxesdir\n";

	$dfd = opendir($muxesdir);
	if ($dfd === false) errorexit("Cannot scan $muxesdir");

	while (($entry = readdir($dfd)) !== false)
	{
		if ($entry == ".") continue;
		if ($entry == "..") continue;


		$jsondata = file_get_contents($muxesdir . "/" . $entry);
		$json = json_decdat($jsondata);

		$GLOBALS[ "muxes" ][ $entry ] = $json;
		
		readServices($muxesdir . "/" . $entry . "/services", $networkname);
	}

	closedir($dfd);
}

function readServices($servicesdir, $networkname)
{
	if (! isset($GLOBALS[ "services" ])) $GLOBALS[ "services" ] = array();

	if (! file_exists($servicesdir)) return;

	//echo "Scan: $servicesdir\n";
	
	$dfd = opendir($servicesdir);
	if ($dfd === false) errorexit("Cannot scan $servicesdir");

	while (($entry = readdir($dfd)) !== false)
	{
		if ($entry == ".") continue;
		if ($entry == "..") continue;

		$jsondata = file_get_contents($servicesdir . "/" . $entry);
		$json = json_decdat($jsondata);

		$json[ "networkname" ] = $networkname;

		$GLOBALS[ "services" ][ $entry ] = $json;
	}

	closedir($dfd);
}

function stripShit($name)
{
	while (preg_match("|(\([^\)]*\))|", $name, $treffer))
	{
		$name = str_replace($treffer[ 1 ], "", $name);
	}
	
	$name = trim($name);
	$name = str_replace("  ", " ", $name);
	$name = str_replace("  ", " ", $name);
	
	return $name;
}

function unifyChannelName($name, $language, $ishd)
{
	//
	// Put HD/SD markers at end.
	//
	
	if (strpos($name, " HD ") !== false) $name = str_replace(" HD ", "", $name) . " HD";
	if (strpos($name, " SD ") !== false) $name = str_replace(" SD ", "", $name) . " SD";
	
	if ($ishd && (substr($name, -2) != "HD"))
	{
		$name .= " HD";
	}
	
	//
	// Remove content in brackets.
	//
	
	while (preg_match("|(\([^\)]*\))|", $name, $treffer))
	{
		$name = str_replace($treffer[ 1 ], "", $name);
	}
	
	$name = str_replace("  ", " ", $name);
	$name = str_replace("  ", " ", $name);
	
	return $name;
}

function addChannelTag(&$config, $tagname, $tagvalue)
{
	if (! isset($config[ $tagname ])) $config[ $tagname ] = array();
	
	foreach ($config[ $tagname ] as $inx => $val)
	{
		if ($val == $tagvalue) return;
	}
	
	$config[ $tagname ][] = $tagvalue;
	
	asort($config[ $tagname ]);
}

function saveChannel($cdata, $adata)
{
	$name  = $cdata[ "name"  ];
	$type  = $cdata[ "type"  ];
	$isocc = $cdata[ "isocc" ];
	$ishd  = $cdata[ "ishd"  ];
	
	$actdir  = "../../var/channels/$type/$isocc"; 
	$actfile = $actdir . "/" . $name . ".json";
	$actlogo = $actdir . "/" . $name . ".png";

	if (! file_exists($actdir)) mkdir($actdir, 0755, true);
	
	if ((! file_exists($actlogo)) && isset($adata[ "logo" ]) && $adata[ "logo" ])
	{
		$im = ImageCreateFromPNG($adata[ "logo" ]);
		
		if ($im)
		{
			imagepng($im, $actlogo, 9);
		}
	}

	$config = array();
	
	if (file_exists($actfile))
	{
		$config = json_decdat(file_get_contents($actfile));
	}
	else
	{
		//
		// Preset fields some fields.
		//
		
		$config[ "name"      ] = $name;
		$config[ "type"      ] = $type;
		$config[ "isocc"     ] = $isocc;
	}
	
	$config[ "ishd"    ] = $ishd;
	$config[ "isolang" ] = $adata[ "isolang" ];
	
	if (isset($adata[ "packs" ])) $config[ "packs" ] = $adata[ "packs" ];

	if (isset($cdata[ "tags" ]))
	{
		$tags = explode("|", $cdata[ "tags" ]);
		
		foreach ($tags as $tag)
		{
			addChannelTag($config, "tags", $tag);
		}
	}
	
	if (isset($cdata[ "network" ]))
	{
		$network = $cdata[ "network" ];
		
		if ((substr($network, 0, 5) == "DVB-T") ||
			(substr($network, 0, 5) == "DVB-C") ||
			(substr($network, 0, 5) == "DVB-S") )
		{
			addChannelTag($config, "tags", substr($network, 0, 5));
		}		
	}
	
	if (isset($cdata[ "provider" ]))
	{
		addChannelTag($config, "providers", $cdata[ "provider" ]);
	}
	
	//
	// Cleanup shit.
	//
	
	if (isset($config[ "tags" ]))
	{
		for ($inx = 0; $inx < count($config[ "tags" ]); $inx++)
		{
			if ($config[ "tags" ][ $inx ] == "TV channels")
			{
				unset($config[ "tags" ][ $inx ]);
			}
		}	
	}
	
	//
	// Create new array for storing with defined
	// content ordering.
	//

	$ordered = array();
	
	$ordered[ "name"      ] = $config[ "name"      ];
	$ordered[ "type"      ] = $config[ "type"      ];
	$ordered[ "ishd"      ] = $config[ "ishd"      ];
	$ordered[ "isocc"     ] = $config[ "isocc"     ];
	$ordered[ "isolang"   ] = $config[ "isolang"   ];
	
	if (isset($config[ "tags"      ])) $ordered[ "tags"      ] = $config[ "tags"      ];
	if (isset($config[ "packs"     ])) $ordered[ "packs"     ] = $config[ "packs"     ];
	if (isset($config[ "providers" ])) $ordered[ "providers" ] = $config[ "providers" ];

	file_put_contents($actfile, json_encdat($ordered) . "\n");
}

function getMultibyteCharAt($str, $pos)
{
	$mb = $str[ $pos ];
	
	if (ord($mb) >= 128)
	{
		if ((($pos + 1) < strlen($str)) && (ord($str[ $pos + 1 ]) >= 128))
		{
			$mb .= $str[ $pos + 1 ];
		}
		
		if ((($pos + 2) < strlen($str)) && (ord($str[ $pos + 2 ]) >= 128))
		{
			$mb .= $str[ $pos + 2 ];
		}
		
		if ((($pos + 3) < strlen($str)) && (ord($str[ $pos + 3 ]) >= 128))
		{
			$mb .= $str[ $pos + 3 ];
		}
	}
	
	return $mb;
}

function getMultibyteCharBefore($str, $pos)
{
	if ($pos == 0) return "";
	
	$mb = $str[ $pos - 1 ];
	
	if (ord($mb) >= 128)
	{
		if ((($pos - 2) >= 0) && (ord($str[ $pos - 2 ]) >= 128))
		{
			$mb = $str[ $pos - 2 ] . $mb;
		}
		
		if ((($pos - 3) >= 0) && (ord($str[ $pos - 3 ]) >= 128))
		{
			$mb = $str[ $pos - 3 ] . $mb;
		}
		
		if ((($pos - 4) >= 0) && (ord($str[ $pos - 4 ]) >= 128))
		{
			$mb = $str[ $pos - 4 ] . $mb;
		}
	}
	
	return $mb;
}

function defuckEPG(&$epg)
{
	if (! isset($epg[ "description" ])) return;
	
	$desc = $epg[ "description" ];
	
	$encoding = mb_detect_encoding($desc, "UTF-8, ISO-8859-1, ISO-8859-15", true);
	
	if (($encoding != "UTF-8") && ($encoding != "ASCII"))
	{
  		echo "WRONG ENCODING: " . $epg[ "channel" ] . " => " . $encoding . "\n";
  		echo "WRONG ENCODING: " . $desc . "\n";
 		return;
	}
	
	if (strpos($desc, "\n") === false)
	{
		$dirty = false;
		
		for ($inx = 0; $inx < strlen($desc); $inx += strlen($mb))
		{
			$mb = getMultibyteCharAt($desc, $inx);
			
			if (mb_strtolower($mb, "UTF-8") != $mb)
			{
				//
				// Uppercase char.
				//
			
				if (($inx == 0)
					 || ($desc[ $inx - 1 ] == " ") 
					 || ($desc[ $inx - 1 ] == "-") 
					 || ($desc[ $inx - 1 ] == "'") 
					 || ($desc[ $inx - 1 ] == "(") 
					 || ($desc[ $inx - 1 ] == "/") 
					 || ($desc[ $inx - 1 ] == "\"") 
					 || ($desc[ $inx - 1 ] == "\n"))
				{
					//
					// Everything is ok.
					//
				
					continue;
				}
			
				//
				// If previous char is uppercase, everything is ok.
				//
				
				$prev = getMultibyteCharBefore($desc, $inx);
				
				if (mb_strtolower($prev, "UTF-8") != $prev)
				{
					//
					// Everything is ok.
					//
				
					continue;
				}
				
				$desc = substr($desc, 0, $inx) . " -- " . substr($desc, $inx);
				$dirty = true;
			}
		}
		
		if ($dirty) echo "FIXFIX " . $epg[ "channel" ] . " => " . $desc . "\n";
	}
}

function saveEPG($epg)
{
	if (! isset($epg[ "title" ])) 
	{
		//
		// Bogous entry w/o title discarded.
		//
		
		return;
	}
	
	if (($epg[ "title" ] == "Leider keine Programminformationen vorhanden") ||
		($epg[ "title" ] == "Leider keine Programminformationen verfügbar"))
	{
		//
		// Bogous entries from Kabel Deutschland spackos discarded.
		//
		
		return;
	}

	defuckEPG($epg);

	$channel  = $epg[ "channel"  ];
	$type     = $epg[ "type"     ];
	$language = $epg[ "language" ];
	$ishd     = isset($epg[ "is_hd" ]) ? $epg[ "is_hd" ] : false;
	
	$orgname = stripShit($channel);
	$channel = unifyChannelName($channel, $language, $ishd);
	$astra   = resolveAstra($orgname, $channel, $language);
	
	if ($astra == null)
	{
		//
		// Do not dump unconsolidated shit to disk.
		//
		
		return;
	}
	
	$channel = $astra[ "name"  ];
	$isocc   = $astra[ "isocc" ];
	
	$actdir  = "../../var/epgdata/$type/$isocc/$channel"; 
	if (! file_exists($actdir)) mkdir($actdir, 0755, true);
	
	$actfile = $actdir . "/0000.00.00." . $GLOBALS[ "hostname" ] . ".json"; 
	
	if ((! isset($GLOBALS[ "actfile" ])) || ($GLOBALS[ "actfile" ] != $actfile))
	{
		if (isset($GLOBALS[ "actfd" ]))
		{
			fclose($GLOBALS[ "actfd" ]);
			unset($GLOBALS[ "actfd" ]);
		}

		if (! file_exists($actdir)) mkdir($actdir, 0755, true);
		
		$GLOBALS[ "actfile" ] = $actfile;
		
		if (isset($GLOBALS[ "actchannels" ][ $channel ]))
		{
			//
			// Secondary write.
			//
			
			$GLOBALS[ "actfd" ] = fopen($actfile,"a+");
		}
		else
		{
			//
			// Primary write.
			//
			
			$GLOBALS[ "actfd" ] = fopen($actfile,"w");
		}
		
		$cdata = array();
		$cdata[ "name"     ] = $channel;
		$cdata[ "isocc"    ] = $isocc;
		$cdata[ "ishd"     ] = $ishd;
		$cdata[ "channel"  ] = $epg[ "channel" ];
		$cdata[ "type"     ] = $epg[ "type"    ];
		$cdata[ "provider" ] = isset($epg[ "provider" ]) ? $epg[ "provider" ] : "";
		$cdata[ "network"  ] = $epg[ "network" ];
		$cdata[ "country"  ] = $epg[ "country" ];
		$cdata[ "tags"     ] = $epg[ "tags"    ];
		$cdata[ "file"     ] = $actfile;
		$cdata[ "dir"      ] = $actdir;
		
		$GLOBALS[ "actchannels" ][ $channel ] = $cdata;
		
		saveChannel($cdata, $astra[ "astra" ]);
		
 		echo "Writing $actdir\n";
	}
	
	unset($epg[ "is_hd"         ]);
	unset($epg[ "is_widescreen" ]);
	unset($epg[ "is_subtitled"  ]);
	
	unset($epg[ "updated"  ]);
	unset($epg[ "channel"  ]);
	unset($epg[ "provider" ]);
	unset($epg[ "network"  ]);
	unset($epg[ "country"  ]);
	unset($epg[ "type"     ]);
	unset($epg[ "tags"     ]);

	if (isset($epg[ "summary" ]) && isset($epg[ "subtitle" ]) 
		   && $epg[ "summary" ] == $epg[ "subtitle" ])
	{
		unset($epg[ "summary" ]);
	}

	fwrite($GLOBALS[ "actfd" ],json_encdat($epg) . ",\n");
}

function readEPG()
{
	$epgdatabase = "~/.hts/tvheadend/epgdb.v2";

	if (substr($epgdatabase, 0, 2) == "~/")
	{	
		$env = posix_getpwuid(posix_getuid());

		$epgdatabase = $env[ "dir" ] . substr($epgdatabase,1);
	}

	$fd = fopen($epgdatabase,"r");
	if ($fd === false)
	{
		errorexit("Cannot open database <$epgdatabase>");
	}

	$count = 0;
	
	while (! feof($fd))
	{
		$length = readInt($fd, 4);
		if ($length < 0) break;

		//echo "$length\n";	

		$json = array();
		decodeLength($fd, $length, $json);

		if (isset($json[ "__section__" ]))
		{
			$section = $json[ "__section__" ];
			echo json_encdat($json) . "\n";	

			continue;
		}

		if ($section == "episodes")
		{
			if (! isset($GLOBALS[ "episodes" ])) $GLOBALS[ "episodes" ] = array();

			//
			//"uri":"tvh://channel-004f9c62d6081d0aa2b1e766b90568a8/bcast-10349/episode",
			//
			
			$uri = $json[ "uri" ];
			preg_match("|bcast-([0-9]*)|", $uri, $treffer);
			$id = $treffer[ 1 ];

			unset($json[ "uri" ]);

			$GLOBALS[ "episodes" ][ $id ] = $json;

			continue;
		}

		if ($section != "broadcasts") continue;

		$episode = $json[ "episode" ];
		preg_match("|bcast-([0-9]*)|", $episode, $treffer);
		$episode = $treffer[ 1 ];
		$episode = $GLOBALS[ "episodes" ][ $episode ];

		if (isset($episode[ "title"    ])) $json[ "title"    ] = $episode[ "title"    ];
		if (isset($episode[ "subtitle" ])) $json[ "subtitle" ] = $episode[ "subtitle" ];

		$channel = $json[ "channel" ];
		$channel = $GLOBALS[ "channels" ][ $channel ];

		$service = $channel[ "services" ];
		$service = $GLOBALS[ "services" ][ $service[ 0 ] ];

		$json[ "network"  ] = $service[ "networkname" ];
		$json[ "channel"  ] = $service[ "svcname"     ];

		if (isset($service[ "provider" ])) $json[ "provider" ] = $service[ "provider" ];

		$json[ "tags" ] = array();

		$type = "tv";

		$tags = $channel[ "tags" ];

		for ($inx = 0; $inx < count($tags); $inx++)
		{
			$tag = $GLOBALS[ "tags" ][ $tags[ $inx ] ][ "name" ];
			if ($tag == "Radio") $type = "rd";
			$json[ "tags" ][] = $tag;
		}

		$json[ "tags" ] = implode("|",$json[ "tags" ]);
		$json[ "type" ] = $type;

		$language = null;

		if (isset($json[ "title" ])) 
		{
			foreach ($json[ "title" ] as $lang => $text)
			{
				$language = $lang;
				$json[ "title" ] = $text;
				break;
			}
		}

		if (isset($json[ "subtitle" ])) 
		{
			foreach ($json[ "subtitle" ] as $lang => $text)
			{
				$language = $lang;
				$json[ "subtitle" ] = $text;
				break;
			}
		}

		if (isset($json[ "description" ])) 
		{
			foreach ($json[ "description" ] as $lang => $text)
			{
				$language = $lang;
				$json[ "description" ] = $text;
				break;
			}
		}

		if (isset($json[ "summary" ])) 
		{
			foreach ($json[ "summary" ] as $lang => $text)
			{
				$language = $lang;
				$json[ "summary" ] = $text;
				break;
			}
		}
		
		$json[ "language" ] = $language;
		$json[ "country"  ] = "xx";
		
		//
		// Remove bogous language from channel name.
		//
		
		$lang = $json[ "language" ];
		$json[ "channel" ] = trim(str_replace("($lang)","", $json[ "channel" ]));
		
		$json[ "updated" ] = gmdate("Y-m-d\TH:i:s\Z", gmp_intval($json[ "updated" ]));
		$json[ "start"   ] = gmdate("Y-m-d\TH:i:s\Z", gmp_intval($json[ "start"   ]));
		$json[ "stop"    ] = gmdate("Y-m-d\TH:i:s\Z", gmp_intval($json[ "stop"    ]));

		unset($json[ "id"      ]);
		unset($json[ "grabber" ]);
		unset($json[ "episode" ]);
		unset($json[ "dvb_eid" ]);

		saveEPG($json);

		//echo json_encdat($json) . "\n";	
		
		//if (++$count >= 1000) break;
	}
}

function sortEPG($a, $b)
{
	$starta = $a[ "start" ];
	$startb = $b[ "start" ];
	
	return ($starta > $startb) ? 1 : -1;
}

function splitEPGs()
{
	foreach ($GLOBALS[ "actchannels" ] as $channel => $cdata)
	{
		$tempfile = $cdata[ "file" ];
		$currfile = str_replace("0000.00.00","current", $tempfile);
		
		echo "Splitting " . $tempfile . "\n";
		
		$json = "[" . substr(file_get_contents($tempfile),0,-2) . "]";
		
		$epgs = json_decdat($json);		
		
		usort($epgs, "sortEPG");
		
		$mindate = "9999.99.99";
		$maxdate = "0000.00.00";
		
		foreach ($epgs as $epg)
		{
			$start = str_replace("-", ".", substr($epg[ "start" ], 0, 10));
			
			if ($start < $mindate) $mindate = $start;
			if ($start > $maxdate) $maxdate = $start;
		}
		
		$epgdays = array();
		
		foreach ($epgs as $epg)
		{
			$start = str_replace("-", ".", substr($epg[ "start" ], 0, 10));
			
			if ($start == $mindate) continue;
			if ($start == $maxdate) continue;
			
			if (! isset($epgdays[ $start ])) $epgdays[ $start ] = array();
			
			$epgdays[ $start ][] = $epg;
		}
		
		foreach ($epgdays as $day => $epgday)
		{
			$actfile = $cdata[ "dir" ] . "/" . $day . "." . $GLOBALS[ "hostname" ] . ".json"; 

			$epgwrite[ "epgdata" ] = $epgday;
			file_put_contents($actfile, json_encdat($epgwrite));
			
			echo "Writing " . $actfile . "\n";
		}
		
		unlink($tempfile);

		if (count($epgdays) > 0)
		{
			$epgswrite[ "epgdata" ] = $epgs;
			file_put_contents($currfile, json_encdat($epgswrite) . "\n");
			
			echo "Writing " . $currfile . "\n";
		}
		else
		{
			@rmdir(dirname($tempfile));
		}
	}
}

readHomedir();
readHostname();

readTags();
readChannels();
readNetworks();

readAstraConfig();

readEPG();

splitEPGs();

?>
