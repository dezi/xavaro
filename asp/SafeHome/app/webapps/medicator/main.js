//
// Medicator main page.
//

WebLibLaunch.createFrame();

medicator.onClickTaken = function(event)
{
    var config = medicator.currentConfig
    if (! (config || config.medisets)) return;

    var alltaken = true;

    for (var inx = 0; inx < config.medisets.length; inx++)
    {
        var taken = config.medisets[ inx ].checkBox &&
                    config.medisets[ inx ].checkBox.checked;

        var pillimgs = config.medisets[ inx ].pillimgs;

        if (pillimgs)
        {
            for (var cnt = 0; cnt < pillimgs.length; cnt++)
            {
                pillimgs[ cnt ].src = taken
                    ? "pill_taken_100x100.png" :
                    WebLibSimple.getNixPixImg();
            }
        }

        config.medisets[ inx ].taken = taken;
        alltaken = alltaken && taken;
    }

    //
    // Adjust overlay image.
    //

    if (! config.ondemand)
    {
        var formkey = config.formkey;
        var launchitem = medicator.lauchis[ formkey ];
        var overicon = WebLibLaunch.getOverIconImgElem(launchitem);
        overicon.src = alltaken ? "indicator_ok_480x480.png" : "indicator_no_480x480.png";
    }

    return true;
}

medicator.onClickEverything = function(event)
{
    var config = medicator.currentConfig
    if (! (config || config.medisets)) return;

    for (var inx = 0; inx < config.medisets.length; inx++)
    {
        if (config.medisets[ inx ].checkBox)
        {
            config.medisets[ inx ].checkBox.checked = true;
        }
    }

    WebLibDialog.setOkButtonEnable(true);

    return false;
}

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
            div.style.paddingTop = WebLibSimple.addPixel(12);
            div.style.paddingBottom = WebLibSimple.addPixel(12);
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
            mediset.checkBox.checked = (mediset.taken == true);

            var whatSpan = WebLibSimple.createAnyAppend("div", div);
            whatSpan.style.display = "inline-block";

            var medicationDiv = WebLibSimple.createAnyAppend("div", whatSpan);
            medicationDiv.innerHTML = (mediset.dose ? (mediset.dose + " x ") : "")
                                    + mediset.medication;

            var mediformDiv = WebLibSimple.createAnyAppend("div", whatSpan);
            WebLibSimple.setFontSpecs(mediformDiv, 16, null, "#888888");

            mediformDiv.innerHTML = medicator.form[ mediset.mediform ];

            dlconfig.other = "Alles";
            dlconfig.otherEnabled = true;
            dlconfig.onClickOther = medicator.onClickEverything;

            dlconfig.ok = "Eingenommen";
            dlconfig.okEnabled = false;
            dlconfig.onClickOk = medicator.onClickTaken;
        }
    }

    WebLibDialog.createDialog(dlconfig);
}

medicator.computePillPositions = function(launchitem)
{
    if (medicator.pillpositions) return;

    medicator.pillimg =
    [
        "form/pill0.png", "form/pill1.png", "form/pill2.png", "form/pill3.png",
        "form/pill4.png", "form/pill5.png", "form/pill6.png", "form/pill7.png",
        "form/pill8.png", "form/pill9.png"
    ];

    var icondiv = WebLibLaunch.getIconDivElem(launchitem);
    var iconwid = icondiv.clientWidth;

    medicator.pillsize = Math.floor(iconwid / 5);

    medicator.pillpositions =
    [
        [ 2, 2 ],
        [ 1, 1 ], [ 1, 3 ], [ 3, 1 ], [ 3, 3 ],
        [ 1, 2 ], [ 2, 1 ], [ 2, 3 ], [ 3, 2 ]
    ];

    for (var inx = 0; inx < medicator.pillpositions.length; inx++)
    {
        medicator.pillpositions[ inx ][ 0 ] *= medicator.pillsize;
        medicator.pillpositions[ inx ][ 1 ] *= medicator.pillsize;
    }
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

    medicator.configs = configs = {};
    medicator.lauchis = lauchis = {};

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

        //
        // The formkey collects medication event into the same
        // time slot, while activity events are kept separate.
        //

        var formkey = event.date + ":" + (mediform.startsWith("ZZ") ? mediform : "AAA");

        if (! configs[ formkey ])
        {
            var config = {};

            config.icon = icon;
            config.label = label;
            config.formkey = formkey;
            config.overicon = overicon;
            config.ondemand = ondemand;
            config.onclick = medicator.onClickEventItem;
            config.medisets = [];
            config.pillslots = 0;
            config.pillindex = 0;

            configs[ formkey ] = config;
            lauchis[ formkey ] = WebLibLaunch.createLaunchItem(config);

            //
            // Pill positions depend on the size of the launch item.
            // They are computed when the first item is created.
            //

            medicator.computePillPositions(lauchis[ formkey ]);
        }

        if (mediform.startsWith("ZZ"))
        {
            //
            // Activity medications have only one mediset.
            //

            var mediset = {};

            mediset.medication = medication;
            mediset.mediform = mediform;

            configs[ formkey ].medisets.push(mediset);
        }
        else
        {
            //
            // On demand doses can be taken in the smallest split size.
            //

            var loopmax  = (ondemand ? event.ondemandmax  : dose);
            var loopdose = (ondemand ? event.ondemanddose : dose);

            //
            // Take care for pill parts rounding a la 0.33 pill dose.
            //

            while (loopmax > 0.1)
            {
                var mediset = {};

                mediset.medication = medication;
                mediset.mediform = mediform;
                mediset.ondemand = ondemand;
                mediset.dose = loopdose;

                configs[ formkey ].medisets.push(mediset);

                mediset.pillimgs = [];

                if (config.pillslots < medicator.pillpositions.length)
                {
                    var icondiv = WebLibLaunch.getIconDivElem(lauchis[ formkey ]);
                    var iconwid = icondiv.clientWidth;
                    var iconhei = icondiv.clientHeight;

                    var dose = parseFloat(mediset.dose);

                    while ((dose > 0) && (config.pillslots < medicator.pillpositions.length))
                    {
                        var pilldiv = WebLibSimple.createDivWidHei(
                            medicator.pillpositions[ config.pillslots ][ 0 ],
                            medicator.pillpositions[ config.pillslots ][ 1 ],
                            medicator.pillsize,
                            medicator.pillsize,
                            null, icondiv);

                        var pillimg = WebLibSimple.createImgWidHei(0, 0, "110%", "100%", null, pilldiv);
                        pillimg.src = "form/pill" + config.pillindex + ".png";

                        var pillweg = WebLibSimple.createImgWidHei(0, 0, "100%", "100%", null, pilldiv);
                        pillweg.src = WebLibSimple.getNixPixImg();

                        mediset.pillimgs.push(pillweg);

                        config.pillslots++;
                        dose -= 1.0;
                    }
                }

                loopmax -= loopdose;
            }

            config.pillindex++;
        }
    }
}

medicator.createEvents();