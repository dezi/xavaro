<?php

$GLOBALS[ "debug" ] = false;
$GLOBALS[ "maxlen" ] = 240;

function enc2java($lines)
{
	$numlines = count($lines);
	
	$subname = null;
	$depends = null;
	$register = null;
	$usedvars = null;
	$secondpass = false;
	$both = true;
	
	for ($inx = 0; $inx < $numlines; $inx++)
	{
		$line = trim($lines[ $inx ]);
		
		if (substr($line, 0, 19) == "unsigned char* sub_")
		{
			$substart = $inx;
			
			$subname = $line;
			$subname = str_replace("unsigned char*", "int[] ", $subname);
			$subname = str_replace("unsigned char*", "int[] ", $subname);
			$subname = str_replace("\t", " ", $subname);
			$subname = str_replace("   ", " ", $subname);
			$subname = str_replace("  ", " ", $subname);
			
			if ($secondpass) $subname = str_replace("sub_", "subopt_", $subname);
			
			$depends = array();
			$register = array();
			
			if (! $secondpass) $usedvars = array();
			
			if ($secondpass || $both) echo "$subname\n";
			continue;
		}
		
		if ($subname)
		{
			if ($line == "")
			{
				if ($secondpass || $both) echo "\n";
				continue;	
			}
						
			if ($line == "{")
			{
				if ($secondpass || $both) echo "{\n";
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
				
				if ($secondpass || $both) echo "}\n\n";

				if ($secondpass)
				{
					$secondpass = false;
					$usedvars = null;
				}
				else
				{
					$inx = $substart - 1;
					$secondpass = true;
					exit(0);
				}
				
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
			// Divide index values by 4, changing from bytes to int arrays.
			//
			
			$indices = explode("[",$line);
			
			for ($fnz = 1; $fnz < count($indices); $fnz++)
			{
				$index = explode("]",$indices[ $fnz ]);
				
				$index[ 0 ] = "" . (intval($index[ 0 ]) / 4);
				
				$indices[ $fnz ] = implode("]", $index);
			}
			
			$line = implode("[", $indices);
			
			$line = registerVariable($line, $depends, $register, $usedvars, $secondpass);

			if (($secondpass || $both) && ($line != null)) echo "\t$line\n";
		}
	}
}

function registerUsage($expr, &$register, &$usedvars)
{
	if (preg_match_all("/v[0-9]+/", $expr, $matches))
	{
		$matches = $matches[ 0 ];
		
		for ($inx = 0; $inx < count($matches); $inx++)
		{
			$name = $matches[ $inx ];
			
			$usedvars[ $name ] = true;

			if (! isset($register[ $name ]))
			{
				echo "registerUsage: fucked up at $name\n";
				var_dump($register);
				exit(0);
			}
			else
			{
				registerUsage($register[ $name ], $register, $usedvars);
			}
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

function registerVariable($line, &$depends, &$register, &$usedvars, $secondpass)
{
	$parts = explode(" = ", $line);
	
	if (count($parts) == 2)
	{
		$name = $parts[ 0 ];
		$expr = $parts[ 1 ];
		
		if (isset($register[ $name ]))
		{
			if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Duplicate assign: $name\n";
		}
		
		$expr = str_replace(";", "", $expr);
		
		$register[ $name ] = $expr;

		$depvals = reduceExpression($expr);
		
		if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Dependencies: $depvals\n";

		if (substr($name, 0, 1) == "v")
		{
			if ($secondpass && ! isset($usedvars[ $name ])) 
			{
				//
				// Variable is obsolete.
				//
				
				if ($GLOBALS[ "debug" ]) echo "\t// Obsolete: $name\n";
				
				return null;
			}
			
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
				
				if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Depends: $deppart\n";

				if (! isset($depends[ $deppart ])) $depends[ $deppart ] = array();
			
				$depends[ $deppart ][] = $name;
			}
			
			$assign = resolveExpression($name, $expr, $register, $secondpass);

			if ($secondpass)
			{
				$register[ $name ] = $assign;
				$line = "int " . $name . " = " . $assign . ";";
			}
			else
			{
				$register[ $name ] = $expr;
				$line = "int " . $name . " = " . $expr . ";";
			}	
		}
		else
		{
			//
			// Assignment to a2, a3 or result. Invalidate
			// all v variables which depend on this.
			//
		
			if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Assign: $name = " . (isset($depends[ $name ]) ? "used" : "unused") . "\n";
			
			$assign = resolveExpression($name, $expr, $register, $secondpass);
			
			if ($secondpass)
			{
				$register[ $name ] = $assign;
				$line = $name . " = " . $assign . ";";
			}
			else
			{
				$register[ $name ] = $expr;
				$line = $name . " = " . $expr . ";";
			}
			
			if (isset($depends[ $name ]))
			{
				$depparts = $depends[ $name ];
			
				for ($inx = 0; $inx < count($depparts); $inx++)
				{
					$variable = $depparts[ $inx ];
					$register[ $variable ] .= "@";
				
					if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Invalidate: $name => $variable\n";
				}
			}
			
			if (! $secondpass) registerUsage($assign, $register, $usedvars);
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
		if ($expr[ $inx ] == "~") return false;
	}
	
	return true;
}

function isSynonymExpression($expr)
{
	for ($inx = 0; $inx < strlen($expr); $inx++)
	{
		if ($expr[ $inx ] == "^") return false;
		if ($expr[ $inx ] == "&") return false;
		if ($expr[ $inx ] == "|") return false;
		if ($expr[ $inx ] == "~") return false;
		if ($expr[ $inx ] == "[") return false;
		if ($expr[ $inx ] == "]") return false;
	}
	
	return true;
}

function resolveExpression($name, $expr, &$register, $secondpass)
{
	$assign = $expr;
	
	while (strlen($assign) < $GLOBALS[ "maxlen" ])
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
						$replacement = $register[ $variable ];
						
						if (substr($replacement, -1) == "@")
						{
							//
							// Underlying array entries have been modified. Variable
							// cannot be replaced.
							//
						}
						else
						{
							if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Assignment valid: $name => $variable\n";
										
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

							if ((strlen($replacement) < ($GLOBALS[ "maxlen" ] / 2)) 
									&& (strlen($temp) <= $GLOBALS[ "maxlen" ]))
							{
								$assign = $temp;
								$didone = true;
							}
						}
					}
					else
					{
						if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Assignment cleared: $name => $variable\n";
					}
				}
			}
			
			if ($didone) break;
		}
		
		if (! $didone) break;
	}
	
	if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Assignment: $name => $assign\n";

	return $assign;
}

enc2java(file("./pgoencrypt/src/encrypt.c"));
?>