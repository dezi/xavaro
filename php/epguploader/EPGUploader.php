<?php

	//
	// Web server part of EPG upload procedure.
	//
	
    header("Content-Type: text/plain");

    $method = $_SERVER[ 'REQUEST_METHOD' ];

    if (($method == "PUT") && isset($_SERVER[ 'HTTP_UPLOAD_FILE' ]))
    {
    	$upload = $_SERVER[ 'HTTP_UPLOAD_FILE' ];
    	$lpath  = "/home/xavaro/var/epgdata/" . $upload;
		error_log("EPGUploader: PUT $lpath");

        umask(0002);

		@mkdir(dirname($lpath), 0775, true);

		if ($putData = fopen("php://input","r"))
		{
			if ($fp = fopen($lpath,"w"))
			{
				while ($data = fread($putData,64 * 1024))
				{
					$xfer = strlen($data);
					$yfer = fwrite($fp,$data);
				}

				fclose($fp);
			}

			fclose($putData);
			
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
					
						error_log($size . "=" . "$dir/$name");

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
?>
