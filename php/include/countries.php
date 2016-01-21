<?php

function resolveCountry($country)
{
	if (isset($GLOBALS[ "countrycache" ][ $country ]))
	{
		return $GLOBALS[ "countrycache" ][ $country ];
	}

	if (! isset($GLOBALS[ "countries" ]))
	{
		$json = file_get_contents(dirname(__FILE__) . "/countries.json");
		$GLOBALS[ "countries" ] = json_decdat($json);
	}
	
	$ccc = null;
	
	$list = explode(",",$country);
	
	foreach ($list as $lc)
	{
		$lc = trim($lc);
		
		foreach ($GLOBALS[ "countries" ] as $desc)
		{
			if (mb_strtolower($desc[ 0 ], 'UTF-8') == mb_strtolower($lc, 'UTF-8'))
			{
				$ccc = mb_strtolower($desc[ 1 ], 'UTF-8');
				break;
			}
			
			if (mb_strtolower($desc[ 1 ], 'UTF-8') == mb_strtolower($lc, 'UTF-8'))
			{
				$ccc = mb_strtolower($desc[ 1 ], 'UTF-8');
				break;
			}
			
			if (mb_strtolower($desc[ 2 ], 'UTF-8') == mb_strtolower($lc, 'UTF-8'))
			{
				$ccc = mb_strtolower($desc[ 1 ], 'UTF-8');
				break;
			}
		}
		
		if ($ccc != null) break;
	}
	
	$GLOBALS[ "countrycache" ][ $country ] = $ccc;
	
	return $ccc;
}

?>
