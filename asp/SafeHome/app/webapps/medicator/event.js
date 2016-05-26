medicator.remindMaximum = 3;
medicator.remindIntervall = 600;

medicator.getNextEvents = function()
{
    var events = JSON.parse(WebAppEvents.getCurrentEventsClear());

    if (events.length > 0)
    {
        //
        // Cancel unload request timer because we have events.
        //

        if (medicator.requestUnloadTimer)
        {
            clearTimeout(medicator.requestUnloadTimer);
            medicator.requestUnloadTimer = null;
        }

        //
        // Work on events.
        //

        var configs = {};

        //
        // Preset event types to avoid reminding the same
        // type of medication twice in one run.
        //

        medicator.pillsreminder = false;
        medicator.weightreminder = false;
        medicator.bloodglucosereminder = false;
        medicator.bloodpressurereminder = false;

        for (var inx = 0; inx < events.length; inx++)
        {
            var event = events[ inx ];

            //
            // The formkey collects medication event into the same
            // time slot, while activity events are kept separate.
            //

            var mediform = event.medication.substring(event.medication.length - 3);
            var mediflat = (mediform.startsWith("ZZ") ? mediform : "AAA")
            var formkey = event.date + ":" + mediflat;

            if (! configs[ formkey ])
            {
                //
                // Create a new launch item for this event.
                //

                var config = {};
                config.date = event.date;
                config.mediflat = mediflat;
                config.mediform = mediform;
                config.events = [];

                configs[ formkey ] = config;
            }

            configs[ formkey ].events.push(event);
        }

        for (var formkey in configs)
        {
            var config = configs[ formkey ];

            console.log("config=" + JSON.stringify(config));

            var alltaken = true;
            var ondemand = true;

            for (var inx in config.events)
            {
                var event = config.events[ inx ];

                alltaken = alltaken && event.taken;
                ondemand = ondemand && event.ondemand;
            }

            if (alltaken || ondemand)
            {
                medicator.completeConfig(config);

                continue;
            }

            medicator.remindConfig(config);
        }
    }

    medicator.getNextEventsTimer = setTimeout(medicator.getNextEvents,  10 * 1000);

    if (! medicator.requestUnloadTimer)
    {
        //
        // Schedule unload request timer which will fire after
        // the given time if there are no events anymore.
        //

        medicator.requestUnloadTimer = setTimeout(medicator.requestUnload, 100 * 1000);
    }
}

medicator.requestUnload = function()
{
    if (medicator.getNextEventsTimer)
    {
        clearTimeout(medicator.getNextEventsTimer);
        medicator.getNextEventsTimer = null;
    }

    console.log("medicator.requestUnload");

    WebAppIntercept.requestUnload(0);
}

medicator.getImportance = function(config)
{
    var reminded = config.events[ 0 ].reminded;

    if (reminded == 0) return WebAppNotify.getImportanceReminder();
    if (reminded == 1) return WebAppNotify.getImportanceWarning();
    if (reminded == 2) return WebAppNotify.getImportanceAssistance();

    return WebAppNotify.getImportanceUrgent();
}

medicator.remindConfig = function(config)
{
    //
    // Take current reminded date and count from first event.
    //

    var reminded = config.events[ 0 ].reminded;
    var remindeddate = config.events[ 0 ].remindeddate;

    var nowtime = new Date().getTime();
    var remtime = remindeddate ? new Date(remindeddate).getTime() : 0;
    var newtime = remtime + medicator.remindIntervall * 1000;

    //
    // Build or rebuild notifications.
    //

    if ((config.mediflat == "AAA") && ! medicator.pillsreminder)
    {
        var notify = {};

        notify.key         = "medicator.take.pills";
        notify.title       = WebLibStrings.getTrans("events.take.pills");
        notify.icon        = "health_medication_512x512.png";
        notify.importance  = medicator.getImportance(config);
        notify.followText  = WebLibStrings.getTrans("events.take.pills.follow");
        notify.declineText = WebLibStrings.getTrans("events.take.pills.decline");
        notify.speakOnce   = (newtime < nowtime);

        notify.followJavaCall  = "medicator.onTakePills(true);"
        notify.declineJavaCall = "medicator.onTakePills(false);"

        WebAppNotify.addNotification(JSON.stringify(notify));

        medicator.pillsreminder = true;
    }

    if ((config.mediflat == "ZZB") && ! medicator.bloodpressurereminder)
    {
        var notify = {};

        notify.key         = "medicator.take.bloodpressure";
        notify.title       = WebLibStrings.getTrans("events.take.bloodpressure");
        notify.icon        = "health_bpm_256x256.png";
        notify.importance  = medicator.getImportance(config);
        notify.followText  = WebLibStrings.getTrans("events.take.bloodpressure.follow");
        notify.declineText = WebLibStrings.getTrans("events.take.bloodpressure.decline");
        notify.speakOnce   = (newtime < nowtime);

        notify.followJavaCall  = "medicator.onTakeBloodPressure(true);"
        notify.declineJavaCall = "medicator.onTakeBloodPressure(false);"

        WebAppNotify.addNotification(JSON.stringify(notify));

        medicator.bloodpressurereminder = true;
    }

    if ((config.mediform == "ZZG") && ! medicator.bloodglucosereminder)
    {
        var notify = {};

        notify.key         = "medicator.take.bloodglucose";
        notify.title       = WebLibStrings.getTrans("events.take.bloodglucose");
        notify.icon        = "health_glucose_512x512.png";
        notify.importance  = medicator.getImportance(config);
        notify.followText  = WebLibStrings.getTrans("events.take.bloodglucose.follow");
        notify.declineText = WebLibStrings.getTrans("events.take.bloodglucose.decline");
        notify.speakOnce   = (newtime < nowtime);

        notify.followJavaCall  = "medicator.onTakeBloodGlucose(true);"
        notify.declineJavaCall = "medicator.onTakeBloodGlucose(false);"

        WebAppNotify.addNotification(JSON.stringify(notify));

        medicator.bloodglucosereminder = true;
    }

    if ((config.mediflat == "ZZW") && ! medicator.weightreminder)
    {
        var notify = {};

        notify.key         = "medicator.take.weight";
        notify.title       = WebLibStrings.getTrans("events.take.weight");
        notify.icon        = "health_scale_280x280.png";
        notify.importance  = medicator.getImportance(config);
        notify.followText  = WebLibStrings.getTrans("events.take.weight.follow");
        notify.declineText = WebLibStrings.getTrans("events.take.weight.decline");
        notify.speakOnce   = (newtime < nowtime);

        notify.followJavaCall  = "medicator.onTakeWeight(true);"
        notify.declineJavaCall = "medicator.onTakeWeight(false);"

        WebAppNotify.addNotification(JSON.stringify(notify));

        medicator.weightreminder = true;
    }

    if (newtime >= nowtime)
    {
        //
        // Time not yet due to increase reminder count or close event.
        //

        return;
    }

    //
    // Assistance handling.
    //

    var assistanceInformed = false;

    if (config.mediflat == "AAA")
    {
        if (((reminded + 1) >= medicator.remindMaximum) && WebAppAssistance.hasAssistance())
        {
            if (! config.events[ 0 ].alerted)
            {
                var name = WebAppUtility.getOwnerName();
                var text = WebLibStrings.getTrans("events.didnotyettake.pills", name);

                WebAppAssistance.informAssistance(text);

                assistanceInformed = true;
            }
        }
    }

    //
    // Update reminded date and count.
    //

    for (var inx in config.events)
    {
        var event = config.events[ inx ];

        if (assistanceInformed)
        {
            event.alerted = 1;
            event.alerteddate = new Date().toISOString();
        }

        event.reminded = event.reminded ? event.reminded + 1 : 1;
        event.remindeddate = new Date().toISOString();

        if ((event.reminded >= medicator.remindMaximum) && ! assistanceInformed)
        {
            //
            // Complete only on non important events. Alerted events
            // will repeatedly return until user has performed
            // required action.
            //

            event.completed = true;
        }

        WebAppEvents.updateComingEvent(JSON.stringify(event));
    }
}

medicator.completeConfig = function(config)
{
    //
    // Check if assistance was alerted. If so inform,
    // that the medication has been taken in the meantime.
    //

    if (config.events[ 0 ].alerted)
    {
        var tkey = null;

        if ((config.mediflat == "AAA") && ! medicator.pillsreminder)
        {
            tkey = "events.didnowtake.pills";
            medicator.pillsreminder = true;
        }

        if ((config.mediflat == "ZZB") && ! medicator.bloodpressurereminder)
        {
            tkey = "events.didnowtake.bloodpressure";
            medicator.bloodpressurereminder = true;
        }

        if ((config.mediform == "ZZG") && ! medicator.bloodglucosereminder)
        {
            tkey = "events.didnowtake.bloodglucose";
            medicator.bloodglucosereminder = true;
        }

        if ((config.mediflat == "ZZW") && ! medicator.weightreminder)
        {
            tkey = "events.didnowtake.weight";
            medicator.weightreminder = true;
        }

        if (tkey)
        {
            var name = WebAppUtility.getOwnerName();
            var text = WebLibStrings.getTrans(tkey, name);

            WebAppAssistance.informAssistance(text);
        }
    }

    //
    // Mark events as completed.
    //

    for (var inx in config.events)
    {
        var event = config.events[ inx ];

        event.completed = true;

        WebAppEvents.updateComingEvent(JSON.stringify(event));

        console.log("completed=" + JSON.stringify(event));
    }
}

medicator.getNextEvents();
