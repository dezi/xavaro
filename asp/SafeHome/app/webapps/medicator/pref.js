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

    var regex = new RegExp(".*" + query + ".*", "gi");
    var results = medicator.medis.match(regex);

    if (results == null)
    {
        return;
    }

    console.log("Treffer=" + results.length);

    for (var inx = 0; inx < results.length; inx++)
    {
        var result = results[ inx ];

        var mname = result.substring(0,result.length - 4);
        var dkurz = result.substring(result.length - 3);
        var dlang = medicator.form[ dkurz ];

        console.log(mname + "=" + dlang);
    }
}

medicator.buildBasicPreference();