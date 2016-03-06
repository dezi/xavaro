//
// Medicator main page.
//

WebLibLaunch.createFrame();

medicator.onClickDoseCheckbox = function(event)
{
    event.stopPropagation();

    WebAppUtility.makeClick();

    medicator.onCheckOkButtonEnable();
}

medicator.onClickDoseItem = function(event)
{
    event.stopPropagation();

    var target = WebLibSimple.findTarget(event.target, "medication");
    if (! (target || target.mediset)) return;

    console.log("medicator.onClickDoseItem:");

    target.mediset.checkBox.checked = ! target.mediset.checkBox.checked;

    WebAppUtility.makeClick();

    medicator.onCheckOkButtonEnable();
}

medicator.onCheckOkButtonEnable = function()
{
    if (! medicator.currentConfig) return;

    var medisets = medicator.currentConfig.medisets;

    var onechecked = false;

    for (var inx = 0; inx < medisets.length; inx++)
    {
        if (medisets[ inx ].checkBox && medisets[ inx ].checkBox.checked)
        {
            onechecked = true;
            break;
        }
    }

    WebLibDialog.setOkButtonEnable(onechecked);
}

medicator.onClickEventItem = function(event)
{
    event.stopPropagation();

    var target = WebLibSimple.findTarget(event.target, "launchItem");
    if (! (target || target.config)) return;

    WebAppUtility.makeClick();

    medicator.currentConfig = liconfig = target.config;
    medicator.currentDialog = dlconfig = {};

    dlconfig.title = "Medikamente einnehmen";
    dlconfig.content = document.createElement("span");

    for (var inx = 0; inx < liconfig.medisets.length; inx++)
    {
        var mediset = liconfig.medisets[ inx ];

        if (mediset.mediform == "ZZB")
        {
            dlconfig.title = "Blutdruck messen";
        }
        else
        if (mediset.mediform == "ZZG")
        {
            dlconfig.title = "Blutzucker messen";
        }
        else
        if (mediset.mediform == "ZZW")
        {
            dlconfig.title = "Wiegen";
        }
        else
        {
            var div = WebLibSimple.createAnyAppend("div", dlconfig.content);
            WebLibSimple.setFontSpecs(div, 22, "bold", "#444444");
            div.style.paddingBottom = WebLibSimple.addPixel(16);
            div.onclick = medicator.onClickDoseItem;
            div.mediset = mediset;
            div.id = "medication";

            var checkSpan = WebLibSimple.createAnyAppend("span", div);
            checkSpan.style.display = "inline-block";
            checkSpan.style.paddingLeft  = WebLibSimple.addPixel(8);
            checkSpan.style.paddingRight = WebLibSimple.addPixel(8);
            checkSpan.style.float = "left";

            mediset.checkBox = WebLibSimple.createAnyAppend("input", checkSpan);
            mediset.checkBox.type = "checkbox";
            mediset.checkBox.onclick = medicator.onClickDoseCheckbox;

            var whatSpan = WebLibSimple.createAnyAppend("div", div);
            whatSpan.style.display = "inline-block";

            var medicationDiv = WebLibSimple.createAnyAppend("div", whatSpan);
            medicationDiv.innerHTML = (mediset.dose ? (mediset.dose + " x ") : "")
                                    + mediset.medication;

            var mediformDiv = WebLibSimple.createAnyAppend("div", whatSpan);
            WebLibSimple.setFontSpecs(mediformDiv, 16, null, "#888888");

            mediformDiv.innerHTML = medicator.form[ mediset.mediform ];

            dlconfig.ok = "Eingenommen";
            dlconfig.okEnabled = false;
        }
    }

    WebLibDialog.createDialog(dlconfig);
}

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
            config.onclick = medicator.onClickEventItem;
            config.medisets = [];

            configs[ formkey ] = config;

            WebLibLaunch.createLaunchItem(config);
        }

        var mediset = {};

        mediset.medication = medication;
        mediset.mediform = mediform;
        mediset.ondemand = ondemand;
        mediset.dose = dose;

        configs[ formkey ].medisets.push(mediset);
    }
}

medicator.createEvents();