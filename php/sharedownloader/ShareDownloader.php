<?php

	//
	// Web server part of shared items download procedure.
	//
	
    header("Content-Type: text/plain");

    $method = $_SERVER[ 'REQUEST_METHOD' ];

    if (($method == "GET") 
    		&& isset($_SERVER[ 'HTTP_DOWNLOAD_UUID' ]) 
    		&& isset($_SERVER[ 'HTTP_DOWNLOAD_IDENTITY' ]))
    {
    	$uuid = $_SERVER[ 'HTTP_DOWNLOAD_UUID' ];
    	$identity = $_SERVER[ 'HTTP_DOWNLOAD_IDENTITY' ];
    	$lpath  = "/home/xavaro/var/shares/" . $uuid . "_" . $identity;
		
		error_log("ShareDownloader: GET $lpath");

		if ($getData = fopen("php://output","w"))
		{
			if ($fp = fopen($lpath,"r"))
			{
				while ($data = fread($fp,64 * 1024))
				{
					$xfer = strlen($data);
					$yfer = fwrite($getData,$data);
				}

				fclose($fp);
			}

			fclose($getData);
		}
    }
?>
