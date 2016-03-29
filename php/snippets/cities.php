<?php

include("/Users/dezi/xavaro/php/include/json.php");

$stuff = json_decdat(file_get_contents("lists.de-rDE.json"));

$names = $stuff[ "lists.cities.capitals.wikipedia" ];

$array = array();

foreach ($names as $inx => $data)
{
    $found = false;

    $name = $data[ 0 ];

    $wikiname = urlencode(str_replace(" ", "_", $name));
    $wikiurl = "https://de.wikipedia.org/wiki/" . $wikiname;

    if (($found === false) && @file_get_contents($wikiurl))
    {
        $found = true;
    }

    if ($found === false)
    {
        echo "ER: $name => $wikiname\n";
    }
    else
    {
        echo "OK: $name => $wikiname\n";
    }

    if ($found === false)
    {
        $data[] = 0;
    }
    else
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

file_put_contents("xxxx.json",json_encdat($array));

?>
