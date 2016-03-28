<?php



function get($az)
{
	$page = file_get_contents("http://tierdoku.de/index.php?title=Tiere-" . $az);
	
	$lines = explode("\n", $page);
	
	for ($inx = 0; $inx < count($lines); $inx++)
	{
		$line = $lines[ $inx ];
		
		if (substr($line, 0, 13) != "<td> <a href=") continue;
		
		$line = substr($line,14);
		$pos1 = strpos($line, ">") + 1;
		$pos2 = strpos($line, "</a>");
		
		$name = substr($line, $pos1, $pos2 - $pos1);
		$latin = substr($lines[ $inx + 1 ], 13, -4);
		$class = substr($lines[ $inx + 2 ], 13, -4);
		
		$wikiname = str_replace(" ", "_", $name);
		$wikiurl = "https://de.wikipedia.org/wiki/" . $name;
		
		if (@file_get_contents($wikiurl))
		{
			echo "$name => $latin => $class => $wikiname\n";
		}
	}
}

for ($inx = 0; $inx < 26; $inx++) get(chr(ord("A") + $inx));

?>