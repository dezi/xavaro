//
// Country library.
//

WebLibCountry = {};

WebLibCountry.findLocale = function(localearray, locale)
{
    if (localearray)
    {
        for (var inx = 0; inx < localearray.length; inx++)
        {
            if (locale.startsWith(localearray[ inx ]))
            {
                return true;
            }
        }
    }

    return false;
}

WebLibCountry.loadLocale = function()
{
    WebLibCountry.locale = WebAppUtility.getLocale();
    var localestrings = "country." + WebLibCountry.locale + ".json";

    //
    // Load weblib default strings as initial set.
    //

    WebLibCountry.country = JSON.parse(WebAppRequest.loadSync("/weblibs/country.json"));

    //
    // Load weblib locale strings if present.
    //

console.log("=============" + localestrings);

    if (WebLibCountry.findLocale(WebLibCountry.country.locales, localestrings))
    {
        console.log("============= fund");

        var liblocale = JSON.parse(WebAppRequest.loadSync("/weblibs/" + localestrings));
        for (var key in liblocale) WebLibCountry.country[ key ] = liblocale[ key ];
    }
}

WebLibCountry.getCountry = function(isocc)
{
    return WebLibCountry.country.isocc[ isocc.toUpperCase() ];
}

WebLibCountry.loadLocale();