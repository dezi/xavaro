medicator.medis = WebAppRequest.loadSync("medi.csv");

medicator.buildBasicPreference = function()
{
    medicator.prefs = [];

    var pref = {};
    pref.key = "select",
    pref.type = "search",
    pref.title = "Medikation hinzufügen";
    medicator.prefs.push(pref);

    WebAppPrefBuilder.addPreferences(JSON.stringify(medicator.prefs));
}

WebAppPrefBuilder.onSearchRequest = function(prefkey, query)
{
    console.log("WebAppPrefBuilder.onSearchRequest:" + prefkey + "=" + query);

    var result = medicator.medis.match(/Nebo.*/gi);

    for (var inx = 0; inx < result.length; inx++)
    {
        console.log(result[ inx ]);
    }
}

medicator.buildBasicPreference();