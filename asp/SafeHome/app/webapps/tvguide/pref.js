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

    var pref = {};
    pref.key = "rdcheck",
    pref.type = "check",
    pref.title = "Checkbox";
    pref.defvalue = true;
    tvguide.prefs.push(pref);

    var pref = {};
    pref.key = "rdedit",
    pref.type = "edit",
    pref.title = "Edit";
    pref.defvalue = "blaselfasel";
    tvguide.prefs.push(pref);

    var pref = {};
    pref.key = "rdlist",
    pref.type = "list",
    pref.title = "List";
    pref.defvalue = "sd";
    pref.keys = [ "sd", "hd", "free",    "pay",    "domestic", "foreign", "sky" ];
    pref.vals = [ "SD", "HD", "Free-TV", "Pay-TV", "Inland",   "Ausland", "Sky" ];
    tvguide.prefs.push(pref);

    var pref = {};
    pref.key = "rdxxx",
    pref.type = "multi",
    pref.title = "Select";
    pref.defvalue = [ "sd", "free", "domestic" ];
    pref.keys = [ "sd", "hd", "free",    "pay",    "domestic", "foreign", "sky", "dvb-t",   "dvb-c", "dvb-s"    ];
    pref.vals = [ "SD", "HD", "Free-TV", "Pay-TV", "Inland",   "Ausland", "Sky", "Antenne", "Kabel", "Satellit" ];
    tvguide.prefs.push(pref);

    WebAppPrefBuilder.addPreferences(JSON.stringify(tvguide.prefs));
}

tvguide.buildBasicPreference();