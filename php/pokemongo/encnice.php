<?php

$GLOBALS[ "debug" ] = false;
$GLOBALS[ "maxlen" ] = 120;

function enc2java($lines)
{
	$numlines = count($lines);
	
	$subname = null;
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
				$register = null;
				
				if ($secondpass || $both) echo "}\n\n";

				if ($secondpass)
				{
					$secondpass = false;
					$usedvars = null;
					exit(0);
				}
				else
				{
					$inx = $substart - 1;
					$secondpass = true;
				}
				
				continue;
			}
			
			$line = str_replace("(_DWORD *)a2", "(_DWORD *)(a2 + 0)", $line);
			$line = str_replace("(_DWORD *)a3", "(_DWORD *)(a3 + 0)", $line);
			$line = str_replace("(_DWORD *)result", "(_DWORD *)(result + 0)", $line);
			
			$line = str_replace("*(_DWORD *)", "", $line);
			
			$line = preg_replace("/\(a2 \+ ([0-9]+)\)/",  "a2[ \\1 ]" , $line);
			$line = preg_replace("/\(a3 \+ ([0-9]+)\)/",  "a3[ \\1 ]" , $line);
			$line = preg_replace("/\(result \+ ([0-9]+)\)/",  "result[ \\1 ]" , $line);
			
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
			
			$line = registerVariable($line, $register, $usedvars, $secondpass);

			if (($secondpass || $both) && ($line != null)) echo "\t$line\n";
		}
	}
}

function registerUsage($assignvar, $expr, &$register, &$usedvars)
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
				registerUsage($name, $register[ $name ], $register, $usedvars);
			}
		}
	}
}

function registerVariable($line, &$register, &$usedvars, $secondpass)
{
	$parts = explode(" = ", $line);
	
	if (count($parts) == 2)
	{
		$name = $parts[ 0 ];
		$expr = str_replace(";", "", $parts[ 1 ]);
		
		$register[ $name ] = $expr;

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
			
			$assign = resolveExpression($name, $expr, $register, $secondpass);

			if ($secondpass)
			{
				$register[ $name ] = $assign;
				$line = "int " . $name . " = " . $assign . ";";
			}
			else
			{
				$register[ $name ] = $assign;
				$line = "int " . $name . " = " . $expr . ";";
			}	
		}
		else
		{
			//
			// Assignment to a2, a3 or result.
			//
		
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
			
			if (! $secondpass) 
			{
				registerUsage($name, $assign, $register, $usedvars);
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
		if ($expr[ $inx ] == "~") return false;
	}
	
	return true;
}

function isVaronlyExpression($expr)
{
	for ($inx = 0; $inx < strlen($expr); $inx++)
	{
		if ($expr[ $inx ] == "[") return false;
		if ($expr[ $inx ] == "]") return false;
	}
	
	return true;
}

function isStaticExpression($expr)
{
	return (strpos($expr, "result") === false);
}

function resolveExpression($name, $expr, &$register, $secondpass)
{
	$assign = $expr;
	
	while (true)
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
						
						if (isStaticExpression($replacement))
						{
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

							if ((strlen($temp) <= $GLOBALS[ "maxlen" ]) || (strlen($temp) == strlen($assign)))
							{
								if ($secondpass && $GLOBALS[ "debug" ]) echo "\t// Replace: $variable => $replacement\n";
							
								$assign = $temp;
								$didone = true;
							}
						}
					}
					else
					{
						echo "resolveExpression: fucked up at $variable\n";
						var_dump($register);
						exit(0);
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