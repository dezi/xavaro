<?php

function simplifySearchName($mname)
{
	$mname = mb_strtolower($mname, "UTF-8");
	$mname = remove_accents($mname);

	$mname = str_replace("-", " ", $mname);
	$mname = str_replace("_", " ", $mname);
	$mname = str_replace(".", " ", $mname);

	$mname = str_replace("ä", "ae", $mname);
	$mname = str_replace("ö", "oe", $mname);
	$mname = str_replace("ü", "ue", $mname);
	$mname = str_replace("ß", "ss", $mname);

	$mname = str_replace(" ",  "", $mname);

	return $mname;
}

function isBrainDead($language, $channel)
{
	if (! isset($GLOBALS[ "channels.braindead" ]))
	{
		$bdfile  = "../include/channels.braindead.json";
		$bdtable = json_decdat(file_get_contents($bdfile));
		
		$GLOBALS[ "channels.braindead" ] = $bdtable;
	}
	
	$bdtable = $GLOBALS[ "channels.braindead" ];

	$stag = "$language.$channel";
	
	foreach ($bdtable as $braindead)
	{
		if ($braindead == $stag) return true;
	}
	
	return false;
}

function resolveChannel($orgname, $name, $language)
{
	$isknown = false;
	
	if (isset($GLOBALS[ "channels.resolved" ][ $language . "." . $orgname ]))
	{
		$newname = $GLOBALS[ "channels.resolved" ][ $language . "." . $orgname ];
		if ($newname != "") $name = $newname;
		
		$isknown = true;
	}
	
	$result = resolveChannelPhases($orgname, $name, $language);
	
	if (! isset($GLOBALS[ "channels.newentry.json" ]))
	{
		$newentry = dirname(__FILE__) . "/channels.newentry.json";
		$GLOBALS[ "channels.newentry.json" ] = fopen($newentry,"w");
	}
	
	$cachetag = "$orgname $language";
	
	if ((! $isknown) && ! isset($GLOBALS[ "channels.newentry" ][ $cachetag ]))
	{
		$GLOBALS[ "channels.newentry" ][ $cachetag ] = true;
		
		$padorg = str_pad("\"" . $orgname . "\",", 32, " ");
		$padnew = "\"" . (($result != null) ? $result[ "name" ] : "") . "\"";
		
		$entry = "\t[ \"$language\", $padorg $padnew ],\n";
		
		fwrite($GLOBALS[ "channels.newentry.json" ],$entry);
		
		echo "NEW ENTRY: $orgname => $language\n";
	}
	
	return $result;
}

function resolveChannelPhases($orgname, $name, $language)
{
	$mname = simplifySearchName($name);
	
	//
	// Normal lookup.
	//
	
	$result = resolveChannelDoit($name, $language, $mname);	
	if ($result != null) return $result;
		
	//
	// Country lookup part 1.
	//
	
	if ($language == "ger") $mname = simplifySearchName($name . " Deutschland");
	if ($language == "dut") $mname = simplifySearchName($name . " Nederland");
	if ($language == "eng") $mname = simplifySearchName($name . " UK");

	$result = resolveChannelDoit($name, $language, $mname);
	if ($result != null) return $result;

	//
	// Country lookup part 2.
	//
	
	if ($language == "ger") $mname = simplifySearchName($name . " Deutsch");

	$result = resolveChannelDoit($name, $language, $mname);
	if ($result != null) return $result;

	return $result;
}

function resolveChannelDoit($name, $language, $mname)
{
	$cachetag = "$mname $language";
	
	if (isset($GLOBALS[ "channels.cache" ][ $cachetag ]))
	{
		return $GLOBALS[ "channels.cache" ][ $cachetag ];
	}

	$cdefs  = array();
	$anames = array();
	$isoccs = array();
	
	foreach ($GLOBALS[ "channels.config" ] as $channel)
	{
		if (($mname == $channel[ "mname" ]) && ($language == $channel[ "isolang" ]))
		{
 			$cdefs[]  = $channel;
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
				
				echo "DUPLICATE => $name => $language :: $isocc $aname\n";
			}
		}
	}
	
	$result = null;
	
	if (count($isoccs) > 0)
	{
		$result[ "cdefs" ] = $cdefs [ 0 ];
		$result[ "name"  ] = $anames[ 0 ];
		$result[ "isocc" ] = $isoccs[ 0 ];
	}
	
	$GLOBALS[ "channels.cache" ][ $cachetag ] = $result;

	return $result;
}

function readChannelConfig()
{
	$configname   = dirname(__FILE__) . "/channels.config.json";
	$manualname   = dirname(__FILE__) . "/channels.manual.json";
	$resolvedname = dirname(__FILE__) . "/channels.resolved.json";
		
	if (! file_exists($configname))
	{
		$configraw = file_get_contents("http://www.astra.de/webservice/v3/channels"
				 . "?limit=9999&page=1&domain_identifier=d3d3LmFzdHJhLmRl"
				 . "&free=yes&pay=yes");

		$config = json_decdat($configraw);
		
		$genres = array();
		
		foreach ($config[ "genres" ] as $genre)
		{
			$genres[ $genre[ "id" ] ] = $genre[ "name" ];
		}
		
		foreach ($config[ "packages" ] as $package)
		{
			$packages[ $package[ "id" ] ] = $package[ "name" ];
		}
		
		$channels = $config[ "channels" ];
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
	
	$GLOBALS[ "channels.config" ] = json_decdat(file_get_contents($configname));
	
	foreach ($GLOBALS[ "channels.config" ] as $inx => $channel)
	{
		$mname = simplifySearchName($channel[ "name" ]);
		$GLOBALS[ "channels.config" ][ $inx ][ "mname" ] = $mname;
	}
	
	//
	// Read manual config.
	//
	
	$configmanual = json_decdat(file_get_contents($manualname));
	
	foreach ($configmanual as $inx => $channel)
	{
		if (! isset( $channel[ "name" ])) continue;
		
		$mname = simplifySearchName($channel[ "name" ]);
		$channel[ "mname" ] = $mname;
		
		array_unshift($GLOBALS[ "channels.config" ], $channel);
	}
	
	//
	// Read resolved table.
	//

	$channelsresolved = json_decdat(file_get_contents($resolvedname));
 
	foreach ($channelsresolved as $rule)
	{
		if (count($rule) == 0) continue;
		
		$GLOBALS[ "channels.resolved" ][ $rule[ 0 ] . "." . $rule[ 1 ] ] = $rule[ 2 ]; 
	}
}

?>