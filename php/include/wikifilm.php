<?php

function readWiki()
{
	if (! isset($GLOBALS[ "wikifilm" ]))
	{
		$GLOBALS[ "wikifilm" ] = array();

		if (file_exists("../include/wikifilm.done.json"))
		{
			$GLOBALS[ "wikifilm" ] = json_decdat(file_get_contents("../include/wikifilm.done.json"));
		}
	}
}

function getWikiName(&$epg)
{
	$okiyear = null;		
	$badyear = null;		
	
	if (($badyear === null) && isset($epg[ "title" ]) && 
		preg_match("/^[^0-9(]{1,48}([12][0-9][0-9][0-9])/", $epg[ "title" ], $matches))
	{
		$badyear = $matches[ 1 ];
	}
	
	if (($okiyear === null) && isset($epg[ "subtitle" ]) && 
		preg_match("/^[^0-9(]{1,48}([12][0-9][0-9][0-9])/", $epg[ "subtitle" ], $matches))
	{
		$okiyear = $matches[ 1 ];
	}
	
	if (($okiyear === null) && isset($epg[ "description" ]) &&
		preg_match("/^[^0-9(]{1,48}([12][0-9][0-9][0-9])/", $epg[ "description" ], $matches))
	{
		$okiyear = $matches[ 1 ];
	}
	
	if ($okiyear === null) 
	{
		$okiyear = identifyMovie($epg[ "title" ]);
	}
	
	if (($okiyear == null) || ($badyear != null) ||
		($okiyear < 1900) || ($okiyear > 2020))
	{
		$title = stripWikiSearch($epg[ "title" ]);
		return $title;
	}
	
	$title = stripWikiSearch($epg[ "title" ]);
	$title = $title . " (" . $okiyear . ")";
	
	return $title;
}

function stripWikiSearch($title)
{
	$title = trim(preg_replace("/\([^)]*\)/", "", $title));
	$title = trim(str_replace("  ", " ", $title));
	$title = trim(str_replace("  ", " ", $title));

	return $title;
}

function getWikiFilm(&$epg)
{
	$title = getWikiName($epg);
	if ($title == null) return null;
	
	if (! isset($GLOBALS[ "wikifilm" ])) readWiki();

	if (isset($GLOBALS[ "wikifilm" ][ $title ]))
	{
		$epg[ "wikifilm" ] = $GLOBALS[ "wikifilm" ][ $title ];
	}
}

function generateWiki(&$epg)
{
	$title = getWikiName($epg);
	if ($title == null) return null;
	
	if (! isset($GLOBALS[ "wikidone" ])) readWikiWork();

	if (isset($GLOBALS[ "wikidone" ][ $title ]))
	{
		$epg[ "wikifilm" ] = $GLOBALS[ "wikidone" ][ $title ];
		return null; //"OLD====> $title => " . $epg[ "wikifilm" ];
	}
	
	if (! (isset($GLOBALS[ "wikidone" ][ $title ]) 
	    || isset($GLOBALS[ "wikifail" ][ $title ])))
	{
		$url = stripWikiSearch($epg[ "title" ]);
		$url = trim(str_replace(" ", "_", $url));
		$url = trim(str_replace("&", "%26", $url));

		$wikiurl = null;
		
		if ($wikiurl == null)
		{
			if (@file_get_contents("http://de.wikipedia.org/wiki/" . $url))
			{
				$wikiurl = $url;
			}
		}
		
		if ($wikiurl == null)
		{
			$url2 = stripWikiSearch($epg[ "title" ]);
			$url2 = trim(str_replace(" II ",  " 2 ", $url2 . " "));
			$url2 = trim(str_replace(" III ", " 3 ", $url2 . " "));
			$url2 = trim(str_replace(" IV ",  " 4 ", $url2 . " "));
			$url2 = trim(str_replace(" V ",   " 5 ", $url2 . " "));
			$url2 = trim(str_replace(" VI ",  " 6 ", $url2 . " "));
			$url2 = trim(str_replace(" ", "_", $url2));
			$url2 = trim(str_replace("&", "%26", $url2));

			if (($url2 != $url) && @file_get_contents("http://de.wikipedia.org/wiki/" . $url2))
			{
				$wikiurl = $url;
			}
		}
		
		if ($wikiurl == null)
		{
			$url2 = stripWikiSearch($epg[ "title" ]);
			$url2 = trim(str_replace(" 2 ", " II ",  $url2 . " "));
			$url2 = trim(str_replace(" 3 ", " III ", $url2 . " "));
			$url2 = trim(str_replace(" 4 ", " IV ",  $url2 . " "));
			$url2 = trim(str_replace(" 5 ", " V ",   $url2 . " "));
			$url2 = trim(str_replace(" 6 ", " VI ",  $url2 . " "));
			$url2 = trim(str_replace(" ", "_", $url2));
			$url2 = trim(str_replace("&", "%26", $url2));

			if (($url2 != $url) && @file_get_contents("http://de.wikipedia.org/wiki/" . $url2))
			{
				$wikiurl = $url;
			}
		}
		
		if ($wikiurl != null)
		{
			$GLOBALS[ "wikidone" ][ $title ] = $wikiurl;
			$epg[ "wikifilm" ] = $GLOBALS[ "wikidone" ][ $title ];

			writeWikiWork();
		}
		else
		{
			$GLOBALS[ "wikifail" ][ $title ] = false;
			unset($epg[ "wikifilm" ]);
			
			writeWikiWork();
		}
		
		return "NEW====> $title => $wikiurl";
	}
	
	return null;
}

function readWikiWork()
{
	$GLOBALS[ "wikidone" ] = array();
	$GLOBALS[ "wikifail" ] = array();

	if (file_exists("../include/wikifilm.done.json"))
	{
		$GLOBALS[ "wikidone" ] = json_decdat(file_get_contents("../include/wikifilm.done.json"));
	}
	
	if (file_exists("../include/wikifilm.done.json"))
	{
		$GLOBALS[ "wikifail" ] = json_decdat(file_get_contents("../include/wikifilm.fail.json"));
	}
}

function writeWikiWork()
{
	ksort($GLOBALS[ "wikidone" ]);
	ksort($GLOBALS[ "wikifail" ]);
	
	file_put_contents("../include/wikifilm.done.json", json_encdat($GLOBALS[ "wikidone" ]));
	file_put_contents("../include/wikifilm.fail.json", json_encdat($GLOBALS[ "wikifail" ]));
}



?>
