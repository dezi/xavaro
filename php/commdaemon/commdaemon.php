<?php

include("../include/json.php");

if (! ($sock = socket_create(AF_INET, SOCK_DGRAM, 0)))
{
	$errorcode = socket_last_error();
	$errormsg = socket_strerror($errorcode);

	die("Couldn't create socket: [$errorcode] $errormsg \n");
}

echo "Socket created \n";

if (! socket_bind($sock, "www.xavaro.de" , 42742))
{
	$errorcode = socket_last_error();
	$errormsg = socket_strerror($errorcode);

	die("Could not bind socket : [$errorcode] $errormsg \n");
}

echo "Socket bind OK \n";

$identities = array();

$packetlog = array();
$pincodes = array();

function readableuuid($uuid)
{
	$uuidReadable = unpack("H*",$uuid);
	$uuidReadable = preg_replace("/([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})/", "$1-$2-$3-$4-$5", $uuidReadable);
	$uuidReadable = array_merge($uuidReadable);
	$uuidReadable = $uuidReadable[ 0 ];

	return $uuidReadable;
}

function storeident($idsender)
{
	$data = array();
	$data[ "addr" ] = $GLOBALS[ "remote_ip"   ];
	$data[ "port" ] = $GLOBALS[ "remote_port" ];
	
	$GLOBALS[ "identities" ][ $idsender ] = $data;
}

function binaryuuid($uuidstring)
{
	return pack("H*", str_replace("-", "", $uuidstring));
}

function createuuid()
{
    return sprintf('%04x%04x-%04x-%04x-%04x-%04x%04x%04x', 
    	mt_rand(0, 65535), mt_rand(0, 65535), 
    	mt_rand(0, 65535), 
    	mt_rand(16384, 20479), 
    	mt_rand(32768, 49151), 
    	mt_rand(0, 65535), mt_rand(0, 65535), mt_rand(0, 65535)
    	);
}

function storepacket($idremote, $packetuuid, $buf)
{
	$GLOBALS[ "packetlog" ][ $idremote ][ $packetuuid ] = $buf;
	
	echo "    store => " . readableuuid($idremote) 
				. " => " . readableuuid($packetuuid) . "\n";
}

function removepacket($idremote, $packetuuid)
{
	if (isset($GLOBALS[ "packetlog" ][ $idremote ][ $packetuuid ]))
	{
		unset($GLOBALS[ "packetlog" ][ $idremote ][ $packetuuid ]);
	
		echo "    remove => " . readableuuid($idremote) 
					 . " => " . readableuuid($packetuuid) . "\n";
					 
		if (count($GLOBALS[ "packetlog" ][ $idremote ]) == 0)
		{
			unset($GLOBALS[ "packetlog" ][ $idremote ]);
		}
	}
}

function resendpackets($idremote, $remote_ip, $remote_port)
{
	if (! isset($GLOBALS[ "packetlog" ][ $idremote ])) return;

	foreach ($GLOBALS[ "packetlog" ][ $idremote ] as $packetuuid => $buf)
	{
		echo "$remote_ip : $remote_port -- RESEND " 
		   . substr($buf, 0, 4) 
		   . " => " . readableuuid($idremote) 
		   . " => " . readableuuid($packetuuid) 
		   . "\n";

		socket_sendto($GLOBALS[ "sock" ], $buf, strlen($buf), 0, $remote_ip, $remote_port);

		break;
	}	
}

while (1)
{
	echo "\nWaiting for data ...\n";

	$r = socket_recvfrom($sock, $buf, 8192, 0, $remote_ip, $remote_port);
	
	if (substr($buf, 0, 4) == "PUPS")
	{
		echo "$remote_ip : $remote_port -- " . substr($buf, 0, 4) . "\n";
		
		continue;
	}
	
	if (substr($buf, 0, 4) == "MYIP")
	{
		echo "$remote_ip : $remote_port -- " . substr($buf, 0, 4) . "\n";
		
		$myip = "MYIP";
		
		$parts = explode(".",$remote_ip);
		
		$myip .= chr($parts[ 0 ]);
		$myip .= chr($parts[ 1 ]);
		$myip .= chr($parts[ 2 ]);
		$myip .= chr($parts[ 3 ]);
		
		socket_sendto($sock, $myip, strlen($myip), 0, $remote_ip, $remote_port);

		continue;
	}

	if (substr($buf, 0, 4) == "PING")
	{
		$idsender = substr($buf,4);
		
		storeident($idsender);
		
		echo "$remote_ip : $remote_port -- " . substr($buf, 0, 4) . " " . readableuuid($idsender) . "\n";

		resendpackets($idsender, $remote_ip, $remote_port);
		
		continue;
	}
		
	if (substr($buf, 0, 4) == "ACME")
	{
		$idsender = substr($buf,  4, 16);
		$uuid     = substr($buf, 20, 16);
		
		echo "$remote_ip : $remote_port -- " 
			. substr($buf, 0, 4) 
			. " " . readableuuid($idsender) 
			. " => " . readableuuid($uuid) 
			. "\n";

		removepacket($idsender, $uuid);
		
		resendpackets($idsender, $remote_ip, $remote_port);
		
		continue;
	}

	if (substr($buf, 0, 4) == "CRYP")
	{
		//
		// Unreliable packet w/o ack.
		//
		
		$idsender = substr($buf,  4, 16);
		$idremote = substr($buf, 20, 16);
		
		storeident($idsender);

		if (! isset($identities[ $idremote ]))
		{
			echo "    => " . readableuuid($idremote) . " identity unknown\n";
			
			continue;
		}
		
		$remote_ip   = $identities[ $idremote ][ "addr" ];
		$remote_port = $identities[ $idremote ][ "port" ];
		
		echo "$remote_ip : $remote_port -- " 
			. substr($buf, 0, 4) 
			. " " . readableuuid($idsender) 
			. " => " . readableuuid($idremote) 
			. "\n";

		socket_sendto($sock, $buf, strlen($buf), 0, $remote_ip, $remote_port);
		
		continue;
	}
	
	if (substr($buf, 0, 4) == "CACK")
	{
		//
		// Unreliable packet with server ack.
		//
		
		$idsender = substr($buf,  4, 16);
		$idremote = substr($buf, 20, 16);
		$uuid     = substr($buf, 36, 16);
		
		storeident($idsender);
		
		//
		// Send ack to idsender.
		//
		
		$ack = substr($buf, 0, 4 + 16 + 16 + 16);

		echo "$remote_ip : $remote_port -- " 
			. substr($ack, 0, 4) 
			. " " . readableuuid($idsender) 
			. " => " . readableuuid($uuid) 
			. "\n";

		socket_sendto($sock, $ack, strlen($ack), 0, $remote_ip, $remote_port);

		//
		// Deliver packet to idremote.
		//
		
		if (! isset($identities[ $idremote ]))
		{
			echo "    => " . readableuuid($idremote) . " identity unknown\n";
			
			continue;
		}

		$remote_ip   = $identities[ $idremote ][ "addr" ];
		$remote_port = $identities[ $idremote ][ "port" ];

		$buf = "CRYP" . substr($buf, 4, 32) . substr($buf, 52);
		
		echo "$remote_ip : $remote_port -- " 
			. substr($buf, 0, 4) 
			. " " . readableuuid($idsender) 
			. " => " . readableuuid($idremote) 
			. "\n";

		socket_sendto($sock, $buf, strlen($buf), 0, $remote_ip, $remote_port);
		
		continue;
	}
	
	if (substr($buf, 0, 4) == "CARL")
	{
		//
		// Reliable packet with server and client ack.
		//
		
		$idsender = substr($buf,  4, 16);
		$idremote = substr($buf, 20, 16);
		$uuid     = substr($buf, 36, 16);
		
		storeident($idsender);
		
		//
		// Send ack to idsender.
		//
		
		$ack = "CACK" . substr($buf, 4, 16 + 16 + 16);

		echo "$remote_ip : $remote_port -- " 
			. substr($ack, 0, 4) 
			. " " . readableuuid($idsender) 
			. " => " . readableuuid($uuid) 
			. "\n";

		socket_sendto($sock, $ack, strlen($ack), 0, $remote_ip, $remote_port);

		//
		// Deliver packet to idremote with temp storage.
		//
		
		$packetuuid = binaryuuid(createuuid());
		
		$buf = substr($buf, 0, 4 + 16 + 16) . $packetuuid . substr($buf, 52);
		
		storepacket($idremote, $packetuuid, $buf);
		
		//
		// Try to send packet.
		//
		
		if (! isset($identities[ $idremote ]))
		{
			echo "    => " . readableuuid($idremote) . " identity unknown\n";
			
			continue;
		}

		$remote_ip   = $identities[ $idremote ][ "addr" ];
		$remote_port = $identities[ $idremote ][ "port" ];
		
		echo "$remote_ip : $remote_port -- " 
			. substr($buf, 0, 4) 
			. " " . readableuuid($idsender) 
			. " => " . readableuuid($idremote) 
			. " => " . readableuuid($packetuuid) 
			. "\n";

		socket_sendto($sock, $buf, strlen($buf), 0, $remote_ip, $remote_port);
		
		continue;
	}

	if (substr($buf, 0, 4) == "JSON")
	{
		echo "$remote_ip : $remote_port -- " . $buf . "\n";

		$json = json_decdat(substr($buf,4));
		
		if (isset($json[ "identity" ]))
		{
			$idsender = binaryuuid($json[ "identity" ]);
		
			storeident($idsender);
		}

		if (isset($json[ "type" ]) && ($json[ "type" ] == "sendPin"))
		{
			$pincodes[ $json[ "pincode" ] ] = $json[ "identity" ];
		}
	
		if (isset($json[ "type" ]) && ($json[ "type" ] == "requestPin"))
		{
			$pincode = $json[ "pincode" ];
		
			$data = array();
		
			$data[ "type"    ] = "responsePin";
			$data[ "pincode" ] = $pincode;

			if (isset($pincodes[ $pincode ]))
			{
				$data[ "status"   ] = "success";
				$data[ "idremote" ] = $pincodes[ $pincode ];
			}
			else
			{
				$data[ "status" ] = "failure";
			}
		
			$buf = "JSON" . json_encdat($data);
		
			echo "    => " . $remote_ip . ":" . $remote_port . "\n";
		
			socket_sendto($sock, $buf, strlen($buf), 0, $remote_ip, $remote_port);
		}
	
		if (isset($json[ "idremote" ]))
		{
			$idremote = binaryuuid($json[ "idremote" ]);
			
			if (! isset($identities[ $idremote ]))
			{
				echo "    => " . readableuuid($idremote) . " identity unknown\n";
			
				continue;
			}

			$remote_ip   = $identities[ $idremote ][ "addr" ];
			$remote_port = $identities[ $idremote ][ "port" ];
		
			echo "    => " . $remote_ip . ":" . $remote_port . "\n";
		
			socket_sendto($sock, $buf, strlen($buf), 0, $remote_ip, $remote_port);
		}
		
		continue;
	}
}
?>
