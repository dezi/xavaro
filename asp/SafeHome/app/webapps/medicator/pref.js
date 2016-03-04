medicator.medis = WebAppRequest.loadSync("medi.csv");

medicator.buildAllPreferences = function()
{
    //WebAppPrefs.removeAllPrefs("");

    medicator.currentprefs = JSON.parse(WebAppPrefs.getAllPrefs());

    medicator.cleanupSearchPrefs();

    medicator.prefs = [];

    var pref = {};
    pref.key = "select";
    pref.type = "search";
    pref.title = "Medikation hinzufügen";
    medicator.prefs.push(pref);

    var pref = {};
    pref.key = "activity.bloodpressure";
    pref.type = "switch";
    pref.title = "Blutdruck messen";
    medicator.prefs.push(pref);

    var pref = {};
    pref.key = "activity.glucose";
    pref.type = "switch";
    pref.title = "Blutzucker messen";
    medicator.prefs.push(pref);

    var pref = {};
    pref.key = "activity.weight";
    pref.type = "switch";
    pref.title = "Wiegen";
    medicator.prefs.push(pref);

    //
    // Add activity items.
    //

    for (var key in medicator.currentprefs)
    {
        if (key.startsWith("medication.enable.") && key.endsWith("ZZB"))
        {
            var medication = key.substring(18);
            medicator.buildMedication(medication);
        }
    }

    for (var key in medicator.currentprefs)
    {
        if (key.startsWith("medication.enable.") && key.endsWith("ZZG"))
        {
            var medication = key.substring(18);
            medicator.buildMedication(medication);
        }
    }

    for (var key in medicator.currentprefs)
    {
        if (key.startsWith("medication.enable.") && key.endsWith("ZZW"))
        {
            var medication = key.substring(18);
            medicator.buildMedication(medication);
        }
    }

    //
    // Add medication items.
    //

    for (var key in medicator.currentprefs)
    {
        if (key.startsWith("medication.enable.") &&
            ! (key.endsWith("ZZB") || key.endsWith("ZZG") || key.endsWith("ZZW")))
        {
            var medication = key.substring(18);
            medicator.buildMedication(medication);
        }
    }

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

medicator.getActivityText = function(medication, which)
{
    var mediform = medication.substring(medication.length - 3);

    var key = "medication.mode.pills.keys";

    if ((mediform == "ZZB") || (mediform == "ZZG") || (mediform == "ZZW"))
    {
        key = "medication.mode.measure.keys";
    }

    return WebLibStrings.getTransTrans(key, which);
}

medicator.buildTimePref = function(medication, whichtime, enabled)
{
    var pref = {};
    pref.key = "medication." + whichtime + "." + medication;
    pref.type = "number",
    pref.title = medicator.getActivityText(medication, whichtime);
    pref.min = 0;
    pref.max = 24;
    pref.step = 1;
    pref.unit = "Uhr";
    pref.enabled = enabled;

    medicator.prefs.push(pref);

    return pref;
}

medicator.buildDosisPref = function(medication, whichtime, whichdose, enabled)
{
    var mediform = medication.substring(medication.length - 3);
    if ((mediform == "ZZB") || (mediform == "ZZG") || (mediform == "ZZW")) return;

    var dosetext = (whichdose == "ondemand") ? "tägliche Höchstdosis" : "Dosis";

    var pref = {};
    pref.key = "medication." + whichdose + "." + medication;
    pref.type = "list";
    pref.title = medicator.getActivityText(medication, whichtime) + " " + dosetext;
    pref.defvalue = "1";
    pref.enabled = enabled;

    pref.keys = [ "1/4", "1/3", "1/2", "1", "1 1/2", "2", "2 1/2", "3", "4" ];
    pref.vals = [ "1/4", "1/3", "1/2", "1", "1 1/2", "2", "2 1/2", "3", "4" ];

    medicator.prefs.push(pref);

    return pref;
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
    pref.title = mname;
    pref.summary = dlang;
    medicator.prefs.push(pref);

    WebAppPrefs.setPrefBoolean(pref.key, true);

    //
    // Medication duration.
    //

    var pref = {};
    pref.key = "medication.dura." + medication;
    pref.type = "list",
    pref.title = "Zeitraum";
    pref.defvalue = "ongoing";

    pref.keys = [ "ongoing",        "numdays",  "paused"   ];
    pref.vals = [ "Kontinuierlich", "Begrenzt", "Pausiert" ];

    medicator.prefs.push(pref);

    var dura = WebAppPrefs.getPrefString(pref.key);
    if (! dura) dura = pref.defvalue;

    var enabled = (dura != "paused");

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
        pref.enabled = enabled;

        medicator.prefs.push(pref);
    }

    //
    // Start date of medication.
    //

    var pref = {};
    pref.key = "medication.date." + medication;
    pref.type = "date",
    pref.title = "Startdatum";
    pref.enabled = enabled;

    medicator.prefs.push(pref);

    //
    // Special days selection.
    //

    var pref = {};
    pref.key = "medication.days." + medication;
    pref.type = "list",
    pref.title = WebLibStrings.getTrans("medication.days");
    pref.defvalue = "daily";
    pref.enabled = enabled;

    pref.keys = WebLibStrings.getTrans("medication.days.keys");
    pref.vals = WebLibStrings.getTrans("medication.days.vals");

    medicator.prefs.push(pref);

    var days = WebAppPrefs.getPrefString(pref.key);
    if (! days) days = pref.defvalue

    if (days == "weekdays")
    {
        var pref = {};
        pref.key = "medication.wdays." + medication;
        pref.type = "multi";
        pref.title = "Wochentage";
        pref.enabled = enabled;

        pref.keys = [ "1",  "2",  "3",  "4",  "5",  "6",  "7"  ];
        pref.vals = [ "Mo", "Di", "Mi", "Do", "Fr", "Sa", "So" ];

        medicator.prefs.push(pref);
   }

    if (days == "interval")
    {
        var pref = {};
        pref.key = "medication.idays." + medication;
        pref.type = "list";
        pref.title = "Intervall";
        pref.enabled = enabled;

        pref.keys = [
            "2",    "3",   "4",
            "5",    "6",   "7",
            "8",    "9",  "10",
            "11",  "12",  "13",
            "14"
            ];

        pref.vals = [
            "Alle 2 Tage",  "Alle 3 Tage",  "Alle 4 Tage",
            "Alle 5 Tage",  "Alle 6 Tage",  "Alle 7 Tage",
            "Alle 8 Tage",  "Alle 9 Tage",  "Alle 10 Tage",
            "Alle 11 Tage", "Alle 12 Tage", "Alle 13 Tage",
            "Alle 14 Tage"
            ];

        medicator.prefs.push(pref);
    }

    //
    // Medication daily time and dose plan.
    //

    if (days == "ondemand")
    {
        medicator.buildDosisPref(medication, "ondemand", "ondemand");
    }

    if (days != "ondemand")
    {
        var pref = {};
        pref.key = "medication.times." + medication;
        pref.type = "list",
        pref.title = "Zeiten";
        pref.defvalue = "daily1";
        pref.enabled = enabled;

        pref.keys = [
            "daily1", "daily2", "daily3", "daily4",
            "every6", "every8", "every12"
            ];

        pref.vals = [
            "1x pro Tag", "2x pro Tag", "3x pro Tag", "4x pro Tag",
            "Alle 6 Stunden", "Alle 8 Stunden", "Alle 12 Stunden"
            ];

        medicator.prefs.push(pref);

        var times = WebAppPrefs.getPrefString(pref.key);
        if (! times) times = pref.defvalue;

        if ((times == "daily1") || (times == "daily2") || (times == "daily3") || (times == "daily4"))
        {
            var pref = medicator.buildTimePref(medication, "time1", enabled);
            pref.defvalue = (times == "daily4") ? 6 : 8;
            medicator.buildDosisPref(medication, "time1", "dose1", enabled);
        }

        if ((times == "daily2") || (times == "daily3") || (times == "daily4"))
        {
            var pref = medicator.buildTimePref(medication, "time2", enabled);
            pref.defvalue = (times == "daily4") ? 12 : (times == "daily3") ? 16 : 20;
            medicator.buildDosisPref(medication, "time2", "dose2", enabled);
        }

        if ((times == "daily3") || (times == "daily4"))
        {
            var pref = medicator.buildTimePref(medication, "time3", enabled);
            pref.defvalue = (times == "daily4") ? 18 : 22;
            medicator.buildDosisPref(medication, "time3", "dose3", enabled);
        }

        if (times == "daily4")
        {
            var pref = medicator.buildTimePref(medication, "time4", enabled);
            pref.defvalue = 0;
            medicator.buildDosisPref(medication, "time4", "dose4", enabled);
        }

        if ((times == "every6") || (times == "every8") || (times == "every12"))
        {
            var pref = medicator.buildTimePref(medication, "time1", enabled);
            pref.defvalue = (times == "every6") ? 6 : 8;
            medicator.prefs.push(pref);
            medicator.buildDosisPref(medication, "time1", "dose1", enabled);
        }

        if ((times == "every6") || (times == "every8") || (times == "every12"))
        {
            var pref = medicator.buildTimePref(medication, "time2", enabled);
            pref.defvalue = (times == "every6") ? 12 : (times == "every8") ? 16 : 20;
            medicator.buildDosisPref(medication, "time2", "dose2", enabled);
        }

        if ((times == "every6") || (times == "every8"))
        {
            var pref = medicator.buildTimePref(medication, "time3", enabled);
            pref.defvalue = (times == "every6") ? 18 : 24;
            medicator.buildDosisPref(medication, "time3", "dose3", enabled);
        }

        if (times == "every6")
        {
            var pref = medicator.buildTimePref(medication, "time4", enabled);
            pref.defvalue = 0;
            medicator.buildDosisPref(medication, "time4", "dose4", enabled);
        }
    }
}

WebAppPrefBuilder.onPreferenceChanged = function(prefkey)
{
    console.log("=========================>" + prefkey);

    if (prefkey.startsWith("activity."))
    {
        var medication = null;

        if (prefkey == "activity.bloodpressure") medication = "Blutdruck messen,ZZB"
        if (prefkey == "activity.glucose") medication = "Blutzucker messen,ZZG"
        if (prefkey == "activity.weight") medication = "Wiegen,ZZW"

        if (medication)
        {
            var key = "medication.enable." + medication;

            if (WebAppPrefs.getPrefBoolean(prefkey))
            {
                 WebAppPrefs.setPrefBoolean(key, true);
            }
            else
            {
                WebAppPrefs.removePref(key);
            }

            medicator.buildAllPreferences();

            return;
        }
    }

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

    if (prefkey.startsWith("medication.times."))
    {
        var medication = prefkey.substring(16);

        WebAppPrefs.removePref("medication.time1." + medication);
        WebAppPrefs.removePref("medication.time2." + medication);
        WebAppPrefs.removePref("medication.time3." + medication);
        WebAppPrefs.removePref("medication.time4." + medication);
    }

    medicator.buildAllPreferences();
}

WebAppPrefBuilder.onSearchCancel = function(prefkey)
{
    medicator.removeLastSearch();

    WebAppPrefBuilder.updatePreferences(JSON.stringify(medicator.prefs));
}

WebAppPrefBuilder.onSearchRequest = function(prefkey, query)
{
    medicator.removeLastSearch();

    var targetinx = 0;

    for (var inx = 0; inx < medicator.prefs.length; inx++)
    {
        if (medicator.prefs[ inx ].key == "select")
        {
            targetinx = inx;
            break;
        }
    }

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
        medicator.prefs.splice(++targetinx, 0, pref);

        if (inx > 100) break;
    }

    WebAppPrefBuilder.updatePreferences(JSON.stringify(medicator.prefs));
}

medicator.buildAllPreferences();