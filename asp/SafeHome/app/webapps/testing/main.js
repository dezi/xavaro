//
// Testing webapp.
//

testing.pre = WebLibSimple.createAnyAppend("pre", document.body);


testing.pre.innerHTML = WebAppMedia.getLocaleInternetChannels("tv");
testing.pre.innerHTML = WebAppMedia.getRecordedItems();
testing.pre.innerHTML = WebAppMedia.getLocaleDefaultChannels("tv");

//WebAppMedia.openPlayer("/storage/emulated/0/Movies/Recordings/2016-03-15T15-10-00Z.tv.de.ARTE Deutsch.mp4");


testing.locale = JSON.parse(WebAppMedia.getLocaleInternetChannels("tv"));
testing.channels = JSON.parse(WebAppRequest.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz"));

testing.check = function(array, tag)
{
    for (var inx = 0; inx < array.length; inx++)
    {
        if (tag == array[ inx ]) return true;
    }

    return false;
}

for (var inx = 0; inx < testing.channels.length; inx++)
{
    var ccc = testing.channels[ inx ];
    var tag = "tv/" + ccc.isocc + "/" + ccc.name;

    if (testing.check(testing.locale, tag)) continue;

    if (ccc.name.startsWith("Sky ")) console.log(tag);
}