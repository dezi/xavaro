<?php

include("/Users/dezi/xavaro/php/include/json.php");

$lines = explode("\n", file_get_contents("pflanzen.txt"));
$count = count($lines);
$cols = $count / 4;

echo "$cols => $count\n";

$array = array();

for ($inx = 0; $inx < $cols; $inx++)
{
    $name   = $lines[ $inx + (0 * $cols) ];
    $latin  = $lines[ $inx + (1 * $cols) ];
    $class  = $lines[ $inx + (2 * $cols) ];
    $clalat = $lines[ $inx + (3 * $cols) ];

    $nameparts = explode(", ", $name);

    if (count($nameparts) == 2)
    {
        if (substr($nameparts[ 1 ], -1) == "-")
        {
            $name = $nameparts[ 1 ] . $nameparts[ 0 ];
        }
        else
        {
            $name = $nameparts[ 1 ] . " " . $nameparts[ 0 ];
        }
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

    $data = array();
    $data[] = $name;
    $data[] = $latin;
    $data[] = $class;
    $data[] = $clalat;

    if (($wikiname == str_replace(" ", "_", $name)) ||
        ($wikiname == urlencode(str_replace(" ", "_", $name))))
    {
        $data[] = 1;
    }
    else
    {
        $data[] = $wikiname;
    }

    $array[] = $data;
}

file_put_contents("pflanzen.json", json_encdat($array));

?>