<?php

	//
	// Web server part of shared items upload and download
	// procedure. These scripts react completely unorthodox
	// and that is on purpose.
	//
	
	if (! isset($_SERVER[ 'CONTENT_LENGTH' ])) exit();
	if (! isset($_SERVER[ 'HTTP_XREQUEST_UUID' ])) exit();
	
	$uuid  = $_SERVER[ 'HTTP_XREQUEST_UUID' ];
    $lpath = "/home/xavaro/var/shares/" . $uuid;

    $method = $_SERVER[ 'REQUEST_METHOD' ];
    
    if ($method == "XGET")
    {
		error_log("ShareHandler UL: XGET $lpath");

		//
		// Respond with size of file already on disk.
		//
		
		$localsize = file_exists($lpath) ? filesize($lpath) : 0;
		header("Content-Length: $localsize");
		ob_flush();
		flush();
		
        umask(0002);

		if ($input = fopen("php://input","r"))
		{
			if ($fd = fopen($lpath,"a"))
			{
				while (($data = fread($input,64 * 1024)) != false)
				{
					fwrite($fd,$data);
				}

				fclose($fd);
			}

			fclose($input);
		}
    }
    
    if ($method == "XPUT")
    {
		error_log("ShareHandler DL: XPUT $lpath");
		
		//
		// Adjust with size of file already on remote.
		//

    	$rlen = $_SERVER[ 'CONTENT_LENGTH' ];
		$localsize = file_exists($lpath) ? filesize($lpath) : 0;
		$localsize -= $rlen;
		header("Content-Length: $localsize");
		ob_flush();
		flush();
		
        umask(0002);

		if ($output = fopen("php://output","w"))
		{
			if ($fd = fopen($lpath,"r"))
			{
				fseek($fd, $rlen);
				
				while (($data = fread($fd,64 * 1024)) != false)
				{
					fwrite($output,$data);
				}

				fclose($fd);
			}

			fclose($output);
			
			//
			// Delete file after download.
			//

			@unlink($lpath);

			error_log("ShareHandler RM: XPUT $lpath");
		}
    }
?>
