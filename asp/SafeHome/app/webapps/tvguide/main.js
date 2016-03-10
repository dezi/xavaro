tvguide.constants =
{
    title                    : "Title",
    loadHours                : 48 * 2,
    timelineInterval         : 15,

    today                    : Math.floor(WebLibSimple.getTodayDate().getTime() / 1000 / 60),

    epgDataSrcPrefix         : "http://" + WebApp.manifest.appserver + "/epgdata/",
    epgDataSrcPostfix        : "/current.json.gz",

    titlebarHeight           : 80,
    senderbarWidth           : 80,
    senderHeight             : 64,
    timelineHeight           : 30,
    hoursPix                 : 360,

    titlebarColor            : "#888888",
    content1Color            : "#dedede",
    content2Color            : "#acacac",
    senderImgDivColor        : "#ffffff",
    senderbarColor           : "#585858",
    epgdataColor             : "#dedede",
    epgScrollColor           : "#ffffff",
    epgScrollStyleColor      : "#ffffff",
    epgScrollfontWeight      : "bold",
    timelineColor            : "#dedede",
    timelineDivColor         : "#ffffff",
    timelineScrollStyleColor : "#000000",
    timelineScrollfontWeight : "bold",
    programBGColor           : "#a0a0a0",
    descriptionColor         : "#ffffff",
    pastLineColor            : "#33ff0000",

    programBoder             : "0px solid black"
}

tvguide.createFrameSetup = function()
{
    //
    // tvguide.topdiv
    //

    tvguide.topdiv = WebLibSimple.createDiv(
        0,
        0,
        0,
        0,
        "topdiv",
        document.body
    );

    //
    // tvguide.titlebar
    //

    tvguide.titlebar = WebLibSimple.createDivHeight(
        0,
        0,
        0,
        tvguide.constants.titlebarHeight,
        "titlebar",
        tvguide.topdiv
    );

    WebLibSimple.setBGColor(tvguide.titlebar, tvguide.constants.titlebarColor);

    tvguide.title = WebLibSimple.createDiv(
        tvguide.constants.titlebarHeight,
        tvguide.constants.titlebarHeight / 4,
        tvguide.constants.titlebarHeight,
        0,
        "title",
        tvguide.titlebar
    );

    tvguide.title.style.textAlign = "center";
    tvguide.title.innerHTML       = tvguide.constants.title;

    WebLibSimple.setFontSpecs(tvguide.title, 34, "bold", "#ffffff");

    //
    // tvguide.content1
    //

    tvguide.content1 = WebLibSimple.createDiv(
        0,
        tvguide.constants.titlebarHeight,
        0,
        0,
        "content1",
        tvguide.topdiv
    );

    tvguide.content1.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.content1, tvguide.constants.titlebarColor);

    //
    // tvguide.timeline
    //

    tvguide.timeline = WebLibSimple.createDivHeight(
        0,
        0,
        0,
        tvguide.constants.timelineHeight,
        "timeline",
        tvguide.content1
    );

    tvguide.timeline.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.timeline, tvguide.constants.timelineColor);

    //
    // tvguide.content2
    //

    tvguide.content2 = WebLibSimple.createDiv(
        0,
        tvguide.constants.timelineHeight,
        0,
        0,
        "content2",
        tvguide.content1
    );

    WebLibSimple.setBGColor(tvguide.content2, tvguide.constants.content2Color);

    //
    // tvguide.epgbody
    //

    tvguide.epgbody = WebLibSimple.createDiv(
        0,
        0,
        0,
        0,
        "epgdata",
        tvguide.content2
    );

    tvguide.epgbody.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.epgbody, tvguide.constants.epgdataColor);

    //
    // tvguide.senderbar
    //

    tvguide.senderbar = WebLibSimple.createDivWidth(
        0,
        0,
        tvguide.constants.senderbarWidth,
        0,
        "senderbar",
        tvguide.content2
    );

    tvguide.senderbar.style.overflow = "hidden";

    WebLibSimple.setBGColor(tvguide.senderbar, tvguide.constants.senderbarColor);

    //
    // tvguide.description
    //

    tvguide.description = WebLibSimple.createDivWidth(
        "100%",
        0,
        "50%",
        0,
        "description",
        tvguide.content1
    );

    tvguide.description.style.zIndex = "3";

    WebLibSimple.setBGColor(tvguide.description, tvguide.constants.descriptionColor);
}

tvguide.getSenderList = function()
{
//    var obj = JSON.parse(WebAppRequest.loadSync("http://epg.xavaro.de/channels/tv/de.json.gz"));
//
//    tvguide.senderList = [];
//
//    for(var index in obj)
//    {
//        tvguide.senderList.push("tv/de/" + obj[ index ].name);
//    }

    tvguide.senderList =
    [
        "tv/de/Das Erste",
        "tv/de/ZDF",
        "tv/de/Sat. 1 Deutschland",
        "tv/de/RTL Deutschland",
        "tv/de/Pro Sieben Deutschland",
        "tv/de/Kabel Eins Deutschland",
        "tv/de/Vox Deutschland",
        "tv/de/RTL 2 Deutschland",
        "tv/de/Tele 5",
        "tv/de/Sixx Deutschland",
        "tv/de/Eins Plus",
        "tv/de/3sat",
        "tv/de/NDR Fernsehen Hamburg",
        "tv/de/WDR Fernsehen Köln",
        "tv/de/MDR Fernsehen Sachsen-Anhalt",
        "tv/de/Bayerisches Fernsehen Nord",
        "tv/de/ZDF info",
        "tv/de/ZDF Kultur",
        "tv/de/ZDF neo",
        "tv/de/Einsfestival"
    ];
}

tvguide.createSenderBar = function()
{
    tvguide.senderbarScroll = WebLibSimple.createDivHeight(
        0,
        0,
        0,
        null,
        "senderbarScroll",
        tvguide.senderbar
    );

    tvguide.senderbarScroll.scrollVertical = true;
    tvguide.senderbarScroll.onTouchScroll  = tvguide.onSenderBarScroll;
    tvguide.senderbarScroll.style.zIndex   = "2";

    for(var index in tvguide.senderList)
    {
        var senderImgDiv = WebLibSimple.createAnyAppend("div", tvguide.senderbarScroll);
        senderImgDiv.style.position = "relative";
        senderImgDiv.style.height   = tvguide.constants.senderHeight + "px";

        WebLibSimple.setBGColor(senderImgDiv, tvguide.constants.senderImgDivColor);

        var paddingDiv = WebLibSimple.createDiv(
            0,
            0,
            0,
            0,
            null,
            senderImgDiv
        );

        paddingDiv.style.paddingLeft     = "7px";
        paddingDiv.style.paddingTop      = "8px";
        paddingDiv.style.paddingRight    = "7px";
        paddingDiv.style.paddingBottom   = "7px";

        var image = WebLibSimple.createAnyAppend("img", paddingDiv);
        image.style.height = "100%";
        image.style.width  = "100%";
        image.src          = encodeURI(
            "http://" + WebApp.manifest.appserver   + "/channels/"
                      + tvguide.senderList[ index ] + ".png"
        );
    }
}

tvguide.createTimeLine = function()
{
    tvguide.timelineScroll = WebLibSimple.createDivWidth(
        0,
        0,
        (tvguide.constants.loadHours * tvguide.constants.hoursPix),
        0,
        "timelineScroll",
        tvguide.timeline
    );

    tvguide.timelineScroll.scrollHorizontal = true;
    tvguide.timelineScroll.onTouchScroll    = tvguide.onTimeLineScroll;
    tvguide.timelineScroll.style.color      = tvguide.constants.timelineScrollStyleColor;
    tvguide.timelineScroll.style.fontWeight = tvguide.constants.timelineScrollfontWeight;

    for(var min = 0; min < (tvguide.constants.loadHours * 60); min += tvguide.constants.timelineInterval)
    {
        var timelineDiv = WebLibSimple.createDivWidth(
            (tvguide.constants.hoursPix / 60 * min),
            0,
            tvguide.constants.hoursPix / 60 * tvguide.constants.timelineInterval,
            0,
            null,
            tvguide.timelineScroll
        );

        WebLibSimple.setBGColor(timelineDiv, tvguide.constants.timelineDivColor);


        if (min % (tvguide.constants.timelineInterval * 2))
        {
            var timeSpan = WebLibSimple.createAnyAppend("span", timelineDiv);
            timeSpan.style.border = "1px dotted #e00073";

        }
        else
        {
            var time = tvguide.constants.today + min;
            var date = new Date(time * 1000 * 60);

            var timeSpan = WebLibSimple.createAnyAppend("span", timelineDiv);
            timeSpan.textContent = WebLibSimple.padNum(date.getHours(), 2) + ":" +
                                   WebLibSimple.padNum(date.getMinutes(), 2);
        }
    }
}

tvguide.createEpgProgram = function(channel, epgdata)
{
    console.log("Create channel: " + channel + " ==> " + epgdata.length);

    var senderIndex = tvguide.senderList.indexOf(channel);
    var minPix = tvguide.constants.hoursPix / 60;

    for (var programIndex in epgdata)
    {
        var startTime = Math.floor((new Date(epgdata[ programIndex ].start).getTime() / 1000 / 60) - tvguide.constants.today);
        var stopTime  = Math.floor((new Date(epgdata[ programIndex ].stop ).getTime() / 1000 / 60) - tvguide.constants.today);
        var duration  = stopTime - startTime;

        var programDiv = WebLibSimple.createDivWidHei(
                        startTime * minPix,
                        tvguide.constants.senderHeight * senderIndex,
                        duration * minPix,
                        tvguide.constants.senderHeight,
                        null, tvguide.epgScroll);

        programDiv.style.position = "absolute";

        var paddingDiv = WebLibSimple.createDiv(0, 0, 0, 0, null, programDiv);
        paddingDiv.style.marginLeft   = "4px";
        paddingDiv.style.marginTop    = "2px";
        paddingDiv.style.marginRight  = "0px";
        paddingDiv.style.marginBottom = "2px";

        paddingDiv.style.padding      = "8px";
        paddingDiv.style.borderRadius = "8px";

        paddingDiv.style.border          = tvguide.constants.programBoder;
        paddingDiv.style.backgroundColor = tvguide.constants.programBGColor;

        paddingDiv.epg       = epgdata[ programIndex ]
        paddingDiv.innerHTML = paddingDiv.epg.title;
    }
}

tvguide.animateInfoIn = function()
{
    var actpos = tvguide.description.style.left;
    actpos = parseInt(WebLibSimple.substring(actpos, 0, -1));

    if (actpos > 50)
    {
        actpos = actpos - 1;
        tvguide.description.style.left = actpos + "%";

        tvguide.animateInfo = setTimeout(tvguide.animateInfoIn, 5);
    }
}

tvguide.onEPGTouchClick = function(target, element)
{
    console.log("tvguide.onEPGTouchClick: element " + element.epg.title);

    WebLibSimple.setBGColor(element, "#747474");

    var tmp = WebLibSimple.createAnyAppend("h2", tvguide.description);
    tmp.textContent = element.epg.start + " : " + element.epg.stop;

//    tvguide.animateInfoIn();
}

tvguide.createEpgBodyScroll = function()
{
    tvguide.epgScroll = WebLibSimple.createDivWidHei(
        0,
        0,
        tvguide.constants.loadHours * tvguide.constants.hoursPix,
        tvguide.senderbarScroll.clientHeight,
        "epgScroll",
        tvguide.epgbody
    );

    tvguide.epgScroll.style.color      = tvguide.constants.epgScrollStyleColor;
    tvguide.epgScroll.style.fontWeight = tvguide.constants.epgScrollfontWeight;

    tvguide.epgScroll.scrollHorizontal = true;
    tvguide.epgScroll.scrollVertical   = true;

    tvguide.epgScroll.onTouchScroll    = tvguide.onEPGTouchScroll;
    tvguide.epgScroll.onTouchClick     = tvguide.onEPGTouchClick;

    WebLibSimple.setBGColor(tvguide.epgScroll, tvguide.constants.epgScrollColor);

    for (var sender in tvguide.senderList)
    {
        var epgurl = encodeURI(
            tvguide.constants.epgDataSrcPrefix +
            tvguide.senderList[ sender ] +
            tvguide.constants.epgDataSrcPostfix
        );

        WebAppRequest.loadAsyncJSON(epgurl);
    }
}

tvguide.PastLineTimeout = function()
{
    var minPix = tvguide.constants.hoursPix / 60;

    var actpos = tvguide.PastLine.style.width;
    actpos     = parseInt(WebLibSimple.substring(actpos, 0, -2));
    actpos     = actpos + minPix;

    tvguide.PastLine.style.width = actpos + "px";

    tvguide.animatePastLine = setTimeout(tvguide.PastLineTimeout, 1000 * 60);
}

tvguide.createPastLine = function()
{
    var now         = Math.floor( new Date().getTime() / 1000 / 60);
    var minPix      = tvguide.constants.hoursPix / 60;
    var nowPosition = minPix * (tvguide.constants.today - now);
    var padding     = tvguide.constants.timelineInterval * 4 * minPix;

    //
    // timeline & epg --> current time
    //

    tvguide.epgScroll.style.left      = nowPosition + padding + "px";
    tvguide.timelineScroll.style.left = nowPosition + padding + "px";

    //
    // tvguide.PastLine
    //

    tvguide.PastLine = WebLibSimple.createDivWidth(
        0,
        0,
        nowPosition * -1,
        0,
        "PastLine",
        tvguide.epgScroll
    );

    WebLibSimple.setBGColor(tvguide.PastLine, tvguide.constants.pastLineColor);

    tvguide.PastLine.style.zIndex        = "1";
    tvguide.PastLine.style.pointerEvents = "none";

    //
    // Past time animation
    //

    tvguide.PastLineTimeout();
}

WebAppRequest.onLoadAsyncJSON = function(src, data)
{
    var channel = WebLibSimple.substring(
        decodeURI(src),
        tvguide.constants.epgDataSrcPrefix.length,
        -tvguide.constants.epgDataSrcPostfix.length
    );

    var epgdata = [];

    for (var index in data.epgdata)
    {
        var indexDate = Math.floor(new Date(data.epgdata[ index ].start).getTime() / 1000 / 60);

        if (indexDate > tvguide.constants.today &&
            indexDate < (tvguide.constants.today + (tvguide.constants.loadHours * 60)))
        {
            epgdata.push(data.epgdata[ index ]);
        }
    }

    tvguide.createEpgProgram(channel, epgdata);
}

tvguide.onTimeLineScroll = function(newX, newY)
{
    tvguide.epgScroll.style.left = newX + "px";

    if (tvguide.epgScroll.style.left != tvguide.timelineScroll.style.left)
    {
        tvguide.timelineScroll.style.left = newX + "px";
    }

    // selectedDay --> Min
    var selectedDay = (tvguide.constants.today - (newX / tvguide.constants.hoursPix * 60));

    tvguide.title.innerHTML = WebLibSimple.getNiceDay(new Date(selectedDay * 1000 * 60));
}

tvguide.onSenderBarScroll = function(newX, newY)
{
    tvguide.epgScroll.style.top = newY + "px";

    if (tvguide.senderbarScroll.style.top != tvguide.epgScroll.style.top)
    {
        tvguide.senderbarScroll.style.top = newY + "px";
    }
}

tvguide.onEPGTouchScroll = function(newX, newY)
{
    if (newX !== null)
    {
        tvguide.onTimeLineScroll(newX, null);
    }

    if (newY !== null)
    {
        tvguide.onSenderBarScroll(null, newY);
    }
}

tvguide.main = function()
{
    //
    // frame setup
    //

    tvguide.createFrameSetup();

    //
    // sender list
    //

    tvguide.getSenderList();

    //
    // create scroll components
    //

    tvguide.createSenderBar();
    tvguide.createTimeLine();
    tvguide.createEpgBodyScroll();

    //
    // create pastline
    //

    tvguide.createPastLine();
}

tvguide.main();