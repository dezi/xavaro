//
// API Test...
//

weather.pre = WebLibSimple.createAnyAppend("pre", document.body);

var info = WebAppWeather.getForecast("02953523");
weather.pre.innerHTML = WebAppUtility.getPrettyJson(info);

var info = WebAppWeather.getForecast16("02953523");
weather.pre.innerHTML = WebAppUtility.getPrettyJson(info) + "\n" + weather.pre.innerHTML;

var results = WebAppWeather.getQuery("Hamb");
weather.pre.innerHTML = WebAppUtility.getPrettyJson(results) + "\n" + weather.pre.innerHTML;


