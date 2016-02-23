console.log("==================main.js loaded...");

//testing.alertest();

//console.log("==================" + extras.loadSync("http://epg.xavaro.de/channels/tv/de/ZDF.json"));

//testing.channels = extras.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz");
//console.log("==================" + testing.channels.length);

testing.ZDF = WebAppRequest.loadSync("http://epg.xavaro.de/epgdata/tv/de/ZDF/current.json.gz");

var pre = document.createElement("pre");
document.body.appendChild(pre);
pre.innerHTML = testing.ZDF;

json = { a:23, b:24, c:15 };

WebAppRequest.callback("hallo");
WebAppRequest.jsonfunz("pipa");

