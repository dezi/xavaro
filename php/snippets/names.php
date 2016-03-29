<?php

include("/Users/dezi/xavaro/php/include/json.php");

$stuff = json_decdat(file_get_contents("lists.de-rDE.json"));

$names = $stuff[ "lists.names.male.top500" ];

$array = array();

foreach ($names as $inx => $name)
{
    $found = false;

    $name1 = explode(" / ", $name);
    $name1 = $name1[ 0 ];

    $wikiname = urlencode(str_replace(" ", "_", $name1));
    $wikiurl = "https://de.wikipedia.org/wiki/" . $wikiname;

    if (($found === false) && @file_get_contents($wikiurl))
    {
        $found = true;
    }

    if ($found === false)
    {
        $wikiname = urlencode(str_replace(" ", "_", $name1)) . "_(Vorname)";
        $wikiurl = "https://de.wikipedia.org/wiki/" . $wikiname;

        if (@file_get_contents($wikiurl)) $found = true;
    }

    if ($found === false)
    {
        echo "ER: $name => $wikiname\n";
    }
    else
    {
        echo "OK: $name => $wikiname\n";
    }

    $data = array();

    $data[] = $inx + 1;
    $data[] = $name;

    if ($found === false)
    {
        $data[] = 0;
    }
    else
    if (($wikiname == str_replace(" ", "_", $name1)) ||
        ($wikiname == urlencode(str_replace(" ", "_", $name1))))
    {
        $data[] = 1;
    }
    else
    {
        $data[] = $wikiname;
    }

    $array[] = $data;
}

file_put_contents("name.json",json_encdat($array));

?>
