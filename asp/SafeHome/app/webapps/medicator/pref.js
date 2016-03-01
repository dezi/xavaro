medicator.medis = WebAppRequest.loadSync("medi.csv");

medicator.buildBasicPreference = function()
{
    medicator.prefs = [];

    var pref = {};
    pref.key = "select";
    pref.type = "search";
    pref.title = "Medikation hinzufügen";
    medicator.prefs.push(pref);

    WebAppPrefBuilder.updatePreferences(JSON.stringify(medicator.prefs));
}

medicator.removeLastSearch = function()
{
    for (var inx = 0; inx < medicator.prefs.length; inx++)
    {
        if (medicator.prefs[ inx ].search)
        {
            medicator.prefs.splice(inx--, 1);
        }
    }
}

WebAppPrefBuilder.onSearchRequest = function(prefkey, query)
{
    console.log("WebAppPrefBuilder.onSearchRequest:" + prefkey + "=" + query);

    medicator.removeLastSearch();

    var regex = new RegExp(".*" + query + ".*", "gi");
    var results = medicator.medis.match(regex);

    if (results == null)
    {
        return;
    }

    for (var inx = 0; inx < results.length; inx++)
    {
        var result = results[ inx ];

        var mname = result.substring(0,result.length - 4);
        var dkurz = result.substring(result.length - 3);
        var dlang = medicator.form[ dkurz ];

        var pref = {};
        pref.key = "search.result." + result;
        pref.type = "check";
        pref.title = mname;
        pref.summary = dlang;
        pref.search = true;
        medicator.prefs.push(pref);
    }

    WebAppPrefBuilder.updatePreferences(JSON.stringify(medicator.prefs));
}

medicator.buildBasicPreference();