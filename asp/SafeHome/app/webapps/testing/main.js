//
// Testing webapp.
//

testing.pre = WebLibSimple.createAnyAppend("pre", document.body);


testing.pre.innerHTML = WebAppMedia.getRecordedItems();
testing.pre.innerHTML = WebAppMedia.getLocaleDefaultChannels("tv");
testing.pre.innerHTML = WebAppMedia.getLocaleInternetChannels("tv");
