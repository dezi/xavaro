//
// Testing webapp.
//

testing.recordings = WebAppMedia.getRecordedItems();
testing.pre = WebLibSimple.createAnyAppend("pre", document.body);
testing.pre.innerHTML = testing.recordings;