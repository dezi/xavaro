<?php

	//
	// Web server part of PGM upload procedure.
	//
	
    header("Content-Type: text/plain");

    $method = $_SERVER[ 'REQUEST_METHOD' ];

    if ($method == "PUT")
    {
    	include("../include/json.php");

    	//
    	// Read content json.
    	//
    	
    	$data = "";
    	
    	if ($putData = fopen("php://input","r"))
		{
			while ($chunk = fread($putData,64 * 1024))
			{
				$data .= $chunk;
			}

			fclose($putData);
		}
		
		//error_log($data);

		$message = json_decdat($data);
		
		if (! isset($message[ "type" ])) exit();
		if (! isset($message[ "name" ])) exit();
		if (! isset($message[ "imgurl" ])) exit();
		if (! isset($message[ "country" ])) exit();

		$type = $message[ "type" ];
		$name = $message[ "name" ];
		$imgurl = $message[ "imgurl" ];
		$country = $message[ "country" ];
		
		$name = str_replace("/", "_", $name);
		
		$lpath = "/home/xavaro/var/pgminfo/$type/$country";
	 	@mkdir($lpath, 0755, true);
	 	$lpath .= "/" . $name;
		
		$jpeg = file_get_contents($imgurl);
		if ($jpeg == false) exit();
		
		//
		// Write original.
		//
		
		$orig = $lpath . ".orig.jpg";
		$thmb = $lpath . ".thmb.jpg";
		
		file_put_contents($orig, $jpeg);
		chmod($orig, 0644);
		error_log("PGMUploader: PUT $orig");
	 
		//
		// Write thumbnail.
		//

		$source_image = imagecreatefromjpeg($orig);
		$width = imagesx($source_image);
		$height = imagesy($source_image);
	
		$desired_width = 256;
		$desired_height = floor($height * ($desired_width / $width));
		
		$virtual_image = imagecreatetruecolor($desired_width, $desired_height);
		
		imagecopyresampled($virtual_image, $source_image, 0, 0, 0, 0, 
			$desired_width, $desired_height, $width, $height);
			
		imagejpeg($virtual_image, $thmb);    
		chmod($thmb, 0644);
		
		error_log("PGMUploader: PUT $thmb");
	}
?>
