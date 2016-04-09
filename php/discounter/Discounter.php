<?php

include("../include/json.php");

function getRawdata()
{
	$host = "https://prod." . $GLOBALS[ "host" ];
	
	if (! file_exists("markets.json"))
	{
		$markets = json_decdat(file_get_contents("$host-api.de/markets/markets"));
		file_put_contents("markets.json", json_encdat($markets));
	}
	
	if (! file_exists("categories.json"))
	{
		$categories = json_decdat(file_get_contents("$host-api.de/mobile/categories"));
		file_put_contents("categories.json", json_encdat($categories));
	}
	
	if (! file_exists("products.json"))
	{
		$products = json_decdat(file_get_contents("$host-api.de/stores/240162/products?objectsPerPage=999999"));
		file_put_contents("products.json", json_encdat($products));
	}
}

function recurseCategories($prefix, $rawdata, &$categories)
{
	foreach ($rawdata as $index => $dummy)
	{
		if ($rawdata[ $index ][ "name" ] == "Treuepunkt-Aktion") continue;
		
		echo $prefix . " => " . $rawdata[ $index ][ "name" ] .  "\n";
		
		$categories[ $index ] = array();
		$categories[ $index ][ "name" ] = $rawdata[ $index ][ "name" ];
		$categories[ $index ][ "items" ] = array();
		
		$GLOBALS[ "inx2cat" ][ $rawdata[ $index ][ "id" ] ] = &$categories[ $index ][ "items" ];
		$GLOBALS[ "inx2nam" ][ $rawdata[ $index ][ "id" ] ] = &$categories[ $index ][ "name" ];

		if (isset($rawdata[ $index ][ "childCategories" ]))
		{
			$nextpref = $prefix . " => " . $rawdata[ $index ][ "name" ];

			recurseCategories($nextpref, $rawdata[ $index ][ "childCategories" ], $categories[ $index ][ "items" ]);
		}
	}
}

function buildProducts(&$products)
{
	echo count($products) . "\n";

	foreach ($products as $index => $product)
	{	
		$category = &$GLOBALS[ "inx2cat" ][ $product[ "categoryId" ] ];
	
		$title = trim($product[ "title" ]);
		$brand = trim($product[ "brand" ]);
	
		if (strtolower(substr($title, 0, strlen($brand))) == strtolower($brand))
		{
			$title = trim(substr($title, strlen($brand)));
		}
	
		$title = str_replace("\"", "", $title);
		$title = str_replace(" %", "%", $title);
		$title = str_replace("   ", " ", $title);
		$title = str_replace("  ", " ", $title);
		$title = trim($title);
	
		//
		// Get regular price.
		//
	
		$cents = $product[ "regularPrice" ] ? $product[ "regularPrice" ] : $product[ "price" ];

		//
		// Derive quantity.
		//
	
		$quantity = $product[ "baseQuantity" ] . strtolower($product[ "quantityType" ]);
		
		if ($product[ "drippedOffWeight" ])
		{
			$quantity = $product[ "drippedOffWeight" ] . strtolower($product[ "quantityType" ]);
		}
	
		if ($product[ "multi1" ])
		{
			$quantity = $product[ "multi1" ] . "x" . $quantity;
		}
	
		//
		// Derive base price and price.
		//
	
		if (isset($GLOBALS[ "inx2nam" ][ $product[ "categoryId" ] ]))
		{
			$catname = $GLOBALS[ "inx2nam" ][ $product[ "categoryId" ] ];
		}
		else
		{
			$catname = "Skip Category";
		}
		
		$price = null;
		$base = null;
	
		if ($product[ "basePrice" ] && $product[ "baseAmount" ]  && $product[ "baseMeasure" ])
		{
			$base = $product[ "baseAmount" ] 
				  . strtolower($product[ "baseMeasure" ])
				  . "="
				  . $product[ "basePrice" ];
		
			$price = $quantity
				   . "="
				   . $cents;		   
		}

		if (($base == null) && $product[ "pricePerKilo" ])
		{
			$base = 1 
				  . "kg"
				  . "="
				  . $product[ "pricePerKilo" ];	
			   
			$price = $base; 
		}

		if (($base == null) && $product[ "baseQuantity" ] && $product[ "quantityType" ])
		{
			$base = $quantity
				  . "="
				  . $cents;	
			   
			$price = $base;
		}
	
		if (($base == null) && ($product[ "volumeCode" ] == "STK"))
		{
			$quantity = "1st";
			
			$base = $quantity
				  . "="
				  . $cents;	
	
			$price = $base;
		}
	
		//
		// Remove quantity from title.
		//
	
		$dispquant = str_replace(".", ",", $quantity);
	
		if (substr($dispquant, -2) == "st") 
		{
			if ($dispquant == "1st");
			{
				$parts = explode(" ", $title);
				
				if (count($parts) > 2)
				{
					if (($parts[ count($parts) - 1 ] == "Stück") &&
					    ($parts[ count($parts) - 2 ] != $product[ "baseQuantity" ]))
					{
						$base = $parts[ count($parts) - 2 ]
							  . "st"
				  			  . "="
				  			  . $cents;	
	
						$price = $base;
						
						unset($parts[ count($parts) - 1 ]);
						unset($parts[ count($parts) - 1 ]);
						
						$title = implode(" ",$parts);
					}
				}
			}
			
			$dispquant = substr($dispquant, 0, -2) . " Stück";
		}
	
		if ($dispquant && (substr($title, -strlen($dispquant)) == $dispquant))
		{
			$title = trim(substr($title, 0, -strlen($dispquant)));
		}
		
		if ($dispquant && (strpos($title, " $dispquant, ") !== false))
		{
			$title = str_replace(" $dispquant, ", " ", $title);
		}
		
		if ($catname == "Bedientheke")
		{
			$title = trim(str_replace(" 100g", " ", $title));
			$title = trim(str_replace(" 150g", " ", $title));
		}
		
		//
		// Tune brand and title.
		//
		
		if (strtolower(substr($title, 0, 3)) == $GLOBALS[ "port" ])
		{
			$title = trim(substr($title, 3));
		}
		
		if (strtolower(substr($brand, 0, 4)) == $GLOBALS[ "host" ])
		{
			$brand = "Hausmarke" . substr($brand, 4);
		}
		
		if (strtolower($brand) == $GLOBALS[ "port" ]) $brand = "Eigenmarke";
		if (strtolower($brand) == "marke noch nicht gesetzt") $brand = "-";
		if (strtolower($brand) == "j | hettinger") $brand = "J. Hettinger";
		if (strtolower($brand) == "hausmarke beste wahl") $brand = "Hausmarke";
		
		if (strtolower($brand) == "hausmarke beste wahl") $brand = "Hausmarke";
		
		//
		// Tune price and base.
		//
		
		if (substr($price, 0, 6) == "1l")
		{
			if (substr($title, -3) == " 1000ml") $title = substr($title, 0, -7);
		}
	
		if (substr($base, 0, 6) == "1000ml")
		{
			$base = "1l" . substr($base, 6);
		}
		

		if (substr($price, 0, 6) == "1000ml")
		{
			$price = "1l" . substr($price, 6);
			
			if (substr($title, -3) == " 1l") $title = substr($title, 0, -3);
		}
	
		if (substr($base, 0, 6) == "1000ml")
		{
			$base = "1l" . substr($base, 6);
		}
		
		if (substr($price, 0, 5) == "1000g")
		{
			$price = "1kg" . substr($price, 5);
			
			if (substr($title, -4) == " 1kg") $title = substr($title, 0, -4);
		}

		if (substr($base, 0, 5) == "1000g")
		{
			$base = "1kg" . substr($base, 5);
		}
		
		if (substr($price, 0, 5) == "0.5l=")
		{
			if (substr($title, -6) == " 500ml") $title = substr($title, 0, -6);
		}
		
		if ($price == $base) $base = "*";
		
		//
		// Title fine tuning.
		//
		
		$title = str_replace("Bio-", "Bio ", $title);
		
		if (($brand == "Hausmarke Bio") && (strpos(strtolower($title), "bio") === false))
		{
			$title = "Bio " . $title;
		}
		
		if (substr($title, 0, 1) == "-" ) $title = $brand . $title;
		if (substr($title, 0, 2) == "& ") $title = $brand . $title;
		if (substr($title, 0, 2) == "+ ") $title = $brand . $title;
		
		if (substr($title, -1) == ",") $title = trim(substr($title, 0, -1));
		
		if (substr($title, -6) == " Paket") $title = trim(substr($title, 0, -6));
		
		if (substr($title, 0, 6) == "Marken") 
		{
			$title = strtoupper(substr($title, 6, 1)) . substr($title, 7);
		}
		
		if ($title == "") $title = $brand;

		//
		// Check data.
		//
		
		if (($price == null) || ($base == null)) 
		{
			echo "FAIL: " . $product[ "categoryId" ] . ":" . $product[ "title" ] . "\n";
			
			continue;
		}
		
		$prodstr = "$title|$brand|$price|$base";

		if ((! isset($GLOBALS[ "inx2cat" ][ $product[ "categoryId" ] ])) ||
			(strpos(strtolower($prodstr), $GLOBALS[ "host" ]) !== false) || 
			(strpos(strtolower($prodstr), $GLOBALS[ "port" ]) !== false) ||
			(strpos(strtolower($brand), "feine welt") !== false)) 
		{
			echo "SKIP: " . $product[ "categoryId" ] . ":" . $product[ "title" ] . "\n";
			continue;
		}
		
		$prodstr = "$catname|$title|$brand|$price|$base";

		//
		// Setup product with nice property order.
		//
	
		$category[] = $prodstr;
		
		$GLOBALS[ "csvprods" ][] = $prodstr;
	}
}

$GLOBALS[ "host" ] = chr(0x72) . chr(0x65) . chr(0x77) . chr(0x65);
$GLOBALS[ "port" ] = chr(0x6a) . chr(0x61) . chr(0x21);

getRawdata();

$GLOBALS[ "categories" ] = array();
$GLOBALS[ "inx2cat"    ] = array();
$GLOBALS[ "inx2nam"    ] = array();
$GLOBALS[ "csvprods"   ] = array();

$rawcats = json_decdat(file_get_contents("categories.json"));
$rawcats = $rawcats[ "topLevelCategories" ];
recurseCategories("", $rawcats, $GLOBALS[ "categories" ]);

$products = json_decdat(file_get_contents("products.json"));
$products = $products[ "products" ];
buildProducts($products);

file_put_contents("complete.json", json_encdat($GLOBALS[ "categories" ]));

sort($GLOBALS[ "csvprods" ]);
$csvlines = implode("\n", $GLOBALS[ "csvprods" ]) . "\n";
file_put_contents("complete.csv", $csvlines);

file_put_contents("../../var/prodata/proprices.de-rDE.csv.gzbin", gzencode($csvlines, 9));
?>

