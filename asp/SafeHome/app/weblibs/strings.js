//
// Strings library.
//

WebLibStrings = {};

WebLibStrings.webLibLocale = [ "strings.de-rDE.json" ];

WebLibStrings.findLocale = function(localearray, locale)
{
    if (localearray)
    {
        for (var inx = 0; inx < localearray.length; inx++)
        {
            if (localearray[ inx ] == locale)
            {
                return true;
            }
        }
    }

    return false;
}

WebLibStrings.loadLocale = function()
{
    WebLibStrings.locale = WebAppUtility.getLocale();
    var localestrings = "strings." + WebLibStrings.locale + ".json";

    //
    // Load weblib default strings as initial set.
    //

    WebLibStrings.strings = JSON.parse(WebAppRequest.loadSync("/weblibs/strings.json"));

    //
    // Load weblib locale strings if present.
    //

    if (WebLibStrings.findLocale(WebLibStrings.webLibLocale, localestrings))
    {
        var liblocale = JSON.parse(WebAppRequest.loadSync("/weblibs/" + localestrings));
        for (var key in liblocale) WebLibStrings.strings[ key ] = liblocale[ key ];
    }

    //
    // Load webapp default.
    //

    var appdefault = JSON.parse(WebAppRequest.loadSync("strings.json"));
    for (var key in appdefault) WebLibStrings.strings[ key ] = appdefault[ key ];

    if (WebLibStrings.findLocale(WebApp.manifest.locale, localestrings))
    {
        var applocale = JSON.parse(WebAppRequest.loadSync(localestrings));
        for (var key in applocale) WebLibStrings.strings[ key ] = applocale[ key ];
    }
}

WebLibStrings.getTrans = function(key)
{
    return WebLibStrings.strings[ key ];
}

WebLibStrings.getTransMap = function(key)
{
    if (! key.endsWith(".keys")) return null;

    var keyskey = key;
    var valskey = key.substring(0, key.length - 5) + ".vals";

    if (! (WebLibStrings.strings[ keyskey ] || WebLibStrings.strings[ keyskey ])) return null;

    var map = {};

    for (var inx = 0; inx < WebLibStrings.strings[ keyskey ].length; inx++)
    {
        map[ WebLibStrings.strings[ keyskey ][ inx ] ] = WebLibStrings.strings[ valskey ][ inx ];
    }

    return map;
}

WebLibStrings.getTransTrans = function(key, keyval)
{
    var map = WebLibStrings.getTransMap(key);

    return map ? map[ keyval ] : keyval;
}

WebLibStrings.loadLocale();