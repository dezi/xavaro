DeMoronize = {};

DeMoronize.cleanEpg = function(moronedEpg)
{
    if (! moronedEpg.description) return moronedEpg;

    var demoronized = moronedEpg.description;

    // console.log("++> " + demoronized);

    var regex = new RegExp("(.*?[12][0-9][0-9][0-9])([A-ZÉÄÜÖ])", "g");

    var match = demoronized.match(regex);

    if (match)
    {
        var match = match[ 0 ];
        var result = WebLibSimple.substring(match, 0, -1);

        if (moronedEpg.subtitle)
        {
            moronedEpg.subtitle = moronedEpg.subtitle + " " + result;
        }
    }

    demoronized = demoronized.replace(regex, "$1<br />$2");

    var regex   = new RegExp("([a-zA-ZÄÜÖäüöß])[.]([A-ZÄÜÖ])", "g");
    demoronized = demoronized.replace(regex, "$1.<br />$2");

    var regex   = new RegExp("([a-zäüöß])([A-ZÉÄÜÖ])", "g");
    demoronized = demoronized.replace(regex, "$1<br />$2");

    var regex   = new RegExp("([A-Z][a-z]*?:)([A-Z])", "g");
    demoronized = demoronized.replace(regex, "<br />$1<br />$2");

    // var regex   = new RegExp("([A-Z][a-z]*?/[A-Z][a-z].*?:)", "g");
    // demoronized = demoronized.replace(regex, "<br />$1");
    //
    // var regex   = new RegExp("([a-z]) - ([A-Z])", "g");
    // demoronized = demoronized.replace(regex, "$1<br />$2");
    //
    // var regex   = new RegExp("[^/]([A-Z][a-z].*?:)", "g");
    // demoronized = demoronized.replace(regex, "@<br />$1");

    var regex   = new RegExp("Produziert in HD", "g");
    demoronized = demoronized.replace(regex, "");

    var regex   = new RegExp("[*]", "g");
    demoronized = demoronized.replace(regex, "");

    // var regex = new RegExp("[\n]", "g");
    // demoronized = demoronized.replace(regex, "**<br />**");

    moronedEpg.description = "DeMoronize:<br /><br />" + demoronized;
    moronedEpg.isbd = false;

    // console.log("--> " + demoronized);
    return moronedEpg;
}

DeMoronize.shiftTime = function(date)
{
    var date         = new Date(date);
    var shift        = 1000 * 60;
    var seconds      = date.getSeconds() * 1000;
    var cleanTime    = date.getTime() - seconds;
    var newDate      = new Date(cleanTime + shift);

    return newDate;
}

DeMoronize.cleanTime = function(moronedEpg)
{
    var epg = moronedEpg;

    epg.start = DeMoronize.shiftTime(epg.start);
    epg.stop  = DeMoronize.shiftTime(epg.stop );

    return epg;
}

DeMoronize.cleanShortShows = function(data)
{
    if (! data) return;

    var data = data;
    var lastBroadcast = null;

    for (var broadcast in data)
    {
        var src = data[ broadcast ];
        var duration = WebLibSimple.getDuration(src.start, src.stop) / 1000 / 60;

        if ((lastBroadcast !== null) && (duration <= 4))
        {
            lastBroadcast.title2       = src.title;
            lastBroadcast.subtitle2    = src.subtitle;
            lastBroadcast.description2 = src.description;
            lastBroadcast.stop         = src.stop;

            data.splice(data.indexOf(src), 1);

        }
        else
        {
            lastBroadcast = src;
        }
    }

    return data;
}
