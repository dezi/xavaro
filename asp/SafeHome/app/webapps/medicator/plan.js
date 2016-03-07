medicator.planEvents = function()
{
    console.log("medicator.planEvents start");

    medicator.currentevents = [];

    medicator.currentprefs = JSON.parse(WebAppPrefs.getAllPrefs());

    for (var key in medicator.currentprefs)
    {
        if (key.startsWith("medication.enable."))
        {
            var medication = key.substring(18);
            medicator.planMedication(medication);
        }
    }

    WebAppEvents.putComingEvents(JSON.stringify(medicator.currentevents));

    console.log("medicator.planEvents done");
}

medicator.planEvent = function(date, hour, medication, dose, ondemand)
{
    if (! WebLibSimple.isNumber(hour)) return;

    //
    // Daylight savings time issues here. Therefor discrete terms.
    //

    var year = date.getFullYear();
    var month = date.getMonth();
    var day = date.getDate();

    var datetime = new Date(year, month, day, hour, 0, 0);
    if (datetime.getTime() < new Date().getTime()) return;

    var event = {};

    event.date = datetime;
    event.medication = medication;
    if (dose) event.dose = dose;

    if (ondemand)
    {
        event.ondemand     = true;
        event.ondemandmax  = medicator.currentprefs[ "medication.ondemandmax."  + medication ];
        event.ondemanddose = medicator.currentprefs[ "medication.ondemanddose." + medication ];
    }

    medicator.currentevents.push(event);
}

medicator.planMedication = function(medication)
{
    for (var key in medicator.currentprefs)
    {
        if (! key.endsWith(medication)) continue;
        console.log(medicator.currentprefs[ key ] + "=" + key);
    }

    //
    // Step one: Figure out medication days.
    //

    var dura = medicator.currentprefs[ "medication.dura." + medication ];
    if (! dura || dura == "paused") return;

    var date = medicator.currentprefs[ "medication.date." + medication ];
    date = WebLibSimple.getPickerDate(date);
    if (! date) return;

    var ende = new Date(new Date().getTime() + 4 * 86400 * 1000);

    if (dura == "numdays")
    {
        var numdays = medicator.currentprefs[ "medication.numdays." + medication ];
        if (! numdays) return;

        ende = new Date(date.getTime() + numdays * 86400 * 1000);
    }

    //console.log("Startdate=" + date.toLocaleString());
    //console.log("Endedate=" + ende.toLocaleString());

    var days = medicator.currentprefs[ "medication.days." + medication ];
    if (! days) return;

    //
    // Start plan in one minutes distance.
    //

    var today = WebLibSimple.getTodayDate();

    //console.log("Todaydate=" + today.toLocaleString());

    var plandays = [];

    if ((days == "daily") || (days == "ondemand"))
    {
        console.log("daily");

        while (today.getTime() <= ende.getTime())
        {
            plandays.push(today);

            today = new Date(today.getTime() + 86400 * 1000);
        }
    }

    if (days == "weekdays")
    {
        var wdays = medicator.currentprefs[ "medication.wdays." + medication ];
        if (! wdays) return;

        var wdaysorder = [ "w7", "w1", "w2", "w3", "w4", "w5", "w6" ];

        while (today.getTime() <= ende.getTime())
        {
            var wday = wdaysorder[ today.getDay() ];

            for (var inx = 0; inx < wdays.length; inx++)
            {
                if (wday == wdays[ inx ])
                {
                    plandays.push(today);

                    break;
                }
            }

            today = new Date(today.getTime() + 86400 * 1000);
        }
    }

    if ((days == "interval") || (days == "weekly"))
    {
        var idays = (days == "weekly") ? 7 : medicator.currentprefs[ "medication.idays." + medication ];
        if (! idays) return;

        var start = new Date(date.getTime());

        while (start.getTime() <= ende.getTime())
        {
            if (start.getTime() >= today.getTime())
            {
                plandays.push(start);
            }

            start = new Date(start.getTime() + idays * 86400 * 1000);
        }
    }

    if (days == "monthly")
    {
        var monthdays = [ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 ];

        var year  = date.getFullYear();
        var month = date.getMonth();
        var day   = date.getDate();

        var start = new Date(date.getTime());

        while (start.getTime() <= ende.getTime())
        {
            if (start.getTime() >= today.getTime())
            {
                plandays.push(start);
            }

            if (month < 11)
            {
                month += 1;
            }
            else
            {
                month = 0;
                year += 1;
            }

            var sday = (day > monthdays[ month ]) ? monthdays[ month ] : day;

            start = new Date(year, month, sday);
        }
    }

    //
    // Step two: Figure out medication times.
    //

    for (var inx = 0; inx < plandays.length; inx++)
    {
        if (days == "ondemand")
        {
            medicator.planEvent(plandays[ inx ], 0, medication, 0, true);
        }
        else
        {
            var hour = medicator.currentprefs[ "medication.time1." + medication ];
            var dose = medicator.currentprefs[ "medication.dose1." + medication ];
            medicator.planEvent(plandays[ inx ], hour, medication, dose);

            var hour = medicator.currentprefs[ "medication.time2." + medication ];
            var dose = medicator.currentprefs[ "medication.dose2." + medication ];
            medicator.planEvent(plandays[ inx ], hour, medication, dose);

            var hour = medicator.currentprefs[ "medication.time3." + medication ];
            var dose = medicator.currentprefs[ "medication.dose3." + medication ];
            medicator.planEvent(plandays[ inx ], hour, medication, dose);

            var hour = medicator.currentprefs[ "medication.time4." + medication ];
            var dose = medicator.currentprefs[ "medication.dose4." + medication ];
            medicator.planEvent(plandays[ inx ], hour, medication, dose);
        }
    }
}

