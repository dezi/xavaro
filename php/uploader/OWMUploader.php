<?php

//
// Web server part of OWM upload procedure.
//

header("Content-Type: text/plain");

$method = $_SERVER[ 'REQUEST_METHOD' ];

if (($method == "PUT") && isset($_SERVER[ 'HTTP_UPLOAD_FILE' ]))
{
	$upload = $_SERVER[ 'HTTP_UPLOAD_FILE' ];
	$lpath  = "/home/xavaro/var/owmdata/forecast/" . basename($upload);
	$tpath  = $lpath . "." . getmypid() . ".tmp";

	error_log("OWMUploader: PUT $lpath");

	umask(0002);
	
	if ($putData = fopen("php://input","r"))
	{
		$owmjson = "";
		
		while ($data = fread($putData, 64 * 1024))
		{
			$owmjson .= $data;
		}
		
		fclose($putData);
		
		$gzencoded = gzencode($owmjson, 9);

		if ($fp = fopen($tpath, "w"))
		{
			fwrite($fp, $gzencoded);
			fclose($fp);
			
			rename($tpath, $lpath);
		}
	}
}

?>