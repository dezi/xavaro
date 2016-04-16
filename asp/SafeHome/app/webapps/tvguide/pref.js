WebAppPrefBuilder.onPreferenceChanged = function(prefkey)
{
    console.log("=========================>" + prefkey);
}

tvguide.buildBasicPreference = function()
{
//    WebAppPrefs.removeAllPrefs("");

    tvguide.prefs = [];

    var StringsKeys = WebLibStrings.strings[ "tvguide.senderlists.keys" ];
    var StringsVals = WebLibStrings.strings[ "tvguide.senderlists.vals" ];

    for (var key in StringsKeys)
    {
        var category = StringsKeys[ key ];
        var name     = StringsVals[ key ];

        var pref = {};
        pref.key = "cat." + category;
        pref.type = "category";
        pref.title = name;
        tvguide.prefs.push(pref);

        var channels = "tvguide.list." + category;
        var channelList = WebLibStrings.strings[ channels ];

        for (var channel in channelList)
        {
            channel = channelList[ channel ];

            var pref = {};
            pref.key = "channel." + channel;
            pref.type = "check";
            pref.title = channel;
            pref.icon = encodeURI("http://" + WebApp.manifest.appserver + "/channels/" + channel + ".png");
            pref.defvalue = false;

            if (category == "default") pref.defvalue = true;

            tvguide.prefs.push(pref);
        }
    }

    WebAppPrefBuilder.updatePreferences(JSON.stringify(tvguide.prefs));
}

tvguide.buildBasicPreferenceSample = function()
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

//    WebAppPrefBuilder.updatePreferences(JSON.stringify(tvguide.prefs));
}

tvguide.buildBasicPreference();