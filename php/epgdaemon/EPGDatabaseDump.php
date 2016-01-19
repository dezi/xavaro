<?php

include("../include/json.php");

$epgdatabase = "/home/pi/.hts/tvheadend/epgdb.v2";

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
	$val = gmp_init(0);

	for ($inx = 0; $inx < $len; $inx++)
	{ 
		$data = fread($fd,1);

		$val = gmp_mul($val, gmp_init(256));
		$val = gmp_add($val, gmp_init(ord($data[ 0 ])));
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

function readEPG($epgdatabase)
{
$fd = fopen($epgdatabase,"r");

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

	if ($section != "broadcasts") continue;

	$channel = $json[ "channel" ];
	$channel = $GLOBALS[ "channels" ][ $channel ];

	$service = $channel[ "services" ];
	$service = $GLOBALS[ "services" ][ $service[ 0 ] ];

	if (isset($service[ "svcname"     ])) $json[ "channel"  ] = $service[ "svcname"     ];
	if (isset($service[ "provider"    ])) $json[ "provider" ] = $service[ "provider"    ];
	if (isset($service[ "networkname" ])) $json[ "network"  ] = $service[ "networkname" ];

	$json[ "tags" ] = array();

	$tags = $channel[ "tags" ];
	for ($inx = 0; $inx < count($tags); $inx++)
	{
		$json[ "tags" ][] = $GLOBALS[ "tags" ][ $tags[ $inx ] ][ "name" ];
	}

	unset($json[ "grabber" ]);
	unset($json[ "episode" ]);
	unset($json[ "dvb-eid" ]);

	echo json_encdat($json) . "\n";	
}
}

readEPG($epgdatabase)

?>
