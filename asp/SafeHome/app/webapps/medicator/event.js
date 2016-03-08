medicator.remindMaximum = 3;
medicator.remindIntervall = 60;

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

medicator.getNextEvents = function()
{
    if (medicator.requestUnloadTimer)
    {
        clearTimeout(medicator.requestUnloadTimer);
        medicator.requestUnloadTimer = null;
    }

    var events = JSON.parse(WebAppEvents.getCurrentEvents());

    var configs = {};

    for (var inx = 0; inx < events.length; inx++)
    {
        var event = events[ inx ];

        if (event.ondemand)
        {
            //
            // Ondemand medication is ignored.
            //

            continue;
        }

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

        for (var inx in config.events)
        {
            var event = config.events[ inx ];

            alltaken = alltaken && event.taken;
        }

        if (alltaken)
        {
            medicator.completeConfig(config);
            continue;
        }

        medicator.remindConfig(config);
    }

    medicator.getNextEventsTimer = setTimeout(medicator.getNextEvents,  10 * 1000);
    medicator.requestUnloadTimer = setTimeout(medicator.requestUnload, 100 * 1000);
}

medicator.remindConfig = function(config)
{
    //
    // Take current reminded date and count from first event.
    //

    var reminded = config.events[ 0 ].reminded;
    var remindeddate = config.events[ 0 ].remindedate;

    var nowtime = new Date().getTime();
    var remtime = remindeddate ? new Date(remindeddate).getTime() : 0;

    if ((remtime + (medicator.remindIntervall * 1000)) < nowtime)
    {
        if (config.mediflat == "AAA")
        {
            WebAppSpeak.speak(WebLibStrings.getTrans("events.take.pills"));

            if ((reminded + 1) >= medicator.remindMaximum)
            {
                var name = WebAppUtility.getOwnerName();
                var text = WebLibStrings.getTrans("events.didnotyettake.pills", name);

                WebAppAssistance.informAssistance(text);
            }
        }

        if (config.mediflat == "ZZB")
        {
            WebAppSpeak.speak(WebLibStrings.getTrans("events.take.bloodpressure"));
        }

        if (config.mediform == "ZZG")
        {
            WebAppSpeak.speak(WebLibStrings.getTrans("events.take.bloodglucose"));
        }

        if (config.mediflat == "ZZW")
        {
            WebAppSpeak.speak(WebLibStrings.getTrans("events.take.weight"));
        }

        //
        // Update reminded date and count.
        //

        for (var inx in config.events)
        {
            var event = config.events[ inx ];

            event.reminded = event.reminded ? event.reminded + 1 : 1;
            event.remindeddate = new Date().toISOString();

            if (event.reminded >= medicator.remindMaximum)
            {
                event.completed = true;
            }

            WebAppEvents.updateComingEvent(JSON.stringify(event));

            console.log("reminded=" + JSON.stringify(event));
        }
    }
}

medicator.completeConfig = function(config)
{
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
