medicator.medis = WebAppRequest.loadSync("medi.csv");

medicator.buildAllPreferences = function()
{
    //WebAppPrefs.removeAllPrefs("");

    medicator.currentprefs = JSON.parse(WebAppPrefs.getAllPrefs());

    medicator.cleanupSearchPrefs();

    medicator.prefs = [];

    for (var key in medicator.currentprefs)
    {
        if (key.startsWith("medication.enable."))
        {
            var medication = key.substring(18);
            medicator.buildMedication(medication);
        }
    }

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

medicator.buildDosisPref = function(medication, which)
{
    var pref = {};
    pref.key = "medication." + which + "." + medication;
    pref.type = "list";
    pref.title = "Dosis";
    pref.defvalue = "1";

    pref.keys = [ "1", "2", "3", "4", "1/2", "1/3", "1/4" ];
    pref.vals = [ "1", "2", "3", "4", "1/2", "1/3", "1/4" ];

    medicator.prefs.push(pref);
}

medicator.buildMedication = function(medication)
{
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
    // Medication duration ongoing or number of days.
    //

    var pref = {};
    pref.key = "medication.dura." + medication;
    pref.type = "list",
    pref.title = "Einnahmedauer";
    pref.defvalue = "ongoing";

    pref.keys = [ "ongoing",        "numdays" ];
    pref.vals = [ "Kontinuierlich", "Begrenzt" ];

    medicator.prefs.push(pref);

    var dura = WebAppPrefs.getPrefString(pref.key);

    if (dura == "numdays")
    {
        var pref = {};
        pref.key = "medication.numdays." + medication;

        pref.type = "number",
        pref.title = "Anzahl der Tage";
        pref.min = 1;
        pref.max = 90;
        pref.step = 1;
        pref.defvalue = 14;
        pref.unit = "Tage";

        medicator.prefs.push(pref);
    }

    //
    // Medication daily time and dose plan.
    //

    var pref = {};
    pref.key = "medication.time." + medication;
    pref.type = "list",
    pref.title = "Einnahmezeiten";
    pref.defvalue = "daily1";

    pref.keys = [
        "daily1", "daily2", "daily3", "daily4",
        "every6", "every8", "every12",
        "weekly", "monthly"
        ];

    pref.vals = [
        "Täglich 1x", "Täglich 2x", "Täglich 3x", "Täglich 4x",
        "Alle 6 Stunden", "Alle 8 Stunden", "Alle 12 Stunden",
        "Wöchentlich", "Monatlich"
        ];

    medicator.prefs.push(pref);

    var time = WebAppPrefs.getPrefString(pref.key);

    if ((time == "daily1") || (time == "daily2") || (time == "daily3") || (time == "daily4"))
    {
        var pref = {};
        pref.key = "medication.daily1." + medication;
        pref.type = "number",
        pref.title = "Erste Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = (time == "daily4") ? 6 : 8;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose1");
    }

    if ((time == "daily2") || (time == "daily3") || (time == "daily4"))
    {
        var pref = {};
        pref.key = "medication.daily2." + medication;
        pref.type = "number",
        pref.title = "Zweite Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = (time == "daily4") ? 12 : (time == "daily3") ? 16 : 20;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose2");
   }

    if ((time == "daily3") || (time == "daily4"))
    {
        var pref = {};
        pref.key = "medication.daily3." + medication;
        pref.type = "number",
        pref.title = "Dritte Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = (time == "daily4") ? 18 : 0;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose3");
    }

    if (time == "daily4")
    {
        var pref = {};
        pref.key = "medication.daily4." + medication;
        pref.type = "number",
        pref.title = "Vierte Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = 0;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose4");
    }

    if ((time == "every6") || (time == "every8") || (time == "every12"))
    {
        var pref = {};
        pref.key = "medication.daily1." + medication;
        pref.type = "number",
        pref.title = "Erste Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = (time == "every6") ? 6 : 8;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose1");
    }

    if ((time == "every6") || (time == "every8") || (time == "every12"))
    {
        var pref = {};
        pref.key = "medication.daily2." + medication;
        pref.type = "number",
        pref.title = "Zweite Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = (time == "every6") ? 12 : (time == "every8") ? 16 : 20;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose2");
    }

    if ((time == "every6") || (time == "every8"))
    {
        var pref = {};
        pref.key = "medication.daily3." + medication;
        pref.type = "number",
        pref.title = "Dritte Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = (time == "every6") ? 18 : 0;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose3");
   }

    if (time == "every6")
    {
        var pref = {};
        pref.key = "medication.daily4." + medication;
        pref.type = "number",
        pref.title = "Vierte Einnahme";
        pref.min = 0;
        pref.max = 24;
        pref.step = 1;
        pref.defvalue = 0;
        pref.unit = "Uhr";

        medicator.prefs.push(pref);
        medicator.buildDosisPref(medication, "dose4");
   }

    if (time == "weekly")
    {
        medicator.buildDosisPref(medication, "doseweekly");
    }

    if (time == "monthly")
    {
        medicator.buildDosisPref(medication, "dosemonthly");
    }

    //
    // Special days selection.
    //

    var pref = {};
    pref.key = "medication.days." + medication;
    pref.type = "list",
    pref.title = "Einnahmetage";
    pref.defvalue = "every";

    pref.keys = [ "every",   "weekdays",             "intervall"      ];
    pref.vals = [ "Täglich", "Bestimmte Wochentage", "Tagesintervall" ];

    medicator.prefs.push(pref);

    var days = WebAppPrefs.getPrefString(pref.key);

    if (days == "weekdays")
    {
        var pref = {};
        pref.key = "medication.wdays." + medication;
        pref.type = "multi";
        pref.title = "Wochentage";

        pref.keys = [ "1",  "2",  "3",  "4",  "5",  "6",  "7"  ];
        pref.vals = [ "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So" ];

        medicator.prefs.push(pref);
   }

    if (days == "intervall")
    {
        var pref = {};
        pref.key = "medication.idays." + medication;
        pref.type = "multi";
        pref.title = "Intervall";

        pref.keys = [
            "2",    "3",   "4",
            "5",    "6",   "7",
            "8",    "9",  "10",
            "11",  "12",  "13",
            "14"
            ];

        pref.vals = [
             "2. Tag",  "3. Tag",  "4. Tag",
             "5. Tag",  "6. Tag",  "7. Tag",
             "8. Tag",  "9. Tag", "10. Tag",
            "11. Tag", "12. Tag", "13. Tag",
            "14. Tag"
            ];

        medicator.prefs.push(pref);
    }

    WebAppPrefBuilder.updatePreferences(JSON.stringify(medicator.prefs));
}

WebAppPrefBuilder.onPreferenceChanged = function(prefkey)
{
    if (prefkey.startsWith("search.result."))
    {
        var enabled = WebAppPrefs.getPrefBoolean(prefkey);
        var medication = prefkey.substring(14);

        medicator.removeLastSearch();

        if (enabled)
        {
            var key = "medication.enable." + medication;
            WebAppPrefs.setPrefBoolean(key, true);
            medicator.buildAllPreferences();
        }

        return;
    }

    if (prefkey.startsWith("medication.time."))
    {
        var medication = prefkey.substring(16);

        WebAppPrefs.removePref("medication.daily1." + medication);
        WebAppPrefs.removePref("medication.daily2." + medication);
        WebAppPrefs.removePref("medication.daily3." + medication);
        WebAppPrefs.removePref("medication.daily4." + medication);
    }

    medicator.buildAllPreferences();
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

medicator.buildAllPreferences();