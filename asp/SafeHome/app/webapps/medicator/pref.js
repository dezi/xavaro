medicator.medis = WebAppRequest.loadSync("medi.csv");

medicator.buildBasicPreference = function()
{
    medicator.extprefs = WebAppPrefs.getAllPrefs();

    medicator.cleanupSearchPrefs();

    console.log(medicator.extprefs);

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

    medicator.cleanupSearchPrefs();
}

medicator.cleanupSearchPrefs = function()
{
    WebAppPrefs.removeAllPrefs("search.result.");
}

medicator.buildMedication = function(prefkey)
{
    var medication = prefkey.substring(14);
    var mname = medication.substring(0,medication.length - 4);
    var dkurz = medication.substring(medication.length - 3);
    var dlang = medicator.form[ dkurz ];

    //
    // Medication category header. The header will not store
    // a preference with its key. So we do it manually.
    //

    var pref = {};
    pref.key = "medication.enable." + medication;
    pref.type = "category";
    pref.title = mname + "\n" + dlang;
    medicator.prefs.push(pref);

    WebAppPrefs.setPrefBoolean(pref.key, true);

    //
    // Other medication preferences.
    //

    var pref = {};
    pref.key = "medication.time." + medication;
    pref.type = "list",
    pref.title = "Einnahmezeiten";
    pref.defvalue = "daily1";

    pref.keys = [
        "daily1", "daily2", "daily3",
        "every6", "every8", "every12",
        "weekly", "monthly"
        ];

    pref.vals = [
        "Täglich 1x", "Täglich 2x", "Täglich 3x",
        "Alle 6 Stunden", "Alle 8 Stunden", "Alle 12 Stunden",
        "Wöchentlich", "Monatlich"
        ];

    medicator.prefs.push(pref);

    var pref = {};
    pref.key = "medication.dura." + medication;
    pref.type = "list",
    pref.title = "Einnahmedauer";
    pref.defvalue = "ongoing";

    pref.keys = [ "ongoing",        "numdays" ];
    pref.vals = [ "Kontinuierlich", "Anzahl der Tage" ];

    medicator.prefs.push(pref);

    var pref = {};
    pref.key = "medication.days." + medication;
    pref.type = "list",
    pref.title = "Einnahmetage";
    pref.defvalue = "every";

    pref.keys = [ "every",   "weekdays",             "intervall"      ];
    pref.vals = [ "Täglich", "Bestimmte Wochentage", "Tagesintervall" ];

    medicator.prefs.push(pref);

    WebAppPrefBuilder.updatePreferences(JSON.stringify(medicator.prefs));
}

WebAppPrefBuilder.onPreferenceChanged = function(prefkey)
{
    if (prefkey.startsWith("search.result."))
    {
        var enabled = WebAppPrefs.getPrefBoolean(prefkey);

        medicator.removeLastSearch();

        if (enabled) medicator.buildMedication(prefkey);
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

        if (inx > 100) break;
    }

    WebAppPrefBuilder.updatePreferences(JSON.stringify(medicator.prefs));
}

medicator.buildBasicPreference();