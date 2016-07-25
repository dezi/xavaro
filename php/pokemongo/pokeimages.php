<?php

include "../include/json.php";

function downloadMeta() 
{
	$cont = file_get_contents("http://www.pokemon.com/us/api/pokedex/kalos");
	file_put_contents("./pokeimages/kalos.us.json", json_encdat(json_decdat($cont)));

	$cont = file_get_contents("http://www.pokemon.com/de/api/pokedex/kalos");
	file_put_contents("./pokeimages/kalos.de.json", json_encdat(json_decdat($cont)));
}

function downloadImages() 
{
	for ($inx = 1; $inx < 721; $inx++)
	{
		//
		// http://assets.pokemon.com/assets/cms2/img/pokedex/full/010.png
		//
	
		$num = str_pad($inx, 3, "0", STR_PAD_LEFT);
		$url = "http://assets.pokemon.com/assets/cms2/img/pokedex/full/" . $num . ".png";
		$png = file_get_contents($url);
		if (! $png) continue;
		$dst = "./pokeimages/full_id/$num.png";
	
		echo "$url\n";
	
		file_put_contents($dst, $png);
	}
}

function resizeImages() 
{
	for ($inx = 1; $inx < 721; $inx++)
	{
		$num = str_pad($inx, 3, "0", STR_PAD_LEFT);
		resizeImage("./pokeimages/full/$num.png", "./pokeimages/64x64/$num.png", 64);
		echo "./pokeimages/64x64/$num.png\n";
	}
}

function resizeImage($originalFile, $targetFile, $newWidth) 
{
    $info = getimagesize($originalFile);
    $mime = $info['mime'];

    switch ($mime) 
    {
            case 'image/jpeg':
                    $image_create_func = 'imagecreatefromjpeg';
                    $image_save_func = 'imagejpeg';
                    $new_image_ext = 'jpg';
                    break;

            case 'image/png':
                    $image_create_func = 'imagecreatefrompng';
                    $image_save_func = 'imagepng';
                    $new_image_ext = 'png';
                    break;

            case 'image/gif':
                    $image_create_func = 'imagecreatefromgif';
                    $image_save_func = 'imagegif';
                    $new_image_ext = 'gif';
                    break;

            default: 
                    throw new Exception('Unknown image type.');
    }

    $img = $image_create_func($originalFile);
    list($width, $height) = getimagesize($originalFile);

    $newHeight = ($height / $width) * $newWidth;
    
    $tmp = imagecreatetruecolor($newWidth, $newHeight);

    imagealphablending($tmp, false);
    imagesavealpha($tmp, true);
    $transparent = imagecolorallocatealpha($tmp, 255, 255, 255, 127);
    imagefilledrectangle($tmp, 0, 0, $newWidth, $newHeight, $transparent);
    
    imagecopyresampled($tmp, $img, 0, 0, 0, 0, $newWidth, $newHeight, $width, $height);

    if (file_exists($targetFile)) 
    {
        unlink($targetFile);
    }
    
    //$image_save_func($tmp, $targetFile);
    imagepng($tmp, $targetFile, 9);
}

function decodeASCII85($str) 
{
	$str = preg_replace("/ \t\r\n\f/","",$str);
	$str = preg_replace("/z/","!!!!!",$str);
	$str = preg_replace("/y/","+<VdL/",$str);
	// Pad the end of the string so it's a multiple of 5
	$padding = 5 - (strlen($str) % 5);
	if (strlen($str) % 5 === 0) {
		$padding = 0;
	}
	$str .= str_repeat('u',$padding);
	$num = 0;
	$ret = '';
	// Foreach 5 chars, convert it to an integer
	while ($chunk = substr($str, $num * 5, 5)) {
		$tmp = 0;
		foreach (unpack('C*',$chunk) as $item) {
			$tmp *= 85;
			$tmp += $item - 33;
		}
		// Convert the integer in to a string
		$ret .= pack('N', $tmp);
		$num++;
	}
	// Remove any padding we had to add
	$ret = substr($ret,0,strlen($ret) - $padding);
	return $ret;
}
	
function encodeASCII85($str) 
{
	$ret   = '';
	$debug = 0;
	$padding = 4 - (strlen($str) % 4);
	if (strlen($str) % 4 === 0) 
	{
		$padding = 0;
	}
	if ($debug) 
	{
		printf("Length: %d = Padding: %s<br /><br />\n",strlen($str),$padding);
	}
	// If we don't have a four byte chunk, append \0s
	
	$str .= str_repeat("\0", $padding);
	
	foreach (unpack('N*',$str) as $chunk) 
	{
		// If there is an all zero chunk, it has a shortcut of 'z'
		if ($chunk == "\0") 
		{
			$ret .= "z";
			continue;
		}
		
		// Four spaces has a shortcut of 'y'
		if ($chunk == 538976288) 
		{
			$ret .= "y";
			continue;
		}
		
		if ($debug) 
		{
			var_dump($chunk); print "<br />\n";
		}
		
		// Convert the integer into 5 "quintet" chunks
		for ($a = 0; $a < 5; $a++) 
		{
			$b	= intval($chunk / (pow(85,4 - $a)));
			$ret .= chr($b + 33);
			if ($debug) 
			{
				printf("%03d = %s <br />\n",$b,chr($b+33));
			}
			$chunk -= $b * pow(85,4 - $a);
		}
	}
	
	// If we added some null bytes, we remove them from the final string
	if ($padding) 
	{
		$ret = preg_replace("/z$/",'!!!!!',$ret);
		$ret = substr($ret,0,strlen($ret) - $padding);
	}
	
	return $ret;
}

function encodeImages() 
{
	$java = "";
	
	for ($inx = 1; $inx <= 151; $inx++)
	{
		$num = str_pad($inx, 3, "0", STR_PAD_LEFT);
		$png = file_get_contents("./pokeimages/64x64/$num.png");
		
		$enc = encodeASCII85($png);
		$enc = str_replace("\\", "\\\\", $enc);
		$enc = str_replace("\"", "\\\"", $enc);
		
		$idx = str_pad($inx, 3, " ", STR_PAD_LEFT);

		$java .= "\t\t\tcase $idx: png = \"$enc\"; break;\n";
	}
	
	file_put_contents("./pokeimages.java", $java);
}

//resizeImages();
encodeImages();

?>