<?php

include "../include/json.php";

$GLOBALS[ "types" ] = array();

$GLOBALS[ "types" ][ "bytes"   ] = true;
$GLOBALS[ "types" ][ "string"  ] = true;

$GLOBALS[ "types" ][ "float"   ] = true;
$GLOBALS[ "types" ][ "double"  ] = true;
$GLOBALS[ "types" ][ "fixed32" ] = true;
$GLOBALS[ "types" ][ "fixed64" ] = true;

$GLOBALS[ "types" ][ "int32"   ] = true;
$GLOBALS[ "types" ][ "int64"   ] = true;

$GLOBALS[ "types" ][ "bool"    ] = true;
$GLOBALS[ "types" ][ "uint32"  ] = true;
$GLOBALS[ "types" ][ "uint64"  ] = true;

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

function parse($protofile)
{
	$content = file_get_contents($protofile);
	
	$GLOBALS[ "lines" ] = explode("\n", $content);
	$GLOBALS[ "lipos" ] = 0;
	
	$GLOBALS[ "data" ] = array();

 	parseItems("", "M");
 	
	return $GLOBALS[ "data" ];
}

function parseItems($prefix, $toptype)
{
	$data = array();
	
	$type = null;
	$path = null;
	
	while ($GLOBALS[ "lipos" ] < count($GLOBALS[ "lines" ]))
	{
		$line = trim($GLOBALS[ "lines" ][ $GLOBALS[ "lipos" ]++ ]);
		
		if (trim($line) == "") continue;
		if (substr(trim($line), 0, 2) == "//") continue;
			
		if (substr($line, 0, 8) == "message ")
		{
			$type = "M";
			$path = $prefix . "." . trim(substr($line, 8));
			//echo "$type=$path\n";
			continue;
		}
		
		if (substr($line, 0, 5) == "enum ")
		{
			$type = "E";
			$path = $prefix . "." . trim(substr($line, 5));
			//echo "$type=$path\n"; 
			continue;
		}
		
		if ($line == "{")
		{
			$subdata = parseItems($path, $type);
			
			$GLOBALS[ "data" ][ $path ] = $subdata;
			
			continue;
		}
		
		if ($line == "}")
		{
			return $data;	
			continue;
		}
		
		if ($toptype == "E")
		{
			$parts = explode(";", $line);
			$line = $parts[ 0 ];
			$parts = explode("=", $line);
			
			$data[ trim($parts[ 0 ]) ] = intval(trim($parts[ 1 ]), 10);
			
			continue;
		}
		
		if ($toptype == "M")
		{
			$item = array();
			
			$packed = false;
			$repeated = false;
			
			if (substr($line, 0, 9) == "repeated ")
			{
				$repeated = true;
				$line = trim(substr($line, 9));
			}
			
			if (substr($line, -15) == " [packed=true];")
			{
				$packed = true;
				$line = trim(substr($line, 0, -15)) . ";";
			}
			
			$parts = explode(";", $line);
			$line = $parts[ 0 ];
			$parts = explode("=", $line);
			
			if (count($parts) != 2)
			{
				echo "==========>$line<<<\n";
				continue;
			}
			
			$idid = intval(trim($parts[ 1 ]), 10);
			$parts = explode(" ", trim($parts[ 0 ]));
			$ttyp = trim($parts[ 0 ]);
			$name = trim($parts[ 1 ]);
			
			//
			// Check type....
			//
			
			if ((substr($ttyp, 0, 1) != ".") && ! isset($GLOBALS[ "types" ][ $ttyp ]))
			{
				if (substr($ttyp, 0, 11) == "POGOProtos.")
				{
					$ttyp = "." . $ttyp;
				}
				else
				{
					//
					// Fixed glitch in original repo.
					//
					
					if ($ttyp == "InventoryUpgrade")
					{
						$ttyp = ".POGOProtos.Inventory.InventoryUpgrade";
					}
					else
					{
						$ttyp = $prefix . "." . $ttyp;
					}
					
					echo "$ttyp=>$line\n";
				}
			}
			
			$item[ "id" ] = $idid;
			$item[ "type" ] = $ttyp;
			
			if ($packed) $item[ "packed" ] = true;
			if ($repeated) $item[ "repeated" ] = true;
			
			$data[ $name ] = $item;
			
			continue;
		}
	}
	
	return $data;
}

function checkLinks()
{
	$links = array();
	
	foreach ($GLOBALS[ "data" ] as $path => $data)
	{
		$links[ $path ] = 0;
	}
	
	foreach ($GLOBALS[ "data" ] as $path => $data)
	{
		foreach ($data as $name => $value)
		{
			if (is_array($value) && isset($value[ "type" ]))
			{
				$ref = $value[ "type" ];
				
				if (! isset($links[ $ref ])) $links[ $ref ] = 0;
				
				$links[ $ref ]++;
			}
		}
	}
	
	file_put_contents("./pogo_protos_refs.json", json_encdat($links) . "\n");
}

function buildjava()
{
	$java = "";
	
	$java .= "package de.xavaro.android.common;\n";
	$java .= "\n";
	$java .= "import org.json.JSONObject;\n";
	$java .= "\n";
	$java .= "public class PokemonProto\n";
	$java .= "{\n";
	$java .= "\tpublic static JSONObject getProtos()\n";
	$java .= "\t{\n";
	$java .= "\t\tJSONObject json = new JSONObject();\n";
	$java .= "\n";
	$java .= "\t\ttry\n";
	$java .= "\t\t{\n";

	foreach ($GLOBALS[ "data" ] as $path => $data)
	{
		$json = json_encode($data, JSON_UNESCAPED_SLASHES +  JSON_UNESCAPED_UNICODE);
		$json = str_replace("\"", "'", $json);

		if ($json == "[]") $json = "{}";
		
		$java .= "\t\t\tjson.put(\"$path\",new JSONObject(\"$json\"));\n";
	}
	
	$java .= "\t\t}\n";
	$java .= "\t\tcatch (Exception ex)\n";
	$java .= "\t\t{\n";
	$java .= "\t\t\tex.printStackTrace();\n";
	$java .= "\t\t}\n";
	$java .= "\n";
	$java .= "\t\treturn json;\n";
	$java .= "\t}\n";
	$java .= "}\n";

	file_put_contents("./PokemonProto.java", $java . "\n");
}

$fd = fopen("./pogo_protos.proto","w");
recurse($fd, "./POGOProtos");
fclose($fd);

$data = parse("./pogo_protos.proto");

file_put_contents("./pogo_protos.json", json_encdat($data) . "\n");

checkLinks();

buildjava();
























?>