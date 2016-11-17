<?php

include("../include/json.php");

$GLOBALS[ "fails" ] = array();

function mailResults()
{
	$to = "dezi@kappa-mm.de";
	$from = "Icon Checker<www.xavaro.de@kappa-mm.de>";
	$subject = "Icon - Check => " . count($GLOBALS[ "fails" ]);
	$message = "";
	
	for ($inx = 0; $inx < count($GLOBALS[ "fails" ]); $inx++)
	{
		$error = $GLOBALS[ "fails" ][ $inx ];
		
		$line = $error[ "file" ] . " => " . 
			    $error[ "keytag" ] . " => " . 
			    $error[ "icon" ] . "\n";
		
		$message .= $line;
	}
	
	$pfd = popen("sendmail $to", "w");
	
	fputs($pfd, "Subject: " . $subject . "\n");
	fputs($pfd, "From: " . $from . "\n");
	fputs($pfd, $message);
	
	pclose($pfd);
}

function checkWebLibsIOC($file)
{
	echo "CHECK: $file\n";
	
	$data = json_decdat(json_decomment(file_get_contents($file)));
	
	if ($data == null)
	{
		echo "CORRUPT: $file\n";
		
		return;
	}
	
	foreach ($data as $catkey => $categories)
	{
		if ($catkey == "locales") continue;
		
		foreach ($categories as $domainkey => $domain)
		{
			if (isset($domain[ "icon" ]))
			{
				checkIcon($file, $domainkey, $domain[ "icon" ]);
			}	
		}
	}
}

function checkWebLibsIPxx($file)
{
	echo "CHECK: $file\n";
	
	$data = json_decdat(json_decomment(file_get_contents($file)));
	
	if ($data == null)
	{
		echo "CORRUPT: $file\n";
		
		return;
	}
	
	foreach ($data as $keytag => $content)
	{
		if ($keytag == "locales") continue;
		
		if (isset($content[ "icon" ]))
		{
			checkIcon($file, $keytag, $content[ "icon" ]);
		}
		
		if (isset($content[ "channels" ]))
		{
			$channels = $content[ "channels" ];
			
			foreach ($channels as $index => $channel)
			{
				if (isset($channel[ "icon" ]))
				{
					$channeltag = $keytag . "/" . $channel[ "label" ];
				
					checkIcon($file, $channeltag, $channel[ "icon" ]);
				}
			}
		}
	}
}

function checkIcon($file, $keytag, $icon)
{
	$cont = file_get_contents($icon);
	$result = ($cont == null) ? "FAIL" : "OK";
	
	if ($result == "FAIL")
	{
		$error = array();
		$error[ "file"   ] = basename($file);
		$error[ "keytag" ] = $keytag;
		$error[ "icon"   ] = $icon;
		
		$GLOBALS[ "fails" ][] = $error;
	}
	
	echo "CHECK: $result => $keytag => $icon\n";
}

function checkWebLibs()
{
	$path = "../../asp/SafeHome/app/weblibs";
	
	$dfd = opendir($path);
	
	while (($entry = readdir($dfd)) !== false)
	{
		if ($entry == ".") continue;
		if ($entry == "..") continue;

		if (substr($entry, -5) != ".json") continue;
				
		if (substr($entry, 0, 3) == "ioc")
		{
			checkWebLibsIOC($path . "/" . $entry);
		}

		if ((substr($entry, 0, 4) == "iptv") ||
			(substr($entry, 0, 4) == "iprd"))
		{
			checkWebLibsIPxx($path . "/" . $entry);
		}
	}

	closedir($dfd);
}

checkWebLibs();
mailResults();

?>