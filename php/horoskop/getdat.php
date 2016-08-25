<?php

function getSSLPage($url) 
{
	$curl = "curl -H \"Referer: $url\" -A \"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9\" -s $url";
	
	$handle = popen($curl, "r");
	
	$read = "";
	
	while ($chunk = fread($handle, 8192))
	{
		$read .= $chunk;
	}	

	pclose($handle);
	return $read;
}

function getSite1Normal()
{
	for ($month = 1; $month <= 12; $month++)
	{
		$mpad = str_pad($month, 2, "0", STR_PAD_LEFT);
		
		for ($day = 1; $day <= 31; $day++)
		{
			$dpad = str_pad($day, 2, "0", STR_PAD_LEFT);

			$url = "https://www.astroportal.com/tageshoroskope/zwillinge/" . $dpad . $mpad . "/";
			
			$cont = getSSLPage($url);
			
			if (! strpos($cont, "hbar career v")) $cont = gzinflate(substr($cont, 10));
			
			$tag = "love";
			$parts = explode("hbar $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			echo "$tag: $score => $text\n";

			$tag = "career";
			$parts = explode("hbar $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			echo "$tag: $score => $text\n";
					
			$tag = "health";
			$parts = explode("hbar $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			echo "$tag: $score => $text\n";
		}
	}
}

function getSite1Money()
{
	for ($month = 1; $month <= 12; $month++)
	{
		$mpad = str_pad($month, 2, "0", STR_PAD_LEFT);
		
		for ($day = 1; $day <= 31; $day++)
		{
			$dpad = str_pad($day, 2, "0", STR_PAD_LEFT);

			$url = "https://www.astroportal.com/finanzhoroskope/zwillinge/" . $dpad . $mpad . "/";
			
			$cont = getSSLPage($url);
			
			if (! strpos($cont, "hbar career v")) $cont = gzinflate(substr($cont, 10));
			
			$tag = "career";
			$parts = explode("hbar $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			echo "$tag: $score => $text\n";
					
			$tag = "gamble";
			$parts = explode("hbar $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			echo "$tag: $score => $text\n";
					
			$tag = "credit";
			$parts = explode("hbar $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			echo "$tag: $score => $text\n";
		}
	}
}

function getSite1Partner()
{
	$list = array();
	$list[] = "widder";
	$list[] = "stier";
	$list[] = "zwillinge";
	$list[] = "krebs";
	$list[] = "loewe";
	$list[] = "jungfrau";
	$list[] = "waage";
	$list[] = "skorpion";
	$list[] = "schuetze";
	$list[] = "steinbock";
	$list[] = "wassermann";
	$list[] = "fische";
	
	$real = array();
	$real[] = "Widder";
	$real[] = "Stier";
	$real[] = "Zwillinge";
	$real[] = "Krebs";
	$real[] = "Löwe";
	$real[] = "Jungfrau";
	$real[] = "Waage";
	$real[] = "Skorpion";
	$real[] = "Schütze";
	$real[] = "Steinbock";
	$real[] = "Wassermann";
	$real[] = "Fische";

	for ($p1 = 0; $p1 < 12; $p1++)
	{
		for ($p2 = 0; $p2 < 12; $p2++)
		{
			$url = "https://www.astroportal.com/partnerhoroskope/partnerhoroskop-heute/" . $list[ $p1 ] . "/"  . $list[ $p2 ] . "/";

			$cont = getSSLPage($url);
			
			if (! strpos($cont, "hbar monthly love v")) $cont = gzinflate(substr($cont, 10));
			
			$tag = "love";
			$parts = explode("hbar monthly $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			$text = str_replace($real[ $p1 ], "@1", $text);
			$text = str_replace($real[ $p2 ], "@2", $text);
			echo "$tag: $score => $text\n";
										
			$tag = "boss";
			$parts = explode("hbar monthly $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			$text = str_replace($real[ $p1 ], "@1", $text);
			$text = str_replace($real[ $p2 ], "@2", $text);
			echo "$tag: $score => $text\n";
										
			$tag = "friends";
			$parts = explode("hbar monthly $tag v", $cont);
			if (count($parts) != 2) continue;
			$score = intval($parts[ 1 ]);
			if (! $score) continue;
			$parts = explode("<p>", $parts[ 1 ]);
			$parts = explode("</p>", $parts[ 1 ]);
			$text = $parts[ 0 ];
			$text = str_replace($real[ $p1 ], "@1", $text);
			$text = str_replace($real[ $p2 ], "@2", $text);
			echo "$tag: $score => $text\n";			
		}
	}
}

getSite1Partner();

?>