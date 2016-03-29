<?php

include("/Users/dezi/xavaro/php/include/json.php");

$result = array();
	
for ($buch = 0; $buch <= 26; $buch++)
{
	$ccc = chr(ord("A") + $buch);
	
	$page = file_get_contents("http://tierdoku.com/index.php?title=Tiere-" . $ccc);
	$lines = explode("\n", $page);

	for ($inx = 0; $inx < count($lines); $inx++)
	{
		$line = $lines[ $inx ];
		if (substr($line, 0, 13) != "<td> <a href=") continue;
		
		$line = substr($line, 14);
		$pos1 = strpos($line, ">") + 1;
		$pos2 = strpos($line, "</a>");
		
		$name = substr($line, $pos1, $pos2 - $pos1);
		
		$latin = substr($lines[ $inx + 1], 13, -4);
		$class = substr($lines[ $inx + 2], 13, -4);
		
		$wikiname = str_replace(" ", "_", $name);
		$wikiurl = "https://de.wikipedia.org/wiki/" . $wikiname;
		
		if (@file_get_contents($wikiurl))
		{
			echo "OK: $name => $latin => $class => $wikiname\n";
		}
		else
		{
			echo "ER: $name => $latin => $class => $wikiname\n";
			continue;
		}
		
		if (! isset($result[ $class ])) $result[ $class ] = array();
		
		$data = array();
		$data[] = $name;
		$data[] = $latin;
		$data[] = 1;
		
		$result[ $class ][] = $data;
	}
	
	file_put_contents("tiere.json", json_encdat($result));
}


?>