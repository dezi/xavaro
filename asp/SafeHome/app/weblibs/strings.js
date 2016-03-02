//
// Strings library.
//

WebLibStrings = {};

WebLibStrings.loadLocale = function()
{
    WebLibStrings.locale = WebAppUtility.getLocale();
    WebLibStrings.strings = JSON.parse(WebAppRequest.loadSync("strings.json"));

    var localestrings = "strings." + WebLibStrings.locale + ".json";
    var localefound = false;

    for (var inx = 0; inx < WebApp.manifest.locale.length; inx++)
    {
        if (WebApp.manifest.locale[ inx ] == localestrings)
        {
            localefound = true;
            break;
        }
    }

    if (localefound)
    {
        var temp = JSON.parse(WebAppRequest.loadSync(localestrings));

        for (var key in temp)
        {
            WebLibStrings.strings[ key ] = temp[ key ];
        }
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