//
// Medicator main page.
//

WebLibLaunch.createFrame();

medicator.createEvents = function()
{
    medicator.comingEvents = JSON.parse(WebAppEvents.getComingEvents());

    var now = new Date();
    var today = WebLibSimple.getTodayDate().getTime();
    var midni = today + 86400 * 1000;

    today = new Date(today).toISOString();
    midni = new Date(midni).toISOString();

    console.log("today=" + today);
    console.log("midni=" + midni);

    var configs = {};

    for (var inx in medicator.comingEvents)
    {
        var event = medicator.comingEvents[ inx ];

        if ((event.date < today) || (event.date >= midni)) continue;

        var date = new Date(event.date);
        var hour = date.getHours();
        var minute = date.getMinutes();

        var medication = event.medication.substring(0,event.medication.length - 4);
        var mediform = event.medication.substring(event.medication.length - 3);
        var ondemand = event.ondemand ? event.ondemand : null;
        var dose = event.dose ? event.dose : null;

        var label = WebLibSimple.padNum(hour, 2) + ":" + WebLibSimple.padNum(minute, 2);
        if (ondemand) label = "Bei Bedarf";

        var icon = "health_frame_490x490.png";
        if (mediform == "ZZB") icon = "health_bpm_256x256.png";
        if (mediform == "ZZG") icon = "health_glucose_512x512.png";
        if (mediform == "ZZW") icon = "health_scale_280x280.png";

        var overicon = null;

        if (! ondemand)
        {
            if (date.getTime() < (now.getTime() - 2 * 3600 * 1000))
            {
                overicon = "indicator_no_480x480.png";
            }
            else
            if (date.getTime() <  (now.getTime() - 1 * 3600 * 1000))
            {
                overicon = "indicator_alert_300x300.png";
            }
            else
            if (date.getTime() <  (now.getTime() + 1 * 3600 * 1000))
            {
                overicon = "indicator_go_480x480.png";
            }
        }

        var formkey = event.date + ":" + (mediform.startsWith("ZZ") ? mediform : "AAA");

        if (! configs[ formkey ])
        {
            var config = {};
            config.icon = icon;
            config.label = label;
            config.overicon = overicon;
            WebLibLaunch.createLaunchItem(config);

            configs[ formkey ] = config;
        }
    }

    /*
    var config = {};
    config.icon = "health_frame_490x490.png";
    config.label = "08:00";
    WebLibLaunch.createLaunchItem(config);

    var config = {};
    config.icon = "health_bpm_256x256.png";
    config.label = "10:00";
    config.overicon = "indicator_no_480x480.png";
    config.frame = "yellow";
    WebLibLaunch.createLaunchItem(config);

    var config = {};
    config.icon = "health_scale_280x280.png";
    config.label = "10:00";
    WebLibLaunch.createLaunchItem(config);

    var config = {};
    config.icon = "health_glucose_512x512.png";
    config.label = "10:00";
    config.frame = "green";
    config.overicon = "indicator_ok_480x480.png";
    WebLibLaunch.createLaunchItem(config);

    var config = {};
    config.icon = "health_glucose_512x512.png";
    config.label = "14:00";
    config.frame = "blue";
    config.overicon = "indicator_go_480x480.png";
    WebLibLaunch.createLaunchItem(config);

    var config = {};
    config.icon = "health_glucose_512x512.png";
    config.label = "18:00";
    config.frame = "red";
    config.overicon = "indicator_alert_300x300.png";
    WebLibLaunch.createLaunchItem(config);
    */
}

medicator.createEvents();