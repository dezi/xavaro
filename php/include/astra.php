<?php

function simplifySearchName($mname)
{
	$mname = mb_strtolower($mname, "UTF-8");

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

function resolveAstra($orgname, $name, $language)
{
	$isknown = false;
	
	if (isset($GLOBALS[ "astra.resolved" ][ $language . "." . $orgname ]))
	{
		$newname = $GLOBALS[ "astra.resolved" ][ $language . "." . $orgname ];
		if ($newname != "") $name = $newname;
		
		$isknown = true;
	}
	
	$result = resolveAstraPhases($orgname, $name, $language);
	
	if (! isset($GLOBALS[ "astra.newentry.json" ]))
	{
		$newentry = dirname(__FILE__) . "/astra.newentry.json";
		$GLOBALS[ "astra.newentry.json" ] = fopen($newentry,"w");
	}
	
	$cachetag = "$orgname $language";
	
	if ((! $isknown) && ! isset($GLOBALS[ "astra.newentry" ][ $cachetag ]))
	{
		$GLOBALS[ "astra.newentry" ][ $cachetag ] = true;
		
		$padorg = str_pad("\"" . $orgname . "\",", 32, " ");
		$padnew = "\"" . (($result != null) ? $result[ "name" ] : "") . "\"";
		
		$entry = "\t[ \"$language\", $padorg $padnew ],\n";
		
		fwrite($GLOBALS[ "astra.newentry.json" ],$entry);
		
		echo "NEW ENTRY: $orgname => $language\n";
	}
	
	return $result;
}

function resolveAstraPhases($orgname, $name, $language)
{
	$mname = simplifySearchName($name);
	
	//
	// Normal lookup.
	//
	
	$result = resolveAstraDoit($name, $language, $mname);	
	if ($result != null) return $result;
		
	//
	// Country lookup part 1.
	//
	
	if ($language == "ger") $mname = simplifySearchName($name . " Deutschland");
	if ($language == "dut") $mname = simplifySearchName($name . " Nederland");
	if ($language == "eng") $mname = simplifySearchName($name . " UK");

	$result = resolveAstraDoit($name, $language, $mname);
	if ($result != null) return $result;

	//
	// Country lookup part 2.
	//
	
	if ($language == "ger") $mname = simplifySearchName($name . " Deutsch");

	$result = resolveAstraDoit($name, $language, $mname);
	if ($result != null) return $result;

	return $result;
}

function resolveAstraDoit($name, $language, $mname)
{
	$cachetag = "$mname $language";
	
	if (isset($GLOBALS[ "astra.cache" ][ $cachetag ]))
	{
		return $GLOBALS[ "astra.cache" ][ $cachetag ];
	}

	$astras = array();
	$anames = array();
	$isoccs = array();
	
	foreach ($GLOBALS[ "astra.config" ] as $channel)
	{
		if (($mname == $channel[ "mname" ]) && ($language == $channel[ "isolang" ]))
		{
 			$anames[] = $channel[ "name"  ];
 			$isoccs[] = $channel[ "isocc" ];
 			$astras[] = $channel;
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
		$result[ "name"  ] = $anames[ 0 ];
		$result[ "isocc" ] = $isoccs[ 0 ];
		$result[ "astra" ] = $astras[ 0 ];
	}
	
	$GLOBALS[ "astra.cache" ][ $cachetag ] = $result;

	return $result;
}

function readAstraConfig()
{
	$configname   = dirname(__FILE__) . "/astra.config.json";
	$manualname   = dirname(__FILE__) . "/astra.manual.json";
	$resolvedname = dirname(__FILE__) . "/astra.resolved.json";
		
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
	}
	
	//
	// Read manual config.
	//
	
	$astramanual = json_decdat(file_get_contents($manualname));
	
	foreach ($astramanual as $inx => $channel)
	{
		if (! isset( $channel[ "name" ])) continue;
		
		$mname = simplifySearchName($channel[ "name" ]);
		$channel[ "mname" ] = $mname;
		$GLOBALS[ "astra.config" ][] = $channel;
	}
	
	//
	// Read resolved table.
	//

	$astraresolved = json_decdat(file_get_contents($resolvedname));
 
	foreach ($astraresolved as $rule)
	{
		if (count($rule) == 0) continue;
		
		$GLOBALS[ "astra.resolved" ][ $rule[ 0 ] . "." . $rule[ 1 ] ] = $rule[ 2 ]; 
	}
}

?>