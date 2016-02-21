console.log("==================main.js loaded...");

//testing.alertest();

console.log("==================" + extras.loadSync("http://epg.xavaro.de/channels/tv/de/ZDF.json"));
console.log("==================" + extras.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz"));

