console.log("==================main.js loaded...");

//testing.alertest();

console.log("==================" + extras.loadSync("http://epg.xavaro.de/channels/tv/de/ZDF.json"));

testing.channels = extras.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz");
testing.ZDF = extras.loadSync("http://epg.xavaro.de/epgdata/tv/de/ZDF/current.json.gz");

console.log("==================" + testing.channels.length);

var pre = document.createElement("pre");
document.body.appendChild(pre);
pre.innerHTML = testing.channels + testing.ZDF;
