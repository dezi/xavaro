console.log("alive........ locale=" + WebAppUtility.getLocaleCountry());

tvguide.buildBasicPreference = function()
{
    tvguide.prefs = [];

    var pref = {};
    pref.key = "cat.tv",
    pref.type = "category",
    pref.title = "Fernsehsender";
    tvguide.prefs.push(pref);

    var pref = {};
    pref.key = "tv",
    pref.type = "switch",
    pref.title = "Aktiviert";
    pref.defvalue = true;
    tvguide.prefs.push(pref);

    var pref = {};
    pref.key = "cat.rd",
    pref.type = "category",
    pref.title = "Radiosender";
    tvguide.prefs.push(pref);

    var pref = {};
    pref.key = "rd",
    pref.type = "switch",
    pref.title = "Aktiviert";
    pref.defvalue = true;
    tvguide.prefs.push(pref);

    WebAppPrefBuilder.addPreferences(JSON.stringify(tvguide.prefs));
}

tvguide.buildBasicPreference();