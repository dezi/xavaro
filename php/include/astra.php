<?php

function simplifySearchName($mname)
{
	$mname = mb_strtolower($mname, 'UTF-8');

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

function resolveAstraDump($orgname, $language, $name)
{
	if (isset($GLOBALS[ "epgnames" ][ $name ])) 
	{
		$orgname = $name;
		$name = $GLOBALS[ "epgnames" ][ $name ];
		
		echo "RESOLVER: $orgname => $name\n";
		
		fwrite($GLOBALS[ "epgdump" ],"\t[ \"$language\", \"$orgname\", \"$name\" ],\n");
	}
}

function resolveAstraCountry($orgname, $name, $country, $language)
{
	$result = resolveAstraPhases($orgname, $name, $country, $language);
	
	if (! isset($GLOBALS[ "astra.resolved.json" ]))
	{
		$GLOBALS[ "astra.resolved.json" ] = fopen(dirname(__FILE__) . "/astra.resolved.json","w");
	}
	
	$cachetag = "$orgname $country $language";
	
	if (! isset($GLOBALS[ "astra.resolved" ][ $cachetag ]))
	{
		$GLOBALS[ "astra.resolved" ][ $cachetag ] = true;
		
		$padorg = str_pad("\"" . $orgname . "\",", 32, " ");
		$padnew = "\"" . (($result != null) ? $result[ "name" ] : "") . "\"";
		
		$entry = "\t[ \"$language\", $padorg $padnew ],\n";
		
		fwrite($GLOBALS[ "astra.resolved.json" ],$entry);
	}
	
	return $result;
}

function resolveAstraPhases($orgname, $name, $country, $language)
{
	$mname = simplifySearchName($name);
	
	//
	// Normal lookup.
	//
	
	$result = resolveAstraCountryDoit($name, $country, $language, $mname);	
	if ($result != null) return $result;
		
	//
	// Country lookup part 1.
	//
	
	if ($language == "ger") $mname = simplifySearchName($name . " Deutschland");
	if ($language == "dut") $mname = simplifySearchName($name . " Nederland");
	if ($language == "eng") $mname = simplifySearchName($name . " UK");

	$result = resolveAstraCountryDoit($name, $country, $language, $mname);
	if ($result != null) return $result;

	//
	// Country lookup part 2.
	//
	
	if ($language == "ger") $mname = simplifySearchName($name . " Deutsch");

	$result = resolveAstraCountryDoit($name, $country, $language, $mname);
	if ($result != null) return $result;

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
		if (($mname == $channel[ "mname" ]) && ($language == $channel[ "isolang" ]))
		{
 			$anames[] = $channel[ "name"  ];
 			$isoccs[] = $channel[ "isocc" ];
		}
	}
	
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
	
	$result = null;
	
	if (count($isoccs) > 0)
	{
		$result[ "name"  ] = $anames[ 0 ];
		$result[ "isocc" ] = $isoccs[ 0 ];
	}
	
	$GLOBALS[ "channelcache" ][ $cachetag ] = $result;

	return $result;
}

function readAstraConfig()
{
	$configname     = dirname(__FILE__) . "/astra.config.json";
	$manualname     = dirname(__FILE__) . "/astra.manual.json";
	$unresolvedname = dirname(__FILE__) . "/astra.unresolved.json";
	
	$GLOBALS[ "unresolved" ] = fopen($unresolvedname,"w");
	
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
			
			//
			// Unify channel name.
			//
			
			$name = $channel[ "name" ];
			$ishd = $channel[ "hd"   ];
	
			if (strpos($name, " HD ") !== false) $name = str_replace(" HD ", "", $name) . " HD";
			if (strpos($name, " SD ") !== false) $name = str_replace(" SD ", "", $name) . " SD";
	
			if ($ishd && (substr($name, -2) != "HD")) $name .= " HD";
			
			$channel[ "name" ] = $name;
			
			//
			// Build compact record.
			//
			
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
	
	$astramanual = json_decdat(file_get_contents($manualname));
	
	foreach ($astramanual as $inx => $channel)
	{
		if (! isset( $channel[ "name" ])) continue;
		
		$mname = simplifySearchName($channel[ "name" ]);
		$channel[ "mname" ] = $mname;
		$GLOBALS[ "astra.config" ][] = $channel;
	}
}


























?>