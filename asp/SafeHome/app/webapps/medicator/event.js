medicator.getNextEvents = function()
{
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
        var formkey = event.date + ":" + (mediform.startsWith("ZZ") ? mediform : "AAA");

        if (! configs[ formkey ])
        {
            //
            // Create a new launch item for this event.
            //

            var config = {};
            config.date = event.date;
            config.mediform = mediform;

            configs[ formkey ] = config;
        }
    }

    for (var formkey in configs)
    {
        var config = configs[ formkey ];
        console.log("config=" + JSON.stringify(config));
    }

    //WebAppSpeak.speak("De Javascripten kann nun sprechen");

    setTimeout(medicator.getNextEvents, 10000);
}

medicator.getNextEvents();
