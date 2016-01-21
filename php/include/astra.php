<?php

function simplifySearchName($mname)
{
	$mname = mb_strtolower($mname, 'UTF-8');

	if (strpos($mname, " hd ") !== false) $mname = str_replace(" hd ", "", $mname) . " hd";
	if (strpos($mname, " sd ") !== false) $mname = str_replace(" sd ", "", $mname) . " sd";
	
	$mname = str_replace(" studio ", " ", $mname);
	$mname = str_replace(" fernsehen ", " ", $mname);
	  
	$mname = str_replace("-", " ", $mname);
	$mname = str_replace(".", " ", $mname);
	$mname = str_replace("è", "e", $mname);
	$mname = str_replace(" ",  "", $mname);
	
	$mname = str_replace("ä", "ae", $mname);
	$mname = str_replace("ö", "oe", $mname);
	$mname = str_replace("ü", "ue", $mname);
	$mname = str_replace("ß", "ss", $mname);

	return $mname;
}

function resolveAstraCountry($name, $country, $language)
{
	if (isset($GLOBALS[ "bypassrules" ][ $name ])) 
	{
		$name = $GLOBALS[ "bypassrules" ][ $name ];
	}
	
	$mname = simplifySearchName($name);

	//
	// Normal lookup.
	//
	
	$result = resolveAstraCountryDoit($name, $country, $language, $mname);	
	if ($result != "xx") return $result;
	
	//
	// HD removed lookup lookup.
	//
	
	if (substr($mname, -2) == "hd")
	{
		$mname = substr($mname, 0, -2);
		
		$result = resolveAstraCountryDoit($name, $country, $language, $mname);
		if ($result != "xx") return $result;
	}
	
	//
	// Country lookup.
	//
	
	if ($language == "ger") $mname = simplifySearchName($name . " Deutschland");
	if ($language == "dut") $mname = simplifySearchName($name . " Nederland");

	$result = resolveAstraCountryDoit($name, $country, $language, $mname);
	if ($result != "xx") return $result;

	$cachetag = "$name $country $language";
	
	if (! isset($GLOBALS[ "nxishow" ][ $cachetag ]))
	{
		$GLOBALS[ "nxishow" ][ $cachetag ] = true;
		echo "UNMATCHED: $name, $country, $language\n";
	}
	
	return $result;
}

function resolveAstraCountryDoit($name, $country, $language, $mname)
{
	$cachetag = "$mname $country $language";
	
	if (isset($GLOBALS[ "channelcache" ][ $cachetag ]))
	{
		return $GLOBALS[ "channelcache" ][ $cachetag ];
	}

	$isoccs = array();
	
	foreach ($GLOBALS[ "compacts" ] as $channel)
	{
		if ($mname == $channel[ "mname" ])
		{
 			$isoccs[] = $channel[ "isocc" ];
		}
	}
	
	/*
	if (count($isoccs) == 0)
	{
		foreach ($GLOBALS[ "compacts" ] as $channel)
		{
			$dist = levenshtein($mname, $channel[ "mname" ]);
			
			if ((strlen($mname) > 5) && ($dist <= 1))
			{
				$isoccs[] = $channel[ "isocc" ];
				
				echo "levenshtein $name => $country => $language :: " . $channel[ "name" ] . "\n";
			}
		}
	}
	*/
	
	if (count($isoccs) > 1)
	{
		$diffisoccs = array();
		
		foreach ($isoccs as $isocc) $diffisoccs[ $isocc ] = true;
		
		if (count($diffisoccs) > 1)
		{
			foreach ($isoccs as $isocc)
			{
				echo "DUPLICATE => $name => $country => $language :: $isocc\n";
			}
		}
	}
	
	$result = (count($isoccs) > 0) ? $isoccs[ 0 ] : $country;
	$GLOBALS[ "channelcache" ][ $cachetag ] = $result;
	
	return $result;
}

function readAstraConfig()
{
	$configname = dirname(__FILE__) . "/astra.config.json";
	$rulesname  = dirname(__FILE__) . "/astra.rules.json";

	if (! file_exists($configname))
	{
		$astraraw = file_get_contents("http://www.astra.de/webservice/v3/channels"
				 . "?limit=9999&page=1&domain_identifier=d3d3LmFzdHJhLmRl"
				 . "&free=yes&pay=yes");

		$astra = json_decdat($astraraw);

		//file_put_contents("./astra.config.json", json_encdat($astra)); exit();
		
		$genres = array();
		
		foreach ($astra[ "genres" ] as $genre)
		{
			$genres[ $genre[ "id" ] ] = $genre[ "name" ];
		}
		
		foreach ($astra[ "packages" ] as $package)
		{
			$packages[ $package[ "id" ] ] = $package[ "name" ];
		}
		
		$channels = $astra[ "channels" ];
		$compacts = array();
		
		foreach ($channels as $channel)
		{
			$compact = array();
			
			$compact[ "name"     ]  = $channel[ "name"       ];
			$compact[ "free"     ]  = $channel[ "free"       ];
			$compact[ "enc"      ]  = $channel[ "encryption" ];
			$compact[ "hd"       ]  = $channel[ "hd"         ];
			$compact[ "website"  ]  = $channel[ "website"    ];
			$compact[ "language" ]  = $channel[ "language"   ];
			$compact[ "logo"     ]  = $channel[ "logo_url"   ];
			$compact[ "type"     ]  = $channel[ "radio" ] ? "radio" : "tv";
			$compact[ "country"  ]  = $channel[ "originating_from" ];
			
			if (resolveCountry($compact[ "country" ]))
			{
				$compact[ "isocc" ]  = resolveCountry($compact[ "country" ]);
			}
			else
			{
				if ($compact[ "country" ])
				{
					echo "Unresolved country: " . $compact[ "country"  ] . "\n";
				}

				$compact[ "isocc"  ] = "xx";
				
				if ($compact[ "language" ] == "German") $compact[ "isocc"  ] = "de";
			}
			
			if (isset($channel[ "genre_id" ]) && $channel[ "genre_id" ])
			{
				$compact[ "genre"    ]  = $channel[ "genre_id"   ] . ":";
				$compact[ "genre"    ] .= $genres[ $channel[ "genre_id" ] ];
			}
			else
			{
				$compact[ "genre" ] = "0:Undefined";
			}
			
			if (count($channel[ "package_ids" ]) > 0)
			{
				$compact[ "packs" ] = array();

				foreach ($channel[ "package_ids" ] as $packid)
				{
					$compact[ "packs" ][] = $packid . ":" . $packages[ $packid ];
				}
			}
			
			$compacts[] = $compact;
		}
		
		file_put_contents($configname,json_encdat($compacts));
	}
	
	$GLOBALS[ "compacts" ] = json_decdat(file_get_contents($configname));
	
	foreach ($GLOBALS[ "compacts" ] as $inx => $channel)
	{
		$mname = simplifySearchName($channel[ "name" ]);
		$GLOBALS[ "compacts" ][ $inx ][ "mname" ] = $mname;
		
		if (($channel[ "hd" ] == true) && (substr($mname, -2) != "hd"))
		{
			$clone = $channel;
			$clone[ "mname" ] = $mname . "hd";
			$GLOBALS[ "compacts" ][] = $clone;
		}
		
		if (($channel[ "hd" ] == false) && (substr($mname, -2) != "sd"))
		{
			$clone = $channel;
			$clone[ "mname" ] = $mname . "sd";
			$GLOBALS[ "compacts" ][] = $clone;
		}
	}
	
	$bypassrules = json_decdat(file_get_contents($rulesname));
	
	foreach ($bypassrules as $rule)
	{
		$GLOBALS[ "bypassrules" ][ $rule[ 0 ] ] = $rule[ 1 ]; 
	}
}


























?>