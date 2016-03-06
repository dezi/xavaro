//
// Medicator main page.
//

medicator.updateMedisetEvent = function(mediset)
{
    console.log("medicator.updateMedisetEvent: want=" + JSON.stringify(mediset));

    var eventdate = mediset.date;
    var medication = mediset.medication + "," + mediset.mediform;

    for (var inx in medicator.comingEvents)
    {
        var event = medicator.comingEvents[ inx ];

        if (! (event.date && (event.date == eventdate))) continue;
        if (! (event.medication && (event.medication == medication))) continue;

        //
        // Taken data.
        //

        if (mediset.taken) event.taken = mediset.taken;
        if (mediset.takendose) event.takendose = mediset.takendose;
        if (mediset.takendate) event.takendate = mediset.takendate;

        //
        // Measurement data.
        //

        if (mediset.puls     ) event.puls      = mediset.puls;
        if (mediset.weight   ) event.weight    = mediset.weight;
        if (mediset.glucose  ) event.glucose   = mediset.glucose;
        if (mediset.systolic ) event.systolic  = mediset.systolic;
        if (mediset.diastolic) event.diastolic = mediset.diastolic;

        console.log("medicator.updateMedisetEvent: have=" + JSON.stringify(event));

        WebAppEvents.updateComingEvent(JSON.stringify(event));
    }
}

medicator.onClickTaken = function(event)
{
    var config = medicator.currentConfig
    if (! (config && config.medisets)) return;

    var ondemand = config.ondemand;
    var alltaken = true;
    var alldoses = 0;

    var ondemands = {};

    for (var inx = 0; inx < config.medisets.length; inx++)
    {
        var mediset = config.medisets[ inx ];

        var taken = mediset.checkBox &&  mediset.checkBox.checked;
        var pillimgs = mediset.pillimgs;

        if (pillimgs)
        {
            for (var cnt = 0; cnt < pillimgs.length; cnt++)
            {
                pillimgs[ cnt ].src = taken
                    ? "pill_taken_100x100.png" :
                    WebLibSimple.getNixPixImg();
            }
        }

        if (taken)
        {
            mediset.taken = taken;
            mediset.takendose = mediset.dose;
            if (! mediset.takendate) mediset.takendate = new Date().toISOString();
        }
        else
        {
            //
            // Could be revoked.
            //

            mediset.taken = false;
            mediset.takendose = "0";
        }

        if (ondemand)
        {
            //
            // Ondemand medisets are split up into seperate doses.
            // We need to recombine them for the event update.
            //

            var eventkey = mediset.medication + "," + mediset.mediform;

            if (! ondemands[ eventkey ])
            {
                ondemands[ eventkey ] = {};
                ondemands[ eventkey ].date = mediset.date;
                ondemands[ eventkey ].medication = mediset.medication;
                ondemands[ eventkey ].mediform = mediset.mediform;
                ondemands[ eventkey ].takendose = 0;
            }

            if (taken)
            {
                ondemands[ eventkey ].takendose += parseFloat(mediset.takendose);
                ondemands[ eventkey ].takendate  = mediset.takendate;
            }
        }
        else
        {
            medicator.updateMedisetEvent(mediset);
        }

        alltaken = alltaken && taken;
    }

    //
    // Update aggregated event record.
    //

    if (config.ondemand)
    {
        for (var eventkey in ondemands)
        {
            //
            // Dose in events is a string. Too bad.
            //

            ondemands[ eventkey ].taken = true;
            ondemands[ eventkey ].takendose += "";
            medicator.updateMedisetEvent(ondemands[ eventkey ]);
        }
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
        config.taken = alltaken;
    }

    return true;
}

medicator.onClickMeasured = function(event)
{
    var config = medicator.currentConfig
    if (! (config && config.medisets)) return;

    config.medisets[ 0 ].taken = true;
    config.medisets[ 0 ].takendate = new Date().toISOString();

    var whatSpan = medicator.currentDialog.whatSpan;

    if (whatSpan.puls) config.medisets[ 0 ].puls = whatSpan.puls;
    if (whatSpan.weight) config.medisets[ 0 ].weight = whatSpan.weight;
    if (whatSpan.glucose) config.medisets[ 0 ].glucose = whatSpan.glucose;
    if (whatSpan.systolic) config.medisets[ 0 ].systolic = whatSpan.systolic;
    if (whatSpan.diastolic) config.medisets[ 0 ].diastolic = whatSpan.diastolic;

    medicator.updateMedisetEvent(config.medisets[ 0 ]);

    var formkey = config.formkey;
    var launchitem = medicator.lauchis[ formkey ];
    var overicon = WebLibLaunch.getOverIconImgElem(launchitem);

    overicon.src = "indicator_ok_480x480.png";
    config.taken = true;

    return true;
}

medicator.onClickEverything = function(event)
{
    var config = medicator.currentConfig
    if (! (config && config.medisets)) return;

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
    if (! (target && target.mediset)) return;

    console.log("medicator.onClickDoseItem:" + JSON.stringify(target.mediset));

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

medicator.onBloodPressureKeypress = function(event)
{
    var whatSpan = WebLibSimple.findTarget(event.target, "whatSpan");
    if (! whatSpan) return;

    whatSpan.systolic = parseInt(whatSpan.systolicEdit.value);
    whatSpan.systolicEdit.style.color = (whatSpan.systolic > 260) ? "#ff0000" : "#444444";

    whatSpan.diastolic = parseInt(whatSpan.diastolicEdit.value);
    whatSpan.diastolicEdit.style.color = (whatSpan.diastolic > 260) ? "#ff0000" : "#444444";

    whatSpan.puls = parseInt(whatSpan.pulsEdit.value);
    whatSpan.pulsEdit.style.color = (whatSpan.puls > 260) ? "#ff0000" : "#444444";

    var okok = (whatSpan.systolic <= 260) && (whatSpan.diastolic <= 260) && (whatSpan.puls <= 260);

    WebLibDialog.setOkButtonEnable(okok);
}

medicator.onBloodGlucoseKeypress = function(event)
{
    var whatSpan = WebLibSimple.findTarget(event.target, "whatSpan");
    if (! whatSpan) return;

    whatSpan.glucose = parseInt(whatSpan.glucoseEdit.value);
    whatSpan.glucoseEdit.style.color = (whatSpan.glucose > 260) ? "#ff0000" : "#444444";

    var okok = (whatSpan.glucose <= 260);

    WebLibDialog.setOkButtonEnable(okok);
}

medicator.onWeightKeypress = function(event)
{
    var whatSpan = WebLibSimple.findTarget(event.target, "whatSpan");
    if (! whatSpan) return;

    whatSpan.weight = parseFloat(whatSpan.weightEdit.value);
    whatSpan.weightEdit.style.color = (whatSpan.weight > 260) ? "#ff0000" : "#444444";

    var okok = (whatSpan.weight <= 260);

    WebLibDialog.setOkButtonEnable(okok);
}

medicator.createNumberInput = function(parent, value, title, focus)
{
    var inputDiv = WebLibSimple.createAnyAppend("div", parent);
    inputDiv.style.padding = WebLibSimple.addPixel(8);

    var numberInput = WebLibSimple.createAnyAppend("input", inputDiv);
    WebLibSimple.setFontSpecs(numberInput, 28, "bold", "#444444");
    numberInput.style.textAlign = "center";
    numberInput.style.width = "80px";
    numberInput.autofocus = focus;
    numberInput.type = "number";
    numberInput.value = value;
    numberInput.select();

    var titleSpan = WebLibSimple.createAnyAppend("span", inputDiv);
    titleSpan.style.paddingLeft = WebLibSimple.addPixel(16);
    titleSpan.innerHTML = title;

    return numberInput;
}

medicator.onClickEventItem = function(event)
{
    event.stopPropagation();

    var target = WebLibSimple.findTarget(event.target, "launchItem");
    if (! (target && target.config)) return;

    WebAppUtility.makeClick();

    medicator.currentConfig = liconfig = target.config;
    medicator.currentDialog = dlconfig = {};

    dlconfig.content = document.createElement("span");

    for (var inx = 0; inx < liconfig.medisets.length; inx++)
    {
        var mediset = liconfig.medisets[ inx ];

        var div = WebLibSimple.createAnyAppend("div", dlconfig.content);
        WebLibSimple.setFontSpecs(div, 22, "bold", "#444444");
        div.style.paddingTop = WebLibSimple.addPixel(12);
        div.style.paddingBottom = WebLibSimple.addPixel(12);
        div.mediset = mediset;
        div.id = "medication";

        if (mediset.mediform.startsWith("ZZ"))
        {
            var whatSpan = WebLibSimple.createAnyAppend("div", div);
            whatSpan.style.display = "inline-block";
            whatSpan.id = "whatSpan";

            dlconfig.whatSpan = whatSpan;
            dlconfig.okEnabled = false;
            dlconfig.onClickOk = medicator.onClickMeasured;

            if (mediset.mediform == "ZZB")
            {
                dlconfig.title = "Blutdruck messen";

                whatSpan.systolicEdit = medicator.createNumberInput(whatSpan, mediset.systolic, "Systolisch", true);
                whatSpan.systolicEdit.onkeyup = medicator.onBloodPressureKeypress;

                whatSpan.diastolicEdit = medicator.createNumberInput(whatSpan, mediset.diastolic, "Diastolisch");
                whatSpan.diastolicEdit.onkeyup = medicator.onBloodPressureKeypress;

                whatSpan.pulsEdit = medicator.createNumberInput(whatSpan, mediset.puls, "Puls");
                whatSpan.pulsEdit.onkeyup = medicator.onBloodPressureKeypress;
            }

            if (mediset.mediform == "ZZG")
            {
                dlconfig.title = "Blutzucker messen";

                whatSpan.glucoseEdit = medicator.createNumberInput(whatSpan, mediset.glucose, "Glucosewert", true);
                whatSpan.glucoseEdit.onkeyup = medicator.onBloodGlucoseKeypress;
            }

            if (mediset.mediform == "ZZW")
            {
                dlconfig.title = "Wiegen";

                whatSpan.weightEdit = medicator.createNumberInput(whatSpan, mediset.weight, "Gewicht", true);
                whatSpan.weightEdit.onkeyup = medicator.onBloodGlucoseKeypress;
            }
        }
        else
        {
            dlconfig.title = "Medikamente einnehmen";

            div.onclick = medicator.onClickDoseItem;

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

            if (mediset.taken && mediset.takendate)
            {
                mediformDiv.innerHTML += " " + new Date(mediset.takendate).toLocaleString();
            }

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
        [ 1, 1 ], [ 1, 2 ], [ 1, 3 ],
        [ 2, 3 ], [ 3, 3 ],
        [ 2, 1 ], [ 2, 3 ], [ 3, 1 ],
    ];

    for (var inx = 0; inx < medicator.pillpositions.length; inx++)
    {
        medicator.pillpositions[ inx ][ 0 ] *= medicator.pillsize;
        medicator.pillpositions[ inx ][ 1 ] *= medicator.pillsize;
    }
}

medicator.updateEvents = function()
{
    var now = new Date().getTime();

    for (var formkey in medicator.configs)
    {
        var config = medicator.configs[ formkey ];
        var date = new Date(config.date).getTime();

        var overicon = null;

        if (! config.ondemand)
        {
            if (config.taken)
            {
                overicon = "indicator_ok_480x480.png";
            }
            else
            if (date < (now - 2 * 3600 * 1000))
            {
                overicon = "indicator_no_480x480.png";
            }
            else
            if (date < (now - 1 * 3600 * 1000))
            {
                overicon = "indicator_alert_300x300.png";
            }
            else
            if (date < (now + 1 * 3600 * 1000))
            {
                overicon = "indicator_go_480x480.png";
            }
        }

        var launchitem = medicator.lauchis[ formkey ];
        var overimg = WebLibLaunch.getOverIconImgElem(launchitem);
        overimg.src = overicon ? overicon : WebLibSimple.getNixPixImg();
    }

    setTimeout(medicator.updateEvents, 30000);
}

medicator.getItemLabel = function(event)
{
    var date = new Date(event.date);
    var hour = date.getHours();
    var minute = date.getMinutes();

    var label = WebLibSimple.padNum(hour, 2) + ":" + WebLibSimple.padNum(minute, 2);

    if (event.ondemand) label = "Bei Bedarf";

    return label;
}

medicator.getItemIcon = function(event)
{
    var mediform = event.medication.substring(event.medication.length - 3);

    var icon = "health_frame_490x490.png";

    if (mediform == "ZZB") icon = "health_bpm_256x256.png";
    if (mediform == "ZZG") icon = "health_glucose_512x512.png";
    if (mediform == "ZZW") icon = "health_scale_280x280.png";

    return icon;
}

medicator.createEvents = function()
{
    medicator.comingEvents = JSON.parse(WebAppEvents.getComingEvents());

    var today = WebLibSimple.getTodayDate().getTime();
    var midni = today + 86400 * 1000;

    today = new Date(today).toISOString();
    midni = new Date(midni).toISOString();

    medicator.configs = configs = {};
    medicator.lauchis = lauchis = {};

    for (var inx in medicator.comingEvents)
    {
        var event = medicator.comingEvents[ inx ];

        if ((event.date < today) || (event.date >= midni)) continue;

        var medication = event.medication.substring(0,event.medication.length - 4);
        var mediform = event.medication.substring(event.medication.length - 3);
        var ondemand = event.ondemand ? event.ondemand : null;
        var dose = event.dose ? event.dose : null;

        //
        // The formkey collects medication event into the same
        // time slot, while activity events are kept separate.
        //

        var formkey = event.date + ":" + (mediform.startsWith("ZZ") ? mediform : "AAA");

        if (! configs[ formkey ])
        {
            //
            // Create a new launch item for this event.
            //

            var config = {};

            config.date = event.date;
            config.icon = medicator.getItemIcon(event);
            config.label = medicator.getItemLabel(event);

            config.formkey = formkey;
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

        var config = configs[ formkey ];

        if (mediform.startsWith("ZZ"))
        {
            //
            // Activity medications have only one mediset.
            //

            var mediset = {};

            mediset.medication = medication;
            mediset.mediform = mediform;
            mediset.date = event.date;

            if (event.taken)
            {
                config.taken = true;
                mediset.taken = true;
                mediset.takendate = event.taken.date;

                mediset.puls      = event.puls;
                mediset.weight    = event.weight;
                mediset.glucose   = event.glucose;
                mediset.systolic  = event.systolic;
                mediset.diastolic = event.diastolic;
            }

            config.medisets.push(mediset);
        }
        else
        {
            //
            // On demand doses can be taken in the smallest split size.
            //

            var loopmax   = parseFloat(ondemand ? event.ondemandmax  : dose);
            var loopdose  = parseFloat(ondemand ? event.ondemanddose : dose);

            var taken = event.taken ? event.taken : false;
            var takendose = parseFloat(event.takendose ? event.takendose : "0");
            var takendate = event.takendate;

            //
            // Take care for pill parts rounding a la 0.33 pill dose.
            //

            var alltaken = true;

            while (loopmax > 0.1)
            {
                var mediset = {};

                mediset.medication = medication;
                mediset.mediform = mediform;
                mediset.ondemand = ondemand;
                mediset.dose = loopdose;
                mediset.date = event.date;

                if (taken && takendose > 0.1)
                {
                    mediset.taken = true;
                    mediset.takendose = loopdose;
                    mediset.takendate = takendate;
                    takendose -= loopdose;
                }
                else
                {
                    alltaken = false;
                }

                config.medisets.push(mediset);

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
                        pillweg.src = mediset.taken
                            ? "pill_taken_100x100.png"
                            : WebLibSimple.getNixPixImg();

                        mediset.pillimgs.push(pillweg);

                        config.pillslots++;
                        dose -= 1.0;
                    }
                }

                loopmax -= loopdose;
            }

            config.taken = alltaken;
            config.pillindex++;
        }
    }

    setTimeout(medicator.updateEvents, 0);
}

WebLibLaunch.createFrame();
medicator.createEvents();