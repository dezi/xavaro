//
// Medicator main page.
//

WebLibLaunch.createFrame();

medicator.createEvents = function()
{
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
}

medicator.createEvents();