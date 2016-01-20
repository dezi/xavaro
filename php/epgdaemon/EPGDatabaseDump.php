<?php

include("../include/json.php");

$epgdatabase = "~/.hts/tvheadend/epgdb.v2";

function readInt($fd)
{
	$len = 4;
	$val = 0;

	for ($inx = 0; $inx < $len; $inx++)
	{ 
		$data = fread($fd,1);

		if ($data == null) return -1;

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

function readByte($fd)
{
	$data = fread($fd,1);
	$data = unpack("C",$data);
	return $data[ 1 ];
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

		$name = readString($fd,$nlen);

		$rest += 6 + $nlen + $dlen;

		if (($type == 1) || ($type == 5))
		{
			$subj = array();
			decodeLength($fd, $dlen, $subj, $level + 1);

			if ($name == "")
				$json[] = $subj;
			else
				$json[ $name ] = $subj;

			//echo "$pad$type $nlen $dlen $name\n";

			continue;
		}

		if ($type == 2)
		{
			$number = readNumber($fd,$dlen);

			if ($name == "")
				$json[] = $number;
			else
			    $json[ $name ] = $number;

			//echo "$pad$type $nlen $dlen $name => " . gmp_strval($number) . "\n";

			continue;
		}

		if ($type == 3)
		{
			$data = readString($fd,$dlen);
			
			if ($name == "")
				$json[] = $data;
			else
				$json[ $name ] = $data;

			//echo "$pad$type $nlen $dlen $name => $data\n";

			continue;
		}

		$data = readString($fd,$dlen);
		$dump = "";
		for ($inx = 0; $inx < strlen($data); $inx++) $dump .= ord($data[ $inx ]) . " ";

		echo "==========================$type $nlen $dlen $name $dump\n";
		exit(0);
	}
}

function readChannels()
{
	if (! isset($GLOBALS[ "channels" ])) $GLOBALS[ "channels" ] = array();

	$channeldir = "/home/pi/.hts/tvheadend/channel/config";

	$dfd = opendir($channeldir);

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

	$dfd = opendir($tagsdir);

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

	$dfd = opendir($networksdir);

	while (($entry = readdir($dfd)) !== false)
	{
		if ($entry == ".") continue;
		if ($entry == "..") continue;

		$jsondata = file_get_contents($networksdir . "/" . $entry . "/config");
		$json = json_decdat($jsondata);

		$GLOBALS[ "networks" ][ $entry ] = $json;
		
		readMuxes($networksdir . "/" . $entry . "/muxes", $json[ "networkname" ]);
	}

	closedir($dfd);
}

function readMuxes($muxesdir, $networkname)
{
	if (! isset($GLOBALS[ "muxes" ])) $GLOBALS[ "muxes" ] = array();

	$dfd = opendir($muxesdir);

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

	$dfd = opendir($servicesdir);

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

readTags();
readChannels();
readNetworks();

function saveEPG($epg)
{
	$country = $epg[ "country" ];
	$channel = $epg[ "channel" ];

	$actfile = "../../var/epg/" . $country . "/" . $channel . ".json"; 
	
	if ((! isset($GLOBALS[ "actfile" ])) || ($GLOBALS[ "actfile" ] != $actfile))
	{
		if (isset($GLOBALS[ "actfd" ]))
		{
			fclose($GLOBALS[ "actfd" ]);
			unset($GLOBALS[ "actfd" ]);
		}

		$GLOBALS[ "actfd"   ] = fopen($actfile,"a+");
		$GLOBALS[ "actfile" ] = $actfile;
	}

	fwrite($GLOBALS[ "actfd" ],json_encdat($epg) . ",\n");
}

function readEPG($epgdatabase)
{
	if (substr($epgdatabase, 0, 2) == "~/")
	{	
		$env = posix_getpwuid(posix_getuid());

		$epgdatabase = $env[ "dir" ] . substr($epgdatabase,1);
	}

	$fd = fopen($epgdatabase,"r");
	if ($fd === false)
	{
		echo "Cannot open database <$epgdatabase>\n";
		exit(-1);
	}

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

		//"uri":"tvh://channel-004f9c62d6081d0aa2b1e766b90568a8/bcast-10349/episode",

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

	if (isset($service[ "svcname"     ])) $json[ "channel"  ] = $service[ "svcname"     ];
	if (isset($service[ "provider"    ])) $json[ "provider" ] = $service[ "provider"    ];
	if (isset($service[ "networkname" ])) $json[ "network"  ] = $service[ "networkname" ];

	$json[ "tags" ] = array();

	$mode = "tv";

	$tags = $channel[ "tags" ];

	for ($inx = 0; $inx < count($tags); $inx++)
	{
		$tag = $GLOBALS[ "tags" ][ $tags[ $inx ] ][ "name" ];
		if ($tag == "Radio") $mode = "radio";
		$json[ "tags" ][] = $tag;
	}

	$json[ "tags" ] = implode("|",$json[ "tags" ]);
	$json[ "mode" ] = $mode;

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

	$json[ "country"  ] = "ger";
	$json[ "language" ] = $language;

	$json[ "updated" ] = gmdate("Y-m-d\TH:i:s\Z", gmp_intval($json[ "updated" ]));
	$json[ "start"   ] = gmdate("Y-m-d\TH:i:s\Z", gmp_intval($json[ "start"   ]));
	$json[ "stop"    ] = gmdate("Y-m-d\TH:i:s\Z", gmp_intval($json[ "stop"    ]));

	unset($json[ "id"      ]);
	unset($json[ "type"    ]);
	unset($json[ "grabber" ]);
	unset($json[ "episode" ]);
	unset($json[ "dvb_eid" ]);

	saveEPG($json);

	echo json_encdat($json) . "\n";	
}
}

readEPG($epgdatabase)

?>
