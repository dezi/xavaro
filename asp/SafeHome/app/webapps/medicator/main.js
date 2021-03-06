//
// Medicator main page.
//

WebAppRequest.onVoiceIntent = function(intent)
{
    console.log("WebAppRequest.onVoiceIntent: ====================>" + JSON.stringify(intent));
}

medicator.onTakePills = function(doit)
{
    medicator.onTakeThat(doit, "AAA");
}

medicator.onTakeBloodPressure = function(doit)
{
    medicator.onTakeThat(doit, "ZZB");
}

medicator.onTakeBloodOxygen = function(doit)
{
    medicator.onTakeThat(doit, "ZZO");
}

medicator.onTakeBloodGlucose = function(doit)
{
    medicator.onTakeThat(doit, "ZZG");
}

medicator.onTakeTemperature = function(doit)
{
    medicator.onTakeThat(doit, "ZZT");
}

medicator.onTakeWeight = function(doit)
{
    medicator.onTakeThat(doit, "ZZW");
}

medicator.onTakeThat = function(doit, mediform)
{
    var now = new Date().getTime()

    var bestForm = null;
    var bestDiff = 0;

    for (var formkey in medicator.configs)
    {
        var config = medicator.configs[ formkey ];

        if (config.ondemand || config.taken) continue;

        //
        // Identify which medication form is suitable.
        //

        var confmediform = config.medisets[ 0 ].mediform;
        if (! confmediform.startsWith("ZZ")) confmediform = "AAA";

        if (confmediform != mediform) continue;

        var targetDate = new Date(config.medisets[ 0 ].date).getTime();
        var targetDiff = Math.abs(now - targetDate);

        if ((bestForm == null) || ((targetDiff + (30 * 60 * 1000)) < bestDiff))
        {
            bestForm = formkey;
            bestDiff = targetDiff;
        }
    }

    if (bestForm)
    {
        if (doit)
        {
            var lauchitem = medicator.lauchis[ bestForm ];
            medicator.onClickEventItem(lauchitem, null, true);
        }
        else
        {
            //
            // User declined medication.
            //

            console.log("medicator.onTakeThat: declined=" + mediform);

            var config = medicator.configs[ bestForm ];

            for (var inx = 0; inx < config.medisets.length; inx++)
            {
                medicator.updateMedisetEvent(config.medisets[ inx ]);
            }

            //
            // Remove notification.
            //

            var notify = {};

            if (mediform == "AAA") notify.key = "medicator.take.pills";
            if (mediform == "ZZB") notify.key = "medicator.take.bloodpressure";
            if (mediform == "ZZO") notify.key = "medicator.take.bloodoxygen";
            if (mediform == "ZZG") notify.key = "medicator.take.bloodglucose";
            if (mediform == "ZZT") notify.key = "medicator.take.temperature";
            if (mediform == "ZZW") notify.key = "medicator.take.weight";

            console.log("medicator.onTakeThat: remove=" + notify.key);

            WebAppNotify.removeNotification(JSON.stringify(notify));
            WebAppNotify.updateNotificationDisplay();
        }
    }
}

medicator.updateMedisetEvent = function(mediset)
{
    var eventdate = mediset.date;
    var medication = mediset.medication + "," + mediset.mediform;

    for (var inx in medicator.comingEvents)
    {
        var event = medicator.comingEvents[ inx ];

        if (! (event.date && (event.date == eventdate))) continue;
        if (! (event.medication && (event.medication == medication))) continue;

        event.completed = true;

        //
        // Taken data.
        //

        if (mediset.taken) event.taken = mediset.taken;
        if (mediset.takendose) event.takendose = mediset.takendose;
        if (mediset.takendate) event.takendate = mediset.takendate;

        //
        // Measurement data.
        //

        if (mediset.puls      ) event.puls       = mediset.puls;
        if (mediset.meal      ) event.meal       = mediset.meal;
        if (mediset.temp      ) event.temp       = mediset.temp;
        if (mediset.weight    ) event.weight     = mediset.weight;
        if (mediset.glucose   ) event.glucose    = mediset.glucose;
        if (mediset.systolic  ) event.systolic   = mediset.systolic;
        if (mediset.diastolic ) event.diastolic  = mediset.diastolic;
        if (mediset.saturation) event.saturation = mediset.saturation;

        console.log("medicator.updateMedisetEvent: event=" + JSON.stringify(event));

        WebAppEvents.updateComingEvent(JSON.stringify(event));
    }
}

medicator.updateHealthData = function(mediset)
{
    if (! mediset.taken) return;

    var record = {};

    //
    // Taken data.
    //

    if (mediset.takendate) record.dts = mediset.takendate;
    if (mediset.takendose) record.dos = mediset.takendose;

    record.med = mediset.medication;
    record.frm = mediset.mediform;
    record.man = true;

    //
    // Measurement data.
    //

    if (mediset.puls      ) record.pls = mediset.puls;
    if (mediset.meal      ) record.mea = mediset.meal;
    if (mediset.temp      ) record.tmp = mediset.temp;
    if (mediset.weight    ) record.wei = mediset.weight;
    if (mediset.glucose   ) record.bgv = mediset.glucose;
    if (mediset.systolic  ) record.sys = mediset.systolic;
    if (mediset.diastolic ) record.dia = mediset.diastolic;
    if (mediset.saturation) record.sat = mediset.saturation;

    var type = "medication";

    if (mediset.mediform == "ZZB") type = "bpm";
    if (mediset.mediform == "ZZO") type = "oxy";
    if (mediset.mediform == "ZZG") type = "glucose";
    if (mediset.mediform == "ZZT") type = "temp";
    if (mediset.mediform == "ZZW") type = "scale";

    WebAppHealth.addRecord(type, JSON.stringify(record));

    medicator.informAssistance(mediset);
}

medicator.informAssistance = function(mediset)
{
    var tkey = "events.didnowtake.pills";

    if (mediset.mediform == "ZZB") tkey = "events.didnowtake.bloodpressure";
    if (mediset.mediform == "ZZO") tkey = "events.didnowtake.bloodoxygen";
    if (mediset.mediform == "ZZG") tkey = "events.didnowtake.bloodglucose";
    if (mediset.mediform == "ZZT") tkey = "events.didnowtake.temperature";
    if (mediset.mediform == "ZZW") tkey = "events.didnowtake.weight";

    if (tkey)
    {
        var name = WebAppUtility.getOwnerName();
        var text = WebLibStrings.getTrans(tkey, name);

        WebAppAssistance.informAssistance(text);
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
            medicator.updateHealthData(mediset);
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

            medicator.updateHealthData(ondemands[ eventkey ]);
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

    WebAppActivity.recordActivity(WebLibStrings.getTrans("activity.took.pills"));

    //
    // Remove any pending notification.
    //

    var notify = {};

    notify.key = "medicator.take.pills";

    WebAppNotify.removeNotification(JSON.stringify(notify));
    WebAppNotify.updateNotificationDisplay();

    return true;
}

medicator.onClickMeasured = function(target)
{
    var config = medicator.currentConfig
    if (! (config && config.medisets)) return;

    var whatSpan = medicator.currentDialog.whatSpan;

    if (! whatSpan.bluetooth)
    {
        config.taken = true;
        config.medisets[ 0 ].taken = true;
        config.medisets[ 0 ].takendate = new Date().toISOString();

        if (whatSpan.puls) config.medisets[ 0 ].puls = whatSpan.puls;
        if (whatSpan.meal) config.medisets[ 0 ].meal = whatSpan.meal;
        if (whatSpan.temp) config.medisets[ 0 ].temp = whatSpan.temp;
        if (whatSpan.weight) config.medisets[ 0 ].weight = whatSpan.weight;
        if (whatSpan.glucose) config.medisets[ 0 ].glucose = whatSpan.glucose;
        if (whatSpan.systolic) config.medisets[ 0 ].systolic = whatSpan.systolic;
        if (whatSpan.diastolic) config.medisets[ 0 ].diastolic = whatSpan.diastolic;
        if (whatSpan.saturation) config.medisets[ 0 ].saturation = whatSpan.saturation;

        medicator.updateHealthData(config.medisets[ 0 ]);
        medicator.updateMedisetEvent(config.medisets[ 0 ]);

        var formkey = config.formkey;
        var launchitem = medicator.lauchis[ formkey ];
        var overicon = WebLibLaunch.getOverIconImgElem(launchitem);
        overicon.src = "indicator_ok_480x480.png";

        var mediform = config.medisets[ 0 ].mediform;
        var meditext = null;

        if (mediform.startsWith("ZZB")) meditext = "activity.took.bloodpressure";
        if (mediform.startsWith("ZZO")) meditext = "activity.took.bloodoxygen";
        if (mediform.startsWith("ZZG")) meditext = "activity.took.bloodglucose";
        if (mediform.startsWith("ZZT")) meditext = "activity.took.temperature";
        if (mediform.startsWith("ZZW")) meditext = "activity.took.weight";

        if (meditext != null) WebAppActivity.recordActivity(WebLibStrings.getTrans(meditext));

        //
        // Remove any pending notification.
        //

        var notify = {};

        if (mediform.startsWith("ZZB")) notify.key = "medicator.take.bloodpressure";
        if (mediform.startsWith("ZZO")) notify.key = "medicator.take.bloodoxygen";
        if (mediform.startsWith("ZZG")) notify.key = "medicator.take.bloodglucose";
        if (mediform.startsWith("ZZT")) notify.key = "medicator.take.temperature";
        if (mediform.startsWith("ZZW")) notify.key = "medicator.take.weight";

        if (notify.key)
        {
            WebAppNotify.removeNotification(JSON.stringify(notify));
            WebAppNotify.updateNotificationDisplay();
        }
    }

    medicator.currentDialog = null;

    return true;
}

medicator.onClickEverything = function(target)
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

medicator.onClickDoseCheckbox = function(target)
{
    WebAppUtility.makeClick();

    medicator.onCheckOkButtonEnable();
}

medicator.onClickDoseItem = function(target)
{
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

medicator.onBloodOxygenKeypress = function(event)
{
    var whatSpan = WebLibSimple.findTarget(event.target, "whatSpan");
    if (! whatSpan) return;

    whatSpan.saturation = parseInt(whatSpan.saturationEdit.value);
    whatSpan.saturationEdit.style.color = (whatSpan.saturation > 100) ? "#ff0000" : "#444444";

    whatSpan.puls = parseInt(whatSpan.pulsEdit.value);
    whatSpan.pulsEdit.style.color = (whatSpan.puls > 260) ? "#ff0000" : "#444444";

    var okok = (whatSpan.saturation <= 260) && (whatSpan.puls <= 260);

    WebLibDialog.setOkButtonEnable(okok);
}

medicator.onBloodGlucoseKeypress = function(event)
{
    console.log("medicator.onBloodGlucoseKeypress: " + event);

    var whatSpan = WebLibSimple.findTarget(event.target, "whatSpan");
    if (! whatSpan) return;

    whatSpan.glucose = parseInt(whatSpan.glucoseEdit.value);
    whatSpan.glucoseEdit.style.color = (whatSpan.glucose > 260) ? "#ff0000" : "#444444";

    whatSpan.meal = whatSpan.mealSelect.value;
    whatSpan.mealSelect.style.color = (whatSpan.meal == "0:undefined") ? "#ff0000" : "#444444";

    var okok = ((whatSpan.glucose <= 260) && (whatSpan.meal != "0:undefined"));

    WebLibDialog.setOkButtonEnable(okok);
}

medicator.onTemperaturKeypress = function(event)
{
    console.log("medicator.onTemperaturKeypress: " + event);

    var whatSpan = WebLibSimple.findTarget(event.target, "whatSpan");
    if (! whatSpan) return;

    whatSpan.temp = parseFloat(whatSpan.tempEdit.value);
    whatSpan.tempEdit.style.color = (whatSpan.temp > 45) ? "#ff0000" : "#444444";

    var okok = (whatSpan.temp <= 45);

    WebLibDialog.setOkButtonEnable(okok);
}

medicator.onWeightKeypress = function(event)
{
    var whatSpan = WebLibSimple.findTarget(event.target, "whatSpan");
    if (! whatSpan) return;

    whatSpan.weight = parseFloat(whatSpan.weightEdit.value);
    whatSpan.weightEdit.style.color = (whatSpan.weight > 20) ? "#ff0000" : "#444444";

    var okok = (whatSpan.weight <= 20);

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
    numberInput.type = "number";
    numberInput.value = value;

    if (focus)
    {
        numberInput.autofocus = true;
        numberInput.select();
    }

    var titleSpan = WebLibSimple.createAnyAppend("span", inputDiv);
    titleSpan.style.paddingLeft = WebLibSimple.addPixel(16);
    titleSpan.innerHTML = title;

    return numberInput;
}

medicator.createSelectInput = function(parent, value, title, langtag, focus)
{
    var inputDiv = WebLibSimple.createAnyAppend("div", parent);
    inputDiv.style.padding = WebLibSimple.addPixel(8);

    var selectInput = WebLibSimple.createAnyAppend("select", inputDiv);
    WebLibSimple.setFontSpecs(selectInput, 28, "bold", "#444444");
    selectInput.style.textAlign = "center";

    var keys = WebLibStrings.getTrans(langtag + ".keys");
    var vals = WebLibStrings.getTrans(langtag + ".vals");

    for (var inx = 0; inx < keys.length; inx++)
    {
        var option = document.createElement("option");
        option.text  = vals[ inx ];
        option.value = keys[ inx ];

        selectInput.add(option);
    }

    selectInput.value = value;

    return selectInput;
}

medicator.onClickEventItem = function(target, ctarget, noclick)
{
    if (! (target && target.config)) return;

    if (! noclick) WebAppUtility.makeClick();

    var liconfig = medicator.currentConfig = target.config;
    var dlconfig = medicator.currentDialog = {};

    dlconfig.content = document.createElement("span");

    for (var inx = 0; inx < liconfig.medisets.length; inx++)
    {
        var mediset = liconfig.medisets[ inx ];

        var div = WebLibSimple.createAnyAppend("div", dlconfig.content);
        WebLibSimple.setFontSpecs(div, 22, "bold", "#444444");
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

            if (mediset.mediform == "ZZO")
            {
                dlconfig.title = "Blutsauerstoff messen";

                whatSpan.saturationEdit = medicator.createNumberInput(whatSpan, mediset.saturation, "Sättigung", true);
                whatSpan.saturationEdit.onkeyup = medicator.onBloodOxygenKeypress;

                whatSpan.pulsEdit = medicator.createNumberInput(whatSpan, mediset.puls, "Puls");
                whatSpan.pulsEdit.onkeyup = medicator.onBloodOxygenKeypress;
            }

            if (mediset.mediform == "ZZG")
            {
                dlconfig.title = "Blutzucker messen";

                whatSpan.mealSelect = medicator.createSelectInput(whatSpan, mediset.meal, "Markierung", "glucose.meal", true);
                whatSpan.mealSelect.onchange = medicator.onBloodGlucoseKeypress;
                whatSpan.mealSelect.style.color = (whatSpan.mealSelect.value == "0:undefined") ? "#ff0000" : "#444444";

                whatSpan.glucoseEdit = medicator.createNumberInput(whatSpan, mediset.glucose, "Glucosewert", true);
                whatSpan.glucoseEdit.onkeyup = medicator.onBloodGlucoseKeypress;
           }

            if (mediset.mediform == "ZZT")
            {
                dlconfig.title = "Temperatur messen";

                whatSpan.tempEdit = medicator.createNumberInput(whatSpan, mediset.temp, "Temperatur", true);
                whatSpan.tempEdit.onkeyup = medicator.onTemperaturKeypress;
           }

            if (mediset.mediform == "ZZW")
            {
                dlconfig.title = "Wiegen";

                whatSpan.weightEdit = medicator.createNumberInput(whatSpan, mediset.weight, "Gewicht", true);
                whatSpan.weightEdit.onkeyup = medicator.onWeightKeypress;
            }

            if (mediset.taken && mediset.takendate)
            {
                var takendateDiv = WebLibSimple.createAnyAppend("div", whatSpan);
                WebLibSimple.setFontSpecs(takendateDiv, 16, null, "#888888");
                takendateDiv.style.paddingLeft = WebLibSimple.addPixel(8);
                takendateDiv.style.paddingTop = WebLibSimple.addPixel(16);

                var nicedate = WebLibSimple.getNiceDate(mediset.takendate)
                takendateDiv.innerHTML = "Gemessen" + " " + nicedate;
            }
        }
        else
        {
            dlconfig.title = "Medikamente einnehmen";

            div.style.paddingTop = WebLibSimple.addPixel(12);
            div.onTouchClick = medicator.onClickDoseItem;

            var checkSpan = WebLibSimple.createAnyAppend("span", div);
            checkSpan.style.display = "inline-block";
            checkSpan.style.paddingLeft  = WebLibSimple.addPixel(8);
            checkSpan.style.paddingRight = WebLibSimple.addPixel(8);
            checkSpan.style.float = "left";

            mediset.checkBox = WebLibSimple.createAnyAppend("input", checkSpan);
            mediset.checkBox.type = "checkbox";
            mediset.checkBox.onTouchClick = medicator.onClickDoseCheckbox;
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
                var takendateDiv = WebLibSimple.createAnyAppend("div", whatSpan);
                WebLibSimple.setFontSpecs(takendateDiv, 16, null, "#888888");

                var nicedate = WebLibSimple.getNiceDate(mediset.takendate)
                takendateDiv.innerHTML = "Eingenommen" + " " + nicedate;
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

medicator.insertDialogValues = function(event)
{
    if (! (medicator.currentDialog && medicator.currentDialog.whatSpan)) return;

    var whatSpan = medicator.currentDialog.whatSpan;
    var haveType = false;

    if (whatSpan.pulsEdit && event.puls)
    {
        //
        // Puls does not identify type.
        //

        whatSpan.pulsEdit.value = event.puls;
    }

    if (whatSpan.mealSelect && event.meal)
    {
        whatSpan.mealSelect.value = event.meal;
        haveType = true;
    }

    if (whatSpan.weightEdit && event.weight)
    {
        whatSpan.weightEdit.value = event.weight;
        haveType = true;
    }

    if (whatSpan.glucoseEdit && event.glucose)
    {
        whatSpan.glucoseEdit.value = event.glucose;
        haveType = true;
    }

    if (whatSpan.tempEdit && event.temp)
    {
        whatSpan.tempEdit.value = event.temp;
        haveType = true;
    }

    if (whatSpan.systolicEdit && event.systolic)
    {
        whatSpan.systolicEdit.value = event.systolic;
        haveType = true;
    }

    if (whatSpan.diastolicEdit && event.diastolic)
    {
        whatSpan.diastolicEdit.value = event.diastolic;
        haveType = true;
    }

    if (whatSpan.saturationEdit && event.saturation)
    {
        whatSpan.saturationEdit.value = event.saturation;
        haveType = true;
    }

    if (haveType)
    {
        whatSpan.bluetooth = true;
        WebLibDialog.setOkButtonEnable(true);
    }
}

medicator.updateEvents = function()
{
    //
    // Re-read actual events.
    //

    medicator.comingEvents = JSON.parse(WebAppEvents.getComingEvents());

    //
    // Update measurement result values from bluetooth devices.
    //

    for (var inx in medicator.comingEvents)
    {
        var event = medicator.comingEvents[ inx ];

        if (! event.taken) continue;

        var medication = event.medication.substring(0,event.medication.length - 4);
        var mediform = event.medication.substring(event.medication.length - 3);

        if (! mediform.startsWith("ZZ")) continue;

        var formkey = event.date + ":" + (mediform.startsWith("ZZ") ? mediform : "AAA");
        if (! medicator.configs[ formkey ]) continue;

        var config = medicator.configs[ formkey ];
        if (config.taken) continue;

        console.log("medicator.updateEvents: event=" + JSON.stringify(event));

        config.taken = true;
        config.medisets[ 0 ].taken = true;
        config.medisets[ 0 ].takendate = event.takendate;

        config.medisets[ 0 ].puls       = event.puls;
        config.medisets[ 0 ].meal       = event.meal;
        config.medisets[ 0 ].temp       = event.temp;
        config.medisets[ 0 ].weight     = event.weight;
        config.medisets[ 0 ].glucose    = event.glucose;
        config.medisets[ 0 ].systolic   = event.systolic;
        config.medisets[ 0 ].diastolic  = event.diastolic;
        config.medisets[ 0 ].saturation = event.saturation;

        medicator.insertDialogValues(event);
    }

    var now = new Date().getTime();

    for (var formkey in medicator.configs)
    {
        var config = medicator.configs[ formkey ];
        var date = new Date(config.date).getTime();

        console.log("medicator.updateEvents: config=" + JSON.stringify(config));

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

        if (overicon)
        {
            var launchitem = medicator.lauchis[ formkey ];
            var overimg = WebLibLaunch.getOverIconImgElem(launchitem);
            WebLibSimple.setImageSource(overimg, overicon)
        }
    }

    var millis = (medicator.currentDialog && medicator.currentDialog.whatSpan) ? 1000 : 10000;
    setTimeout(medicator.updateEvents, millis);
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
    if (mediform == "ZZO") icon = "health_oxy_440x440.png";
    if (mediform == "ZZG") icon = "health_glucose_512x512.png";
    if (mediform == "ZZT") icon = "health_thermo_512x512.png";
    if (mediform == "ZZW") icon = "health_scale_280x280.png";

    return icon;
}

medicator.createEvents = function()
{
    if (! medicator.configs) medicator.configs = {};
    if (! medicator.lauchis) medicator.lauchis = {};

    medicator.comingEvents = JSON.parse(WebAppEvents.getComingEvents());

    var today = WebLibSimple.getTodayDate().getTime();
    var midni = today + 86400 * 1000;

    today = new Date(today).toISOString();
    midni = new Date(midni).toISOString();

    var configs = medicator.configs;
    var lauchis = medicator.lauchis;

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
            config.onTouchClick = medicator.onClickEventItem;

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
                mediset.takendate = event.takendate;

                mediset.puls       = event.puls;
                mediset.meal       = event.meal;
                mediset.temp       = event.temp;
                mediset.weight     = event.weight;
                mediset.glucose    = event.glucose;
                mediset.systolic   = event.systolic;
                mediset.diastolic  = event.diastolic;
                mediset.saturation = event.saturation;
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
}

WebLibLaunch.createFrame();
medicator.createEvents();
medicator.updateEvents();