<?php

include("/Users/dezi/xavaro/php/include/json.php");

$raw = file_get_contents("https://de.wikipedia.org/wiki/Liste_der_Werkzeuge");
$lines = explode("\n",$raw);

$array = array();

for ($inx = 0; $inx < count($lines); $inx++)
{
    $line = $lines[ $inx ];

    if (substr($line,0,4) != "<li>") continue;
    if (strpos($line, "Seite nicht vorhanden") > 0) continue;

    $found = false;

    if (substr($line, 0, 19) == "<li><a href=\"/wiki/")
    {
        $wiki = explode("\"", substr($line, 19));
        $wiki = $wiki[ 0 ];

        $name = explode("\">", $line);
        $name = explode("<", $name[ 1 ]);
        $name = $name[ 0 ];

        $found = true;

        echo "---$name => $wiki\n";
    }
    else
    {
        $name = explode(", siehe", substr($line, 4));
        $name = $name[ 0 ];

        $wiki = explode("href=\"/wiki/", substr($line, 4));
        $wiki = explode("\"", $wiki[ 1 ]);
        $wiki = $wiki[ 0 ];

        $siehe = explode("\">", $line);
        $siehe = explode("<", $siehe[ 1 ]);
        $siehe = $siehe[ 0 ];

        $name = $name . " (" . $siehe . ")";

        $found = true;

        echo "+++$name => $wiki\n";
    }

    $data = array();

    $data[] = $name;

    if (urlencode($name) == $wiki)
    {
        $data[] = 1;
    }
    else
    {
        $data[] = $wiki;
    }

    $array[] = $data;
}

file_put_contents("xxxx.json",json_encdat($array));

?>