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

			if (substr($line, 0, 4) == "int ")
			{
				continue;
			}

			if ($line == "}")
			{
				$subname = null;
				$depends = null;
				$register = null;
				
				echo "}\n\n";
				exit(0);
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

function reduceExpression($expr)
{
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
	
	return $depvals;
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
		
		$expr = str_replace(";", "", $expr);
		
		$register[ $name ] = $expr;

		$depvals = reduceExpression($expr);
		
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
			
			$assign = resolveExpression($name, $expr, $register);
			$register[ $name ] = $assign;
			$line = "int " . $name . " = " . $assign . ";";
		}
		else
		{
			//
			// Assignment to a2, a3 or result. Invalidate
			// all v variables which depend on this.
			//
		
			echo "\t// Assign: $name = " . (isset($depends[ $name ]) ? "used" : "unused") . "\n";
			
			$assign = resolveExpression($name, $expr, $register);
			
			if (strlen($assign) < 80)
			{
				$register[ $name ] = $assign;
				$line = $name . " = " . $assign . ";";
			}
			
			if (isset($depends[ $name ]))
			{
				$depparts = $depends[ $name ];
			
				for ($inx = 0; $inx < count($depparts); $inx++)
				{
					$variable = $depparts[ $inx ];
					unset($register[ $variable ]);
				
					echo "\t// Invalidate: $name => $variable\n";
				}
			
			}
		}
	}
	
	return $line;
}

function isSimpleExpression($expr)
{
	for ($inx = 0; $inx < strlen($expr); $inx++)
	{
		if ($expr[ $inx ] == "^") return false;
		if ($expr[ $inx ] == "&") return false;
		if ($expr[ $inx ] == "|") return false;
	}
	
	return true;
}

function resolveExpression($name, $expr, &$register)
{
	$assign = $expr;
	
	while (strlen($assign) < 60)
	{
		$didone = false;
		
		for ($inx = 0; $inx < strlen($assign); $inx++)
		{
			if ($assign[ $inx ] == "v")
			{
				$dest = "";
				
				for ($fnz = $inx + 1; $fnz < strlen($assign); $fnz++)
				{
					if (("0" <= $assign[ $fnz ]) && ($assign[ $fnz ] <= "9"))
					{
						$dest .= $assign[ $fnz ];
					}
					else
					{	
						break;
					}
				}
				
				if (strlen($dest) > 0)
				{
					$variable = "v" . $dest;
					
					if (isset($register[ $variable ]))
					{
						echo "\t// Assignment valid: $name => $variable\n";
					
						$replacement = $register[ $variable ];
					
						if (isSimpleExpression($replacement))
						{
							$temp = substr($assign, 0, $inx)
									. $replacement					
									. substr($assign, $fnz);
						}
						else
						{
							$temp = substr($assign, 0, $inx)
									. "(" . $replacement . ")"						
									. substr($assign, $fnz);
						}	
						
						if (strlen($temp) <= 60)
						{
							$assign = $temp;
							$didone = true;
						}						
					}
					else
					{
						echo "\t// Assignment cleared: $name => $variable\n";
					}
				}
			}
			
			if ($didone) break;
		}
		
		if (! $didone) break;
	}
	
	echo "\t// Assignment: $name => $assign\n";

	return $assign;
}

enc2java(file("./pgoencrypt/src/encrypt.c"));
?>