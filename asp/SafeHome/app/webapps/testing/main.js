//
// Testing webapp.
//

testing.pre = WebLibSimple.createAnyAppend("pre", document.body);


testing.pre.innerHTML = WebAppMedia.getLocaleDefaultChannels("tv");
testing.pre.innerHTML = WebAppMedia.getLocaleInternetChannels("tv");
testing.pre.innerHTML = WebAppMedia.getRecordedItems();

WebAppMedia.openPlayer("/storage/emulated/0/Movies/Recordings/2016-03-15T15-10-00Z.tv.de.ARTE Deutsch.mp4");
