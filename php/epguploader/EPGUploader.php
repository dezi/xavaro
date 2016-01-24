<?php

    header("Content-Type: text/plain");

    $method = $_SERVER[ 'REQUEST_METHOD'   ];

    if ($method == "PUT" && isset($_SERVER[ 'HTTP_UPLOAD_FILE' ]))
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

			unset($putData);
		}
    }
?>
