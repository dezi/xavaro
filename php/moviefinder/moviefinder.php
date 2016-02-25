<?php

function identifyMovie($title)
{
	if (! isset($GLOBALS[ "movielist" ]))
	{
		$file = dirname(__FILE__) . "/movielist.de.txt";
		
		$fd = fopen($file, "r");
		
		while (($line = fgets($fd)) != false)
		{
			$line = trim($line);
			
			if (substr($line, -1) != ")") continue;
			if (substr($line, -7, -5) != " (") continue;
			
			$title = substr($line,  0, -7);
			$year  = substr($line, -5, -1);
			
			$GLOBALS[ "movielist" ][ $title ] = $year;
		}
		
		fclose($fd);
	}
	
	if (! isset($GLOBALS[ "movielist" ][ $title ])) return false;
	
	return $GLOBALS[ "movielist" ][ $title ];
}

?>