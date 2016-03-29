<?php

include("/Users/dezi/xavaro/php/include/json.php");

$file = "tiere.json";

$result = array();

if (file_exists($file))
{
    $result = json_decdat(file_get_contents("tiere.json"));

    $index[] = array();

    foreach ($result as $class => $list)
    {
        foreach ($list as $inx => $array)
        {
            $key = $class . "|" . $array[ 0 ] . "|" . $array[ 1 ];
            $index[ $key ] = true;
        }
    }
}

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

        $key = $class . "|" . $name . "|" . $latin;

        if (isset($index[ $key ]))
        {
			echo "OL: $name => $latin => $class\n";
            continue;
        }

		$found = false;

		$wikiname = str_replace(" ", "_", $name);
		$wikiurl = "https://de.wikipedia.org/wiki/" . $wikiname;

		if (($found === false) && @file_get_contents($wikiurl))
		{
		    $found = true;
		}

		if ($found === false)
		{
			$wikiname = urlencode(str_replace(" ", "_", $name));
			$wikiurl = "https://de.wikipedia.org/wiki/" . $wikiname;

			if (@file_get_contents($wikiurl)) $found = true;
		}

		if ($found === false)
		{
			$wikiname = str_replace(" ", "_", $latin);
			$wikiurl = "https://de.wikipedia.org/wiki/" . $wikiname;

			if (@file_get_contents($wikiurl)) $found = true;
		}

		if ($found === false)
		{
			echo "ER: $name => $latin => $class => $wikiname\n";

			continue;
		}
		else
		{
			echo "OK: $name => $latin => $class => $wikiname\n";
		}

		if (! isset($result[ $class ])) $result[ $class ] = array();
		
		$data = array();
		$data[] = $name;
		$data[] = $latin;

		if (($wikiname == str_replace(" ", "_", $name)) ||
		    ($wikiname == urlencode(str_replace(" ", "_", $name))))
		{
		    $data[] = 1;
		}
		else
		{
               $data[] = $wikiname;
		}

		$result[ $class ][] = $data;
	}
	
	file_put_contents($file, json_encdat($result));
}

?>