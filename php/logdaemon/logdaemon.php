<?php

    if(!($sock = socket_create(AF_INET, SOCK_DGRAM, 0)))
    {
        $errorcode = socket_last_error();
        $errormsg = socket_strerror($errorcode);

        die("Couldn't create socket: [$errorcode] $errormsg \n");
    }

    echo "Socket created \n";

    // Bind the source address
    if( !socket_bind($sock, "www.xavaro.de" , 42742) )
    {
        $errorcode = socket_last_error();
        $errormsg = socket_strerror($errorcode);

        die("Could not bind socket : [$errorcode] $errormsg \n");
    }

    echo "Socket bind OK \n";
    
	while(1)
    {
        echo "\n Waiting for data ... \n";

        //Receive some data
        $r = socket_recvfrom($sock, $buf, 2048, 0, $remote_ip, $remote_port);
        echo "$remote_ip : $remote_port -- " . $buf;
        
    	//Send back the data to the client
        socket_sendto($sock, "OK " . $buf , 100 , 0 , $remote_ip , $remote_port);
    }
?>
