<?php

function simplifySearchName($mname)
{
	$mname = mb_strtolower($mname, 'UTF-8');

	if (strpos($mname, " hd ") !== false) $mname = str_replace(" hd ", "", $mname) . " hd";
	if (strpos($mname, " sd ") !== false) $mname = str_replace(" sd ", "", $mname) . " sd";
	
	$mname = str_replace(" studio ", " ", $mname);
	$mname = str_replace(" fernsehen ", " ", $mname);
	  
	$mname = str_replace("-", " ", $mname);
	$mname = str_replace("_", " ", $mname);
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
	if (isset($GLOBALS[ "astra.rules" ][ $name ])) 
	{
		$name = $GLOBALS[ "astra.rules" ][ $name ];
	}
	
	$mname = simplifySearchName($name);

	//
	// Remove language tag from channel if present.
	//
	
	$mname = str_replace("($language)", "", $mname);
	
	//
	// Normal lookup.
	//
	
	$result = resolveAstraCountryDoit($name, $country, $language, $mname);	
	if ($result != "xx") return $result;
		
	//
	// HD add lookup.
	//
	
	if (substr($mname, -2) != "hd")
	{
		$result = resolveAstraCountryDoit($name, $country, $language, $mname . "hd");
		if ($result != "xx") return $result;
	}
	
	//
	// HD remove lookup.
	//
	
	if (substr($mname, -2) == "hd")
	{	
		$result = resolveAstraCountryDoit($name, $country, $language, substr($mname, 0, -2));
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
	$anames = array();
	
	foreach ($GLOBALS[ "astra.config" ] as $channel)
	{
		if ($mname == $channel[ "mname" ])
		{
 			$anames[] = $channel[ "name"  ];
 			$isoccs[] = $channel[ "isocc" ];
		}
	}
	
	/*
	if (count($isoccs) == 0)
	{
		foreach ($GLOBALS[ "astra.config" ] as $channel)
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
			for ($inx = 0; $inx < count($isoccs); $inx++)
			{
				$isocc = $isoccs[ $inx ];
				$aname = $anames[ $inx ];
				
				echo "DUPLICATE => $name => $country => $language :: $isocc $aname\n";
			}
		}
	}
	
	$result = "xx";
	
	if (count($isoccs) > 0)
	{
		$isocc = $isoccs[ 0 ];
		$aname = $anames[ 0 ];
		
		$result = $isocc;
	}
	
	$GLOBALS[ "channelcache" ][ $cachetag ] = $result;
	
	if ($result != "xx")
	{
		//echo "POSITIVE => $name => $country => $language :: $result $aname \n";
	}
	
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
			
			if (resolveLanguage($compact[ "language" ]))
			{
				$compact[ "isolang" ]  = resolveLanguage($compact[ "language" ]);
			}
			else
			{
				if ($compact[ "language" ])
				{
					echo "Unresolved language: " . $compact[ "language"  ] . "\n";
				}

				$compact[ "isolang"  ] = "xxx";
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
	
	$GLOBALS[ "astra.config" ] = json_decdat(file_get_contents($configname));
	
	foreach ($GLOBALS[ "astra.config" ] as $inx => $channel)
	{
		$mname = simplifySearchName($channel[ "name" ]);
		$GLOBALS[ "astra.config" ][ $inx ][ "mname" ] = $mname;
		
		if (($channel[ "hd" ] == true) && (substr($mname, -2) != "hd"))
		{
			$clone = $channel;
			$clone[ "mname" ] = $mname . "hd";
			$GLOBALS[ "astra.config" ][] = $clone;
		}
		
		if (($channel[ "hd" ] == false) && (substr($mname, -2) != "sd"))
		{
			$clone = $channel;
			$clone[ "mname" ] = $mname . "sd";
			$GLOBALS[ "astra.config" ][] = $clone;
		}
	}
	
	$astrarules = json_decdat(file_get_contents($rulesname));

	foreach ($astrarules as $rule)
	{
		$GLOBALS[ "astra.rules" ][ $rule[ 0 ] ] = $rule[ 1 ]; 
	}
}


























?>