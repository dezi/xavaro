<?php

	//
	// Web server part of shared items upload procedure.
	//
	
    header("Content-Type: text/plain");

    $method = $_SERVER[ 'REQUEST_METHOD' ];

    if (($method == "PUT") 
    		&& isset($_SERVER[ 'HTTP_UPLOAD_UUID' ]) 
    		&& isset($_SERVER[ 'HTTP_UPLOAD_IDENTITY' ]))
    {
    	$uuid = $_SERVER[ 'HTTP_UPLOAD_UUID' ];
    	$identity = $_SERVER[ 'HTTP_UPLOAD_IDENTITY' ];
    	$lpath  = "/home/xavaro/var/shares/" . $uuid . "_" . $identity;
		
		error_log("ShareUploader: PUT $lpath");

        umask(0002);

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
		}
    }
?>
