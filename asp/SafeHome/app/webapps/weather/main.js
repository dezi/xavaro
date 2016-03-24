
//var info = WebAppWeather.getForecast("02821085");
var info = WebAppWeather.getForecast16("02821085");

weather.pre = WebLibSimple.createAnyAppend("pre", document.body);
weather.pre.innerHTML = WebAppUtility.getPrettyJson(info);

var results = WebAppWeather.getQuery("Bad ");

weather.pre.innerHTML = WebAppUtility.getPrettyJson(results) + "\n" + weather.pre.innerHTML;


