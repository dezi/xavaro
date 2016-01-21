<?php

function resolveLanguage($language)
{
	if (isset($GLOBALS[ "languagecache" ][ $language ]))
	{
		return $GLOBALS[ "languagecache" ][ $language ];
	}

	if (! isset($GLOBALS[ "languages" ]))
	{
		$json = file_get_contents(dirname(__FILE__) . "/languages.json");
		
		$languages = json_decdat($json);
		$GLOBALS[ "languages" ] = array();
		
		foreach ($languages as $language)
		{
			$parts = explode(", ", $language[ 0 ]);
			
			foreach ($parts as $part)
			{
				$entry = array();
				$entry[ 0 ] = trim($part);
				$entry[ 1 ] = $language[ 1 ];
				
				$GLOBALS[ "languages" ][] = $entry;
			}
		}
	}
	
	$ccc = null;
	
	$list = explode(",",$language);
	
	foreach ($list as $lc)
	{
		$lc = trim($lc);
		
		foreach ($GLOBALS[ "languages" ] as $desc)
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
		}
		
		if ($ccc != null) break;
	}
	
	$GLOBALS[ "languagecache" ][ $language ] = $ccc;
	
	return $ccc;
}

?>
