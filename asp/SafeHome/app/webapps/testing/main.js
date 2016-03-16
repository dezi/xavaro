//
// Testing webapp.
//

testing.pre = WebLibSimple.createAnyAppend("pre", document.body);


testing.pre.innerHTML = WebAppMedia.getLocaleDefaultChannels("tv");
testing.pre.innerHTML = WebAppMedia.getLocaleInternetChannels("tv");
testing.pre.innerHTML = WebAppMedia.getRecordedItems();

//WebAppMedia.openPlayer("/storage/emulated/0/Movies/Recordings/2016-03-15T15-10-00Z.tv.de.ARTE Deutsch.mp4");



testing.moroned = "Spielfilem 2015Hallo bla der BesteDeutschland.Der große ReportÜberflieger sind besser.";

testing.deMoronize = function(moroned)
{
    var demoronized = moroned;

    var regex = new RegExp("([a-z0-9äüöß])([A-ZÉÄÜÖ])", "g");

    demoronized = demoronized.replace(regex,"$1@\n$2");

    return demoronized;
}

testing.demoronized = testing.deMoronize(testing.moroned);

console.log(testing.moroned);
console.log(testing.demoronized);
