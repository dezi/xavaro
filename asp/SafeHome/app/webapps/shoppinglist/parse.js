shoppinglist.parseCents = function(price)
{
    var display = "" + price;

    while (display.length < 3) display = "0" + display;
    var displen = display.length;

    display = display.substring(0, displen - 2) + "," + display.substring(displen - 2, displen);

    return display + " €";
}

shoppinglist.parsePrice = function(price, base)
{
    var pelem = {};

    var parts = price.split("=");
    if (parts.length != 2) return pelem;

    pelem.cents = parseInt(parts[ 1 ]);
    pelem.units = "";

    var work = parts[ 0 ];

    if (work.substring(work.length - 2, work.length) == "bl") pelem.units = "bl";
    if (work.substring(work.length - 2, work.length) == "st") pelem.units = "st";
    if (work.substring(work.length - 2, work.length) == "kg") pelem.units = "kg";
    if (work.substring(work.length - 2, work.length) == "ml") pelem.units = "ml";

    if (pelem.units == "")
    {
        if (work.substring(work.length - 1, work.length) == "g") pelem.units = "g";
        if (work.substring(work.length - 1, work.length) == "l") pelem.units = "l";
    }

    work = work.substring(0, work.length - pelem.units.length);

    if (work.indexOf("x") >= 0)
    {
        parts = work.split("x");

        pelem.multi  = parseInt(parts[ 0 ]);
        pelem.amount = parseInt(parts[ 1 ]);
    }
    else
    {
        pelem.multi  = 1;
        pelem.amount = parseInt(work);
    }

    //
    // Normalize sort price to kilogramm or liter or 1000 pieces.
    //

    pelem.sortcents = pelem.cents;

    if ((pelem.units == "bl") || (pelem.units == "st") ||
        (pelem.units == "ml") || (pelem.units == "g"))
    {
        pelem.sortcents = Math.round((pelem.sortcents * 1000) / (pelem.multi * pelem.amount));
    }

    pelem.sortcents = WebLibSimple.padNum(pelem.sortcents, 8);

    //
    // Normalize base prizes.
    //

    if (base && ((pelem.units == "st") || (pelem.units == "bl")))
    {
        var itemcents = Math.round((pelem.cents + 1) / (pelem.multi * pelem.amount));

        if (itemcents >= 15)
        {
            pelem.cents  = itemcents;
            pelem.multi  = 1;
            pelem.amount = 1;
        }
        else
        {
            pelem.cents  = Math.round((pelem.cents * 100) / (pelem.multi * pelem.amount));
            pelem.multi  = 1;
            pelem.amount = 100;
        }
    }

    if (pelem.units == "st") pelem.units = "Stück";
    if (pelem.units == "bl") pelem.units = "Blatt";

    pelem.displaycents = shoppinglist.parseCents(pelem.cents);
    pelem.displayamount = "";

    if (pelem.multi != 1)
    {
        pelem.displayprice = pelem.multi + "x" + pelem.amount
        pelem.displayamount = pelem.displayprice + " " + pelem.units;
    }
    else
    {
        pelem.displayprice = pelem.amount

        if ((pelem.amount != 1) || (pelem.units != "Stück"))
        {
            pelem.displayamount = pelem.amount + " " + pelem.units;
        }
    }

    pelem.displayprice += " " + pelem.units + " = " + pelem.displaycents;

    return pelem;
}

shoppinglist.parseCategory = function(product, line)
{
    var parts = line.split("|");

    var category = {};

    category.iscategory = true;
    category.product = product;

    category.catinx = parts[ 1 ];
    category.level  = parts.length - 3;
    category.text   = parts[ parts.length - 2 ];
    category.count  = parseInt(parts.pop());

    parts.shift();
    parts.shift();

    category.path = parts.join("|") + "|";

    return category;
}

shoppinglist.parseRealProduct = function(product, line)
{
    var price = {};

    price.isprice = true;
    price.line    = line;

    var parts = line.split("|");

    price.catinx  = parts[ 1 ];
    price.text    = parts[ 2 ];
    price.brand   = parts[ 3 ];
    price.price   = parts[ 4 ];
    price.base    = parts[ 5 ];

    if (price.base == "*") price.base = price.price;

    //
    // Price formatting.
    //

    price.baseobj = shoppinglist.parsePrice(price.base, true);
    price.priceobj = shoppinglist.parsePrice(price.price, false);

    price.basesort = price.baseobj.sortcents;
    price.displayprice = price.priceobj.displaycents;
    price.displayamount = price.priceobj.displayamount;

    //
    // No display of amount with products having a
    // circa weight in text.
    //

    if (price.text.match(/ ca\. [0-9]+g/))
    {
        price.displayamount = "";
    }

    //
    // Adjust display values.
    //

    price.displaytext = price.text;

    if (price.displayamount != "") price.displaytext += " " + price.displayamount;

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

    price.displayprice = price.displayprice + " – " + price.baseobj.displayprice;

    return price;
}

shoppinglist.parseProduct = function(text)
{
    var product = {};

    product.isproduct = true;
    product.text = text;

    shoppinglist.stripIntro(product);
    shoppinglist.parseStore(product);
    shoppinglist.parseQuantity(product);

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

shoppinglist.parseQuantity = function(product)
{
    //
    // Parse quantity from product text.
    //

    product.quantunit = "st";
    product.quantphrase = "x";
    product.quantmulti = 1;
    product.quantity = 1;

    //
    // Get units from phrase.
    //

    var units = WebLibStrings.strings[ "quantity.units" ];

    if (units)
    {
        for (var uinx in units)
        {
            var phrase = units[ uinx ].phrase;
            var parts = product.text.split(new RegExp("\\b" + phrase + "\\b","i"));

            if (parts.length == 2)
            {
                product.quantunit   = units[ uinx ].unit;
                product.quantmulti  = units[ uinx ].multi ? units[ uinx ].multi : 1;
                product.quantphrase = units[ uinx ].phrase;

                //
                // Check rest of left hand string for quantity
                //

                var numparts = parts[ 0 ].trim().split(" ");
                var number = parseFloat(numparts[ numparts.length - 1].replace(",", "."));

                if (! isNaN(number))
                {
                    product.quantity *= number;
                    numparts.pop();

                    parts[ 0 ] = numparts.join(" ");
                }

                product.text = parts[ 0 ].trim() + " " + parts[ 1 ].trim();
            }
        }
    }

    //
    // Get multiple quantities from phrase.
    //

    var amount = WebLibStrings.strings[ "quantity.amount" ];

    if (amount)
    {
        for (var ainx in amount)
        {
            var phrase = amount[ ainx ].phrase;
            var parts = product.text.split(new RegExp("\\b" + phrase + "\\b","i"));

            if (parts.length == 2)
            {
                product.quantity *= amount[ ainx ].amount;

                product.text = parts[ 0 ].trim() + " " + parts[ 1 ].trim();
            }
        }
    }

    //
    // Get simple standalone quantities.
    //

    var parts = product.text.split(new RegExp("\\b[0-9,.]+\\b","i"));

    if (parts.length == 2)
    {
        var start = parts[ 0 ].length;
        var end   = product.text.length - parts[ 1 ].length;

        var numpart = product.text.substring(start,end);
        var number = parseFloat(numpart.replace(",", "."));

        if (! isNaN(number))
        {
            product.quantity *= number;
        }

        product.text = parts[ 0 ].trim() + " " + parts[ 1 ].trim();
    }

    //
    // Normalize quantities.
    //

    product.quantity *= product.quantmulti;
    product.quantmulti = 1;

    if ((product.quantunit == "kg") && (product.quantity <= 0.25))
    {
        product.quantity = product.quantity * 1000;
        product.quantmulti = 1;
        product.quantunit = "g";
    }

    if ((product.quantunit == "g") && (product.quantity >= 1000))
    {
        product.quantity = product.quantity / 1000;
        product.quantmulti = 1;
        product.quantunit = "kg";
    }

    if ((product.quantunit == "l") && (product.quantity <= 0.25))
    {
        product.quantity = product.quantity * 1000;
        product.quantmulti = 1;
        product.quantunit = "ml";
    }

    if ((product.quantunit == "ml") && (product.quantity >= 1000))
    {
        product.quantity = product.quantity / 1000;
        product.quantmulti = 1;
        product.quantunit = "kg";
    }

    if (product.quantunit == "st")
    {
        if (product.quantphrase = "Pack") product.quantphrase = "x";

        product.displayquant = product.quantity + " " + product.quantphrase;
    }
    else
    {
        product.displayquant = product.quantity + " " + product.quantunit;
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

                    product.storename = stores[ stinx ].store;
                    product.storesort = stores[ stinx ].sort;

                    console.log("shoppinglist.parseStore: " + product.storename + " => " + product.text);

                    return;
                }
            }
        }
    }

    //
    // The first entry is default.
    //

    product.storename = stores[ 0 ].store;
    product.storesort = stores[ 0 ].sort;
}
