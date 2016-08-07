<?php

function enc2java($lines)
{
	$numlines = count($lines);
	
	$subname = null;
	$depends = null;
	$register = null;
	
	for ($inx = 0; $inx < $numlines; $inx++)
	{
		$line = trim($lines[ $inx ]);
		
		if (substr($line, 0, 19) == "unsigned char* sub_")
		{
			$subname = $line;
			$subname = str_replace("unsigned char*", "int[] ", $subname);
			$subname = str_replace("unsigned char*", "int[] ", $subname);
			$subname = str_replace("\t", " ", $subname);
			$subname = str_replace("   ", " ", $subname);
			$subname = str_replace("  ", " ", $subname);
			
			$depends = array();
			$register = array();
			
			echo "$subname\n";
			continue;
		}
		
		if ($subname)
		{
			if ($line == "")
			{
				echo "\n";
				continue;	
			}
						
			if ($line == "{")
			{
				echo "{\n";
				continue;
			}

			if ($line == "}")
			{
				$subname = null;
				$depends = null;
				$register = null;
				
				echo "}\n\n";
				continue;
			}
			
			$line = str_replace("(_DWORD *)a2", "(_DWORD *)(a2 + 0)", $line);
			$line = str_replace("(_DWORD *)a3", "(_DWORD *)(a3 + 0)", $line);
			$line = str_replace("(_DWORD *)result", "(_DWORD *)(result + 0)", $line);
			
			$line = str_replace("*(_DWORD *)", "", $line);
			
			$line = preg_replace ("/\(a2 \+ ([0-9]+)\)/",  "a2[ \\1 ]" , $line);
			$line = preg_replace ("/\(a3 \+ ([0-9]+)\)/",  "a3[ \\1 ]" , $line);
			$line = preg_replace ("/\(result \+ ([0-9]+)\)/",  "result[ \\1 ]" , $line);
			
			$line = str_replace("[  ", "[", $line);
			$line = str_replace("[ ", "[", $line);
			$line = str_replace("  ]", "]", $line);
			$line = str_replace(" ]", "]", $line);

			//
			// Divide index values by 4, moving from bytes to int arrays.
			//
			
			$indices = explode("[ ",$line);
			
			for ($fnz = 1; $fnz < count($indices); $fnz++)
			{
				$index = explode(" ",$indices[ $fnz ]);
				
				$index[ 0 ] = "" . (intval($index[ 0 ]) / 4);
				
				$indices[ $fnz ] = implode(" ", $index);
			}
			
			$line = implode("[ ", $indices);
			
			$line = registerVariable($line, $depends, $register);
			
			echo "\t$line\n";
		}
	}
}

function registerVariable($line, &$depends, &$register)
{
	$parts = explode(" = ", $line);
	
	if (count($parts) == 2)
	{
		$name = $parts[ 0 ];
		$expr = $parts[ 1 ];
		
		if (isset($register[ $name ]))
		{
			echo "\t// Duplicate assign: $name\n";
		}
				
		$register[ $name ] = $expr;

		$depvals = $expr;
		
		$depvals = str_replace("^",":", $depvals);
		$depvals = str_replace("~",":", $depvals);
		$depvals = str_replace("&",":", $depvals);
		$depvals = str_replace("|",":", $depvals);
		
		$depvals = str_replace(";","", $depvals);
		$depvals = str_replace(" ","", $depvals);
		$depvals = str_replace("(","", $depvals);
		$depvals = str_replace(")","", $depvals);

		$depvals = str_replace(":::::",":", $depvals);
		$depvals = str_replace("::::",":", $depvals);
		$depvals = str_replace(":::",":", $depvals);
		$depvals = str_replace("::",":", $depvals);
		
		echo "\t// Dependencies: $depvals\n";

		if (substr($name, 0, 1) == "v")
		{
			//
			// Register dependencies for variables v
			//
			
			$depparts = explode(":", $depvals);
		
			for ($inx = 0; $inx < count($depparts); $inx++)
			{
				$deppart = $depparts[ $inx ];
			
				if ($deppart == "") continue;
				if (substr($deppart, 0, 1) == "v") continue;
				
				//
				// $deppart is a2, a3 or result
				// 
				
				echo "\t// Depends: $deppart\n";

				if (! isset($depends[ $deppart ])) $depends[ $deppart ] = array();
			
				$depends[ $deppart ][] = $name;
			}
		}
		else
		{
			//
			// Assignment to a2, a3 or result. Invalidate
			// all v variables which depend on this.
			//
		
			echo "\t// Assign: $name = " . (isset($depends[ $name ]) ? "used" : "unused") . "\n";

			if (isset($depends[ $name ]))
			{
				$depparts = $depends[ $name ];
				
				for ($inx = 0; $inx < count($depparts); $inx++)
				{
					$variable = $depparts[ $inx ];
					echo "\t// Invalidate: $name => $variable\n";
				}
			}
		}
	}
	
	return $line;
}

enc2java(file("./pgoencrypt/src/encrypt.c"));
?>