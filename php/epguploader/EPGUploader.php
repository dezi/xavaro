<?php

//
// Web server part of EPG upload procedure.
//

include("../include/json.php");

header("Content-Type: text/plain");

$method = $_SERVER[ 'REQUEST_METHOD' ];

if (($method == "PUT") && isset($_SERVER[ 'HTTP_UPLOAD_FILE' ]))
{
	$upload = $_SERVER[ 'HTTP_UPLOAD_FILE' ];
	$lpath  = "/home/xavaro/var/epgdata/" . $upload;
	$ipath  = "/home/xavaro/var/pgminfo/" . substr($upload, 0, 5);
	error_log("EPGUploader: PUT $lpath");

	umask(0002);

	@mkdir(dirname($lpath), 0775, true);

	if ($putData = fopen("php://input","r"))
	{
		$gzencoded = "";
		
		while ($data = fread($putData,64 * 1024))
		{
			$gzencoded .= $data;
		}
		
		fclose($putData);

		$epgjson = gzinflate(substr($gzencoded,10,-8));
		$epgdata = json_decdat($epgjson);
		
		addProgramInfos($ipath, $epgdata);
		
		$epgjson = json_encdat($epgdata);
		$gzencoded = gzencode($epgjson, 9);

		if ($fp = fopen($lpath,"w"))
		{
			fwrite($fp, $gzencoded);
			fclose($fp);
		}
		
		//
		// Check which version will be the master.
		//
		
		if (substr($lpath, -8) == ".json.gz")
		{
			$dir  = dirname ($lpath);
			$file = basename($lpath);
		
			if (substr($file, 0, 7) == "current")
			{
				$prefix = substr($file, 0, 7);
				$symlink = "$dir/$prefix.json.gz";
				
				@unlink($symlink);
				@symlink($file, $symlink);
				
				error_log($symlink . " => " . $file);
			}
			else
			{
				$prefix = substr($file, 0, 10);
				$symlink = "$dir/$prefix.json.gz";
			
				$dfd = opendir($dir);
		
				$bestname = null;
				$bestsize = 0;
		
				while (($name = readdir($dfd)) !== false)
				{
					if (($name == ".") || ($name == "..")) continue;

					if ($name == "$prefix.json.gz") continue;
					if (substr($name, -8) != ".json.gz") continue;
					if (substr($name, 0, strlen($prefix)) != $prefix) continue;
			
					$size = filesize("$dir/$name");
				
					if ($size > $bestsize)
					{
						$bestname = $name;
						$bestsize = $size;
					}
				}
		
				closedir($dfd);
		
				if ($bestname != null)
				{
					@unlink($symlink);
					@symlink($bestname, $symlink);
					
					error_log($symlink . " => " . $bestname . " => " . $bestsize);
				}
			}
		}
	}
}

function startsWith($title, $start)
{
	return (substr($title, 0, strlen($start)) == $start);
}

function removeEnd($title, $start)
{
	return substr($title, 0, strlen($start));
}

function chopTitleName($title)
{
	if (startsWith($title, $start = "Tagesschau - Vor 20 Jahren")) return removeEnd($title, $start);
	if (startsWith($title, $start = "Sportclub live - 3. Liga")) return removeEnd($title, $start);
	
	return $title;
}

function addProgramInfos($pgminfodir, &$epgs)
{
	foreach ($epgs[ "epgdata" ] as $inx => $val)
	{
		//
		// Check if title image exists.
		//
		
		$title = $val[ "title" ];
		
		$realtitle = $title;
		$realtitle = preg_replace("/\\([^)]*\\)/", "", $realtitle);
		$realtitle = trim($realtitle);
		$realtitle = chopTitleName($realtitle);
		$realtitle = str_replace("  ", " ", $realtitle);
		$realtitle = str_replace("/", "_", $realtitle);
				
		$name = $realtitle;
		$pgminfofile = $pgminfodir . "/" . $name . ".orig.jpg";
		
		if (file_exists($pgminfofile))
		{
			$info = getimagesize($pgminfofile);
			$epgs[ "epgdata" ][ $inx ][ "imgsize" ] = $info[ 0 ] . "x" . $info[ 1 ];

			if ($realtitle != $title)
			{
				$epgs[ "epgdata" ][ $inx ][ "imgname" ] = $realtitle;
			}
		}
	}
}

?>
