shoppinglist.parsePrice = function(price)
{
    var display = "" + price;

    while (display.length < 3) display = "0" + display;
    var displen = display.length;

    display = display.substring(0, displen - 2) + "," + display.substring(displen - 2, displen);

    return display + " €";
}

shoppinglist.parseRealProduct = function(product, line)
{
    var parts = line.split("|");

    var price = {};

    price.isprice = true;
    price.product = product;

    price.catinx  = parts[ 1 ];
    price.text    = parts[ 2 ];
    price.brand   = parts[ 3 ];
    price.price   = parts[ 4 ];
    price.base    = parts[ 5 ];

    if (price.base == "*") price.base = price.price;

    //
    // Adjust display values.
    //

    price.displaytext = price.text;

    //
    // Price formatting.
    //

    var parts = price.price.split("=");

    if (parts.length == 2)
    {
        price.displayprice = shoppinglist.parsePrice(parts[ 1 ]);

        var units  = parts[ 0 ].substring(parts[ 0 ].length - 2, parts[ 0 ].length);
        var amount = parts[ 0 ].substring(0, parts[ 0 ].length - 2);

        if ((price.text.indexOf(amount) < 0) && ! ((amount == "1") && (units == "st")))
        {
            if (units == "st") units = "Stück";

            price.displaytext += " " + amount + " " + units;
        }
    }

    //
    // Base price formatting.
    //

    var parts = price.base.split("=");

    if (parts.length == 2)
    {
        var cents  = parseInt(parts[ 1 ]);
        var base   = shoppinglist.parsePrice(parts[ 1 ]);
        var units  = parts[ 0 ].substring(parts[ 0 ].length - 2, parts[ 0 ].length);
        var amount = parts[ 0 ].substring(0, parts[ 0 ].length - 2);

        price.displayprice += " – " + amount + " " + units + " = " + base;

        //
        // Make sort to kilogramm or liter.
        //

        price.basesort = WebLibSimple.padNum(cents, 8);
    }

    if ((price.brand == "Eigenmarke") ||
        (price.brand == "Hausmarke") ||
        (price.brand == "Hausmarke Regional") ||
        (price.brand == "-"))
    {
        price.icon = "cheap_320x320.png"
    }
    else
    {
        price.displaytext = price.brand + " " + price.displaytext;
    }

    return price;
}

shoppinglist.parseProduct = function(text)
{
    var product = {};

    product.isproduct = true;
    product.text = text;

    shoppinglist.stripIntro(product);
    shoppinglist.parseStore(product);

    shoppinglist.replaceSynonyms(product);

    product.text = product.text.trim();

    return product;
}

shoppinglist.replaceSynonyms = function(product)
{
    //
    // Replace synonyms in phrase.
    //

    var text = product.text.toLowerCase();

    var synonyms = WebLibStrings.strings[ "product.synonyms" ];

    if (! synonyms) return;

    for (var sninx in synonyms)
    {
        var synonym = synonyms[ sninx ];
        var keywords = synonym[ 1 ];

        if (! Array.isArray(keywords))
        {
            keywords = [];
            keywords.push(synonym[ 1 ]);
        }

        for (var kwinx in keywords)
        {
            var phrase = keywords[ kwinx ];
            var pos = text.indexOf(phrase);
            var len = phrase.length;

            if (pos >= 0)
            {
                product.text = product.text.substring(0, pos)
                             + synonym[ 0 ]
                             + product.text.substring(pos + len);

                text = product.text.toLowerCase();
            }
        }
    }
}

shoppinglist.stripIntro = function(product)
{
    //
    // Strip voice intent intro phrase.
    //

    var text = product.text.toLowerCase();

    var keywords = WebLibStrings.strings[ "intent.intro.keywords" ];

    if (! keywords) return;

    for (var kwinx in keywords)
    {
        var phrase = keywords[ kwinx ];
        var pos = text.indexOf(phrase);
        var len = phrase.length;

        if (pos >= 0)
        {
            product.text = product.text.substring(0, pos).trim()
                         + " "
                         + product.text.substring(pos + len).trim();

            text = product.text.toLowerCase();
        }
    }
}

shoppinglist.parseStore = function(product)
{
    //
    // Parse store phrase.
    //

    var text = product.text.toLowerCase();

    var stores = WebLibStrings.strings[ "stores.defines" ];
    var keywords = WebLibStrings.strings[ "stores.intro.keywords" ];

    if (! (stores && keywords)) return;

    for (var kwinx in keywords)
    {
        for (var stinx in stores)
        {
            if (! stores[ stinx ].keywords) continue;

            for (var ksinx in stores[ stinx ].keywords)
            {
                var phrase = keywords[ kwinx ] + " " + stores[ stinx ].keywords[ ksinx ];
                var pos = text.indexOf(phrase);
                var len = phrase.length;

                if (pos >= 0)
                {
                    product.text = product.text.substring(0, pos).trim()
                                 + " "
                                 + product.text.substring(pos + len).trim();

                    product.storeobj = stores[ stinx ];

                    console.log("shoppinglist.parseStore: " + product.storeobj.name + " => " + product.text);

                    return;
                }
            }
        }
    }

    //
    // The first entry is default.
    //

    product.storeobj = stores[ 0 ];
}
