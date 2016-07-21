<?php

function recurse($fd, $dir)
{
	$dfd = opendir($dir);
	
	if ($dfd)
	{
		while (($file = readdir($dfd)) != false)
		{
			if (($file == ".") || ($file == "..")) continue;
		
			$subdir = $dir . "/" . $file;
			
			echo "$subdir\n";
			
			if (is_dir($subdir))
			{
				recurse($fd, $subdir);
				continue;
			}
			
			if (substr($file, -6) == ".proto")
			{
				$proto = trim(file_get_contents($subdir));
				
				$proto = str_replace("\r", "\n", $proto);
				$proto = str_replace("    ", "\t", $proto);
				
				if (substr($proto, 0, 18) == 'syntax = "proto3";')
				{
					$proto = trim(substr($proto, 18));
				}
				
				if (substr($proto, 3, 18) == 'syntax = "proto3";')
				{
					$proto = trim(substr($proto, 21));
				}
				
				$package = null;
				
				$lines = explode("\n", $proto);
				
				for ($inx = 0; $inx < count($lines); $inx++)
				{
					if (substr($lines[ $inx ], 0, 7) == "import ")
					{
						$lines[ $inx ] = "";
					} 
					
					if (substr($lines[ $inx ], 0, 8) == "package ")
					{
						$package = trim(substr($lines[ $inx ], 8, -1));
						
						$lines[ $inx ] = "";
					} 
					
					if ((substr($lines[ $inx ], 0, 8) == "message ") && $package)
					{				
						$lines[ $inx ] 
							= substr($lines[ $inx ], 0, 8)
							. $package
							. "."
							. substr($lines[ $inx ], 8);
					} 
					
					if ((substr($lines[ $inx ], 0, 5) == "enum ") && $package)
					{				
						$lines[ $inx ] 
							= substr($lines[ $inx ], 0, 5)
							. $package
							. "."
							. substr($lines[ $inx ], 5);
					}
					
					if (substr($lines[ $inx ], 0, 2) == "\t\t")
					{
						$lines[ $inx ] = str_replace(" {", "\n\t\t{", $lines[ $inx ]);
					}
					else
					if (substr($lines[ $inx ], 0, 1) == "\t")
					{
						$lines[ $inx ] = str_replace(" {", "\n\t{", $lines[ $inx ]);
					}
					else
					{
						$lines[ $inx ] = str_replace(" {", "\n{", $lines[ $inx ]);
					}
				}
				
				$proto = trim(implode("\n", $lines));
				
				$proto = str_replace("\n\n\n\n\n", "\n\n", $proto);
				$proto = str_replace("\n\n\n\n", "\n\n", $proto);
				$proto = str_replace("\n\n\n", "\n\n", $proto);
				$proto = str_replace("\n\n\n", "\n\n", $proto);
				$proto = str_replace("\n\n\n", "\n\n", $proto);
				$proto = str_replace("\n\n\n", "\n\n", $proto);
				
				fwrite($fd, $proto);
				fwrite($fd, "\n\n");
			}
		}
		
		closedir($dfd);
	}
}

$fd = fopen("./POGOProtos.proto","w");

recurse($fd, "./POGOProtos");

fclose($fd);


?>